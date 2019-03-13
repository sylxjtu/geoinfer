package cn.edu.nju.ws.geoinfer.transformer;

import cn.edu.nju.ws.geoinfer.data.program.*;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.sip.SipStrategy;
import cn.edu.nju.ws.geoinfer.utils.Utils;

import java.util.*;

/**
 * Transform a program by adding predicate adornment using heuristic side information passing
 * strategy
 */
public class SipTransformer implements Transformer {
  private List<AdornPredicate> predicateList;
  private Map<AdornPredicate, Integer> predicateMap;
  private int predicateCount = 0;

  private Map<Predicate, List<Rule>> headPredicateRuleListMap;
  private Set<Predicate> derivedPredicateSet;

  private DatabaseManager<?> dbm;

  public SipTransformer(DatabaseManager<?> dbm) {
    this();
    this.dbm = dbm;
  }

  public SipTransformer() {
    predicateMap = new HashMap<>();
    predicateList = new ArrayList<>();
  }

  @Override
  public Program transform(Program program) {
    List<Rule> rules = program.getRules();
    Atom goal = program.getGoal();

    derivedPredicateSet = getDerivedPredicates(rules);
    headPredicateRuleListMap = getHeadPredicateRuleListMap(rules);
    Atom newGoal = getNewGoal(goal);
    addPredicate((AdornPredicate) newGoal.getPredicate());
    List<Rule> newRules = getNewRules();

    return new Program(newRules, newGoal);
  }

  /**
   * Get all derived (i.e. IDB) predicates from rule set
   *
   * @param rules input rules
   * @return a set of all derived predicates
   */
  private Set<Predicate> getDerivedPredicates(List<Rule> rules) {
    Set<Predicate> ret = new HashSet<>();
    for (Rule rule : rules) {
      Predicate derivedPredicate = rule.getHead().getPredicate();
      assert derivedPredicate instanceof RawPredicate;
      ret.add(derivedPredicate);
    }
    return ret;
  }

  /**
   * Get rules for predicate
   *
   * @param rules input rules
   * @return a map which the key is predicate and value is the list of rules has this predicate as
   *     head
   */
  private Map<Predicate, List<Rule>> getHeadPredicateRuleListMap(List<Rule> rules) {
    Map<Predicate, List<Rule>> ret = new HashMap<>();
    for (Rule rule : rules) {
      Predicate predicate = rule.getHead().getPredicate();
      ret.computeIfAbsent(predicate, key -> new ArrayList<>());
      ret.get(predicate).add(rule);
    }
    return ret;
  }

  /**
   * Extracts adorn from atom, constant to bound var, variable to free var
   *
   * @param atom input atom
   * @return the extracted adorn
   */
  private String extractAdornFromAtom(Atom atom) {
    StringBuilder stringBuilder = new StringBuilder();

    for (Term term : atom.getTerms()) {
      stringBuilder.append(Utils.<Character>termMap(term, constant -> 'b', variable -> 'f'));
    }

    return stringBuilder.toString();
  }

  /**
   * Get adorned goal predicate
   *
   * @param goal former goal predicate
   * @return adorned goal predicate
   */
  private Atom getNewGoal(Atom goal) {
    // Goal must be raw predicate
    if (!(goal.getPredicate() instanceof RawPredicate)) {
      throw new IllegalArgumentException();
    }
    // Add goal predicate
    String adorn = extractAdornFromAtom(goal);
    AdornPredicate adornGoalPredicate = new AdornPredicate(goal.getPredicate(), adorn);
    Atom adornGoal = Utils.clone(goal);
    adornGoal.setPredicate(adornGoalPredicate);
    return adornGoal;
  }

  /**
   * Add adorned predicate to await computing
   *
   * @param predicate the predicate to be added
   */
  private void addPredicate(AdornPredicate predicate) {
    if (predicateMap.containsKey(predicate)) return;
    predicateList.add(predicate);
    predicateMap.put(predicate, predicateCount++);
  }

  /**
   * Transforms rules
   *
   * @return transformed rules
   */
  private List<Rule> getNewRules() {
    List<Rule> output = new ArrayList<>();

    for (int i = 0; i < predicateCount; i++) {
      AdornPredicate predicate = predicateList.get(i);
      // Get the original predicate's rules
      List<Rule> ruleList = headPredicateRuleListMap.get(predicate.getInnerPredicate());
      if (ruleList == null) continue;
      for (Rule rule : ruleList) {
        Atom newHead = getNewHead(rule, predicate);
        List<Atom> newBody = getNewBody(rule, predicate, newHead);
        Rule newRule = new Rule(newHead, newBody);
        output.add(newRule);
      }
    }
    return output;
  }

