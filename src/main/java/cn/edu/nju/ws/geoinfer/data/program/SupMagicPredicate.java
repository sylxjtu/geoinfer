package cn.edu.nju.ws.geoinfer.data.program;

import java.util.Objects;

public class SupMagicPredicate extends Predicate {
  private Predicate innerPredicate;
  private int ruleIndex;
  private int atomIndex;

  public SupMagicPredicate(Predicate innerPredicate, int ruleIndex, int atomIndex) {
    this.innerPredicate = innerPredicate;
    this.ruleIndex = ruleIndex;
    this.atomIndex = atomIndex;
  }

  public Predicate getInnerPredicate() {
    return innerPredicate;
  }

  public void setInnerPredicate(Predicate innerPredicate) {
    this.innerPredicate = innerPredicate;
  }

  public int getRuleIndex() {
    return ruleIndex;
  }

  public void setRuleIndex(int ruleIndex) {
    this.ruleIndex = ruleIndex;
  }

  public int getAtomIndex() {
    return atomIndex;
  }

  public void setAtomIndex(int atomIndex) {
    this.atomIndex = atomIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SupMagicPredicate that = (SupMagicPredicate) o;
    return ruleIndex == that.ruleIndex &&
        atomIndex == that.atomIndex &&
        Objects.equals(innerPredicate, that.innerPredicate);
  }

  @Override
  public String getFullName() {
    return "supmagic_" + innerPredicate.getFullName() + "_rule_" + ruleIndex + "_atom_" + atomIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(innerPredicate, ruleIndex, atomIndex);
  }
}
