package cn.edu.nju.ws.geoinfer.sip;

import cn.edu.nju.ws.geoinfer.data.program.Atom;
import cn.edu.nju.ws.geoinfer.data.program.Predicate;
import cn.edu.nju.ws.geoinfer.data.program.Term;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.utils.Utils;

import java.util.Set;

public class SipStrategy {
  private SipStrategy() {
  }

  /**
   * Compare which atom will be calculated first
   *
   * @param lhs left-hand atom
   * @param rhs right-hand atom
   * @param boundVariables the set of variables that are already bound
   * @return -1 represents lhs is better, 1 represents rhs is better, 0 represents equal
   */
  public static int compare(
      Atom lhs,
      Atom rhs,
      Set<String> boundVariables,
      Set<Predicate> derivedPredicateSet,
      DatabaseManager<?> dbm) {
    // First check idb or edb
    if (derivedPredicateSet.contains(lhs.getPredicate())) {
      if (derivedPredicateSet.contains(rhs.getPredicate())) {
        return compareBothDerived(lhs, rhs, boundVariables);
      } else {
        return 1;
      }
    } else if (derivedPredicateSet.contains(rhs.getPredicate())) {
      return -1;
    } else {
      return compareBothInternal(lhs, rhs, boundVariables, dbm);
    }
  }

  private static int getBoundCount(Atom atom, Set<String> boundVariables) {
    int boundCount = 0;
    for (Term term : atom.getTerms()) {
      boundCount +=
          Utils.termMap(term, constant -> 1, variable -> boundVariables.contains(variable) ? 1 : 0);
    }
    return boundCount;
  }

  private static int compareBothDerived(Atom lhs, Atom rhs, Set<String> boundVariables) {
    // Both derived, compare bound count
    int boundCountLeft = getBoundCount(lhs, boundVariables);
    int boundCountRight = getBoundCount(rhs, boundVariables);
    // The order of left-right is inverted intentionally
    return Integer.compare(boundCountRight, boundCountLeft);
  }

  private static int compareBothInternal(
      Atom lhs, Atom rhs, Set<String> boundVariables, DatabaseManager<?> dbm) {
    // TODO use bound variables to further optimize
    if (dbm == null) {
      return 0;
    } else {
      return Integer.compare(
          dbm.getTableSize(lhs.getPredicate().getTableName()),
          dbm.getTableSize(rhs.getPredicate().getTableName()));
    }
  }
}
