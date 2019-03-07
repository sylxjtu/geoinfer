package cn.edu.nju.ws.geoinfer.ruleapply;

import cn.edu.nju.ws.geoinfer.data.program.Predicate;
import cn.edu.nju.ws.geoinfer.data.program.Rule;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.db.DatabaseTable;

public class RuleApplier {
  private RuleApplier() {
  }

  public static <T extends DatabaseTable> void applyRule(Rule rule, DatabaseManager<T> dbm) {
    new RuleApplierManager<T>().applyRule(rule, dbm);
  }

  /**
   * @param predicate
   * @param dbm
   * @param <T>       Database table type
   * @return if semi naive need to continue
   */
  public static <T extends DatabaseTable> boolean updatePointer(
      Predicate predicate, DatabaseManager<T> dbm) {
    return new RuleApplierManager<T>().updatePointer(predicate, dbm);
  }
}
