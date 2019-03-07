package cn.edu.nju.ws.geoinfer.utils;

import cn.edu.nju.ws.geoinfer.data.program.Atom;

public class Unifier {
  private Unifier() {
  }

  /**
   * Checks two atoms unify
   *
   * @param lhs left operand
   * @param rhs right operand
   * @return whether two atoms unify
   * @implNote incomplete implementation for effectiveness
   */
  public static boolean checkAtomUnify(Atom lhs, Atom rhs) {
    if (!lhs.getPredicate().equals(rhs.getPredicate()) || lhs.getTerms().size() != rhs.getTerms().size()) {
      return false;
    }
    return true;
  }
}
