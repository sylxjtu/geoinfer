package cn.edu.nju.ws.geoinfer.transformer;

import cn.edu.nju.ws.geoinfer.data.program.*;
import cn.edu.nju.ws.geoinfer.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SelfRecursionAvoidTransformer implements Transformer {
  @Override
  public Program transform(Program program) {
    Map<TwinPredicate, Integer> twinPredicateMap = new HashMap<>();
    List<Rule> newRules = new ArrayList<>();
    for (Rule rule : program.getRules()) {
      Rule newRule = Utils.clone(rule);
      newRule.setBody(
          rule.getBody().stream()
              .map(
                  atom -> {
                    if (rule.getHead().getPredicate().equals(atom.getPredicate())) {
                      Atom newAtom = Utils.clone(atom);
                      TwinPredicate twinPredicate = new TwinPredicate(atom.getPredicate());
                      newAtom.setPredicate(twinPredicate);
                      twinPredicateMap.computeIfAbsent(
                          twinPredicate, predicate -> atom.getTerms().size());
                      return newAtom;
                    } else {
                      return atom;
                    }
                  })
              .collect(Collectors.toList()));
      newRules.add(newRule);
    }
    twinPredicateMap.forEach(
        (twinPredicate, arity) ->
            newRules.add(
                new Rule(
                    new Atom(
                        twinPredicate,
                        IntStream.range(0, arity)
                            .mapToObj(id -> new Variable("_" + id))
                            .collect(Collectors.toList())),
                    Collections.singletonList(
                        new Atom(
                            twinPredicate.getInnerPredicate(),
                            IntStream.range(0, arity)
                                .mapToObj(id -> new Variable("_" + id))
                                .collect(Collectors.toList()))))));
    return new Program(newRules, program.getGoal());
  }
}