  /**
   * Transforms rule head
   *
   * @param rule the rule whose head needs to be transformed
   * @param predicate new predicate
   * @return new rule head
   */
  private Atom getNewHead(Rule rule, AdornPredicate predicate) {
    // New rule will have the adorned predicate as head
    Atom newHead = Utils.clone(rule.getHead());
    newHead.setPredicate(predicate);
    return newHead;
  }

  /**
   * Transforms rule body
   *
   * @param rule the rule whose body needs to be transformed
   * @param predicate new predicate
   * @param newHead new rule head
   * @return new rule body
   */
  private List<Atom> getNewBody(Rule rule, AdornPredicate predicate, Atom newHead) {
    List<Atom> ret = new ArrayList<>();

    // An array to record which atom has been added to new rule
    boolean[] selected = new boolean[rule.getBody().size()];
    // A set of variables that has been bounded
    Set<String> boundVariables = new HashSet<>();

    // Add all already bounded variable (bound by head)
    for (int j = 0; j < predicate.getAdorn().length(); j++) {
      Term term = newHead.getTerms().get(j);
      if (predicate.getAdorn().charAt(j) == 'b') {
        Utils.termMapIfVar(term, boundVariables::add);
      }
    }

    // Select body atoms to add to new rule
    // If rule has empty body (fact), new rule will have empty body too
    for (int selectCount = 0; selectCount < rule.getBody().size(); selectCount++) {
      // Add new atom to new rule
      int selectedAtomIndex = selectAtomToAppend(rule, selected, boundVariables);
      selected[selectedAtomIndex] = true;
      Atom transformedAtom = transformAtom(rule.getBody().get(selectedAtomIndex), boundVariables);
      ret.add(transformedAtom);
    }
    return ret;
  }

  /**
   * Select a atom to append to new rule body using bound variable count heuristics
   *
   * @param rule original rule
   * @param selected selected rule body
   * @param boundVariables bounded variables
   * @return selected atom
   */
  private int selectAtomToAppend(Rule rule, boolean[] selected, Set<String> boundVariables) {
    // Initialize candidate index to -1
    int candidateIndex = -1;

    // Find atom with heuristics
    // Stop when we encounter builtin atom (the calculation order of builtin is determined by the
    // programmer)
    for (int atomIndex = 0; atomIndex < rule.getBody().size(); atomIndex++) {
      if (selected[atomIndex]) continue;
      Atom atom = rule.getBody().get(atomIndex);
      Atom candidate = candidateIndex == -1 ? null : rule.getBody().get(candidateIndex);

      boolean shouldForceSip =
          (atom.getPredicate() instanceof BuiltinPredicate
              || atom.getPredicate() instanceof ForceSipPredicate);

      if (shouldForceSip) return candidate == null ? atomIndex : candidateIndex;
      if (candidate == null
          || SipStrategy.compare(candidate, atom, boundVariables, derivedPredicateSet, dbm) > 0) {
        candidateIndex = atomIndex;
      }
    }

    return candidateIndex;
  }

  private Atom transformAtom(Atom atom, Set<String> boundVariables) {
    Atom newAtom = Utils.clone(atom);

    StringBuilder adorn = new StringBuilder();
    for (Term term : atom.getTerms()) {
      if (term instanceof Constant) {
        adorn.append('b');
      } else if (term instanceof Variable) {
        if (boundVariables.contains(((Variable) term).getName())) {
          adorn.append('b');
        } else {
          adorn.append('f');
        }
      } else {
        throw new IllegalArgumentException();
      }
    }

    // Add new bound variables to the set
    for (Term term : newAtom.getTerms()) {
      Utils.termMapIfVar(term, boundVariables::add);
    }

    // Add the newly produced adorn predicate for further computation
    // Only derived predicate is added since only derived predicate is used in rule head
    Predicate predicate = newAtom.getPredicate();
    // Convert force sip predicate to raw predicate since we don't need them
    if (predicate instanceof ForceSipPredicate) {
      predicate = new RawPredicate(predicate.getFullName());
    }
    if (derivedPredicateSet.contains(predicate)) {
      AdornPredicate adornPredicate = new AdornPredicate(predicate, adorn.toString());
      newAtom.setPredicate(adornPredicate);
      addPredicate(adornPredicate);
    } else {
      newAtom.setPredicate(predicate);
    }

    return newAtom;
  }
}
