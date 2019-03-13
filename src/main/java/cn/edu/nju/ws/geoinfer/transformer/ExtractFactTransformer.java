package cn.edu.nju.ws.geoinfer.transformer;

import cn.edu.nju.ws.geoinfer.data.program.*;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.ruleapply.RuleApplier;
import cn.edu.nju.ws.geoinfer.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extracts facts (predicates with only rules with empty body) from program, and inserts them to db
 */
public class ExtractFactTransformer implements Transformer {
  private DatabaseManager<?> dbm;

  public ExtractFactTransformer(DatabaseManager<?> dbm) {
    this.dbm = dbm;
  }

  @Override
  public Program transform(Program program) {
    Set<Predicate> rulePredicates = new HashSet<>();
    for (Rule rule : program.getRules()) {
      Predicate headPredicate = rule.getHead().getPredicate();
      if (!(headPredicate instanceof RawPredicate)) {
        throw new IllegalArgumentException();
      }
      if (!rule.getBody().isEmpty()) {
        rulePredicates.add(headPredicate);
      }
    }
    List<Rule> newRules = new ArrayList<>();
    for (Rule rule : program.getRules()) {
      if (rulePredicates.contains(rule.getHead().getPredicate())) {
        newRules.add(Utils.clone(rule));
      } else {
        dbm.createTable(
            rule.getHead().getPredicate().getTableName(), rule.getHead().getTerms().size(), false);
        RuleApplier.applyRule(rule, dbm);
      }
    }
    Atom newGoal = Utils.clone(program.getGoal());
    return new Program(newRules, newGoal);
  }
}
