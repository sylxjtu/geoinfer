package cn.edu.nju.ws.geoinfer.transformer;

import cn.edu.nju.ws.geoinfer.data.program.*;
import cn.edu.nju.ws.geoinfer.utils.Utils;

import java.util.*;

/**
 * Implements generalized supplementary magic sets rewriting algorithm.
 */
public class SupMagicTransformer implements Transformer {
  private List<Rule> outputRules;
  private List<Rule> inputRules = null;
  private List<List<Atom>> supMagicAtoms;

  public SupMagicTransformer() {
    outputRules = new ArrayList<>();
    supMagicAtoms = new ArrayList<>();
  }

  @Override
  public Program transform(Program program) {
    Atom newGoal = Utils.clone(program.getGoal());
    inputRules = program.getRules();

    addSupMagicRules();
    addMagicRules();
    addModifiedRules();
    addSeed(program.getGoal());

    return new Program(outputRules, newGoal);
  }

  /** Add rules of the form sup_magic_%s_%d_%d() */
  private void addSupMagicRules() {
    for (int ruleIndex = 0; ruleIndex < inputRules.size(); ruleIndex++) {
      Rule rule = inputRules.get(ruleIndex);

      // Find an adorned atom to transform
      // Because it's adorned, it needs a magic atom to bound variable to it
      int currentTransformAtomIndex = 0;
      for (int atomIndex = rule.getBody().size() - 1; atomIndex >= 0; atomIndex--) {
        Atom atom = rule.getBody().get(atomIndex);
        if (needBound(atom)) {
          currentTransformAtomIndex = atomIndex;
          break;
        }
      }
      Map<String, Integer> variableCount = new HashMap<>();
      for (Term term : rule.getHead().getTerms()) {
        Utils.termMapIfVar(term, variable -> variableCount.merge(variable, 1, Integer::sum));
      }
      for (Atom bodyAtom : rule.getBody()) {
        for (Term term : bodyAtom.getTerms()) {
          Utils.termMapIfVar(term, variable -> variableCount.merge(variable, 1, Integer::sum));
        }
      }

      // First supmagic atom is single magic atom
      Atom lastSupMagicAtom = adornedAtomToMagic(rule.getHead());
      List<Atom> atomList = new ArrayList<>();
      atomList.add(lastSupMagicAtom);

      for (int atomIndex = 0; atomIndex < currentTransformAtomIndex; atomIndex++) {
        // Atom index 0 is synonym of single magic atom
        Atom currentSupMagicAtom =
            new Atom(
                new SupMagicPredicate(rule.getHead().getPredicate(), ruleIndex, atomIndex + 1),
                new ArrayList<>());
        Atom bodyAtom = rule.getBody().get(atomIndex);

        List<Term> allTerms = new ArrayList<>();
        allTerms.addAll(lastSupMagicAtom.getTerms());
        allTerms.addAll(bodyAtom.getTerms());
        Set<Variable> occurVariable = new HashSet<>();
        for (Term term : bodyAtom.getTerms()) {
          Utils.termMapIfVar(term, variable -> variableCount.merge(variable, -1, Integer::sum));
        }
        // Determine the terms of the newly generated supmagic atom
        for (Term term : allTerms) {
          if (term instanceof Variable) {
            Variable variable = (Variable) term;
            if (!occurVariable.contains(variable)
                && variableCount.getOrDefault(variable.getName(), 0) > 0) {
              occurVariable.add(variable);
              currentSupMagicAtom.getTerms().add(variable);
            }
          }
        }
        List<Atom> newBody =
            needBound(rule.getHead())
                ? Arrays.asList(lastSupMagicAtom, bodyAtom)
                : Collections.singletonList(bodyAtom);
        Rule newRule = new Rule(currentSupMagicAtom, newBody);
        outputRules.add(newRule);

        // Add all supmagic atoms according to this rule
        atomList.add(currentSupMagicAtom);
        lastSupMagicAtom = currentSupMagicAtom;
      }
      supMagicAtoms.add(atomList);
    }
  }

  /**
   * Transform atom with adorn predicate to a magic atom according to the adorn variable binding
   *
   * @param adornedAtom the adorned atom to be transformed
   * @return transformed magic atom
   */
  private Atom adornedAtomToMagic(Atom adornedAtom) {
    Predicate predicate = adornedAtom.getPredicate();
    if (!(predicate instanceof AdornPredicate)) {
      throw new IllegalArgumentException();
    }

    AdornPredicate adornedPredicate = ((AdornPredicate) predicate);
    Predicate magicPredicate =
        new MagicPredicate(adornedPredicate.getInnerPredicate(), adornedPredicate.getAdorn());
    List<Term> magicTerms = new ArrayList<>();
    for (int termIndex = 0; termIndex < adornedPredicate.getAdorn().length(); termIndex++) {
      if (adornedPredicate.getAdorn().charAt(termIndex) == 'b') {
        Term term = Utils.clone(adornedAtom.getTerms().get(termIndex));
        magicTerms.add(term);
      }
    }
    return new Atom(magicPredicate, magicTerms);
  }

  /** Add rules of the form magic_%s() */
  private void addMagicRules() {
    for (int ruleIndex = 0; ruleIndex < inputRules.size(); ruleIndex++) {
      Rule rule = inputRules.get(ruleIndex);
      for (int atomIndex = 0; atomIndex < rule.getBody().size(); atomIndex++) {
        Atom atom = rule.getBody().get(atomIndex);
        if (!needBound(atom)) {
          continue;
        }
        List<Atom> magicBody =
            Collections.singletonList(supMagicAtoms.get(ruleIndex).get(atomIndex));
        Atom magicHead = adornedAtomToMagic(atom);
        outputRules.add(new Rule(magicHead, magicBody));
      }
    }
  }

  /** Modify rules to include magic and supmagic atoms */
  private void addModifiedRules() {
    for (int ruleIndex = 0; ruleIndex < inputRules.size(); ruleIndex++) {
      Rule rule = inputRules.get(ruleIndex);
      List<Atom> newRuleBody = new ArrayList<>();
      Rule newRule = new Rule(rule.getHead(), newRuleBody);
      int atomIndex = supMagicAtoms.get(ruleIndex).size() - 1;
      if (needBound(rule.getHead()) || atomIndex > 0) {
        newRule.getBody().add(supMagicAtoms.get(ruleIndex).get(atomIndex));
      }
      for (int cloneAtomIndex = atomIndex;
           cloneAtomIndex < rule.getBody().size();
           cloneAtomIndex++) {
        Atom atom = rule.getBody().get(cloneAtomIndex);
        newRule.getBody().add(Utils.clone(atom));
      }
      outputRules.add(newRule);
    }
  }

  /**
   * Add magic seed
   *
   * @param goal the goal of the new program, which has the initial binding
   */
  private void addSeed(Atom goal) {
    if (needBound(goal)) outputRules.add(new Rule(adornedAtomToMagic(goal), new ArrayList<>()));
  }

  /**
   * Checks if a atom need variable bound
   *
   * @param atom the atom to be checked
   * @return if atom need variable bound
   */
  private boolean needBound(Atom atom) {
    Predicate predicate = atom.getPredicate();
    if (!(predicate instanceof AdornPredicate)) return false;
    return ((AdornPredicate) predicate).getAdorn().chars().anyMatch(x -> x == 'b');
  }
}
