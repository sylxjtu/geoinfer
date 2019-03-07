package cn.edu.nju.ws.geoinfer.data.program;

import java.util.Objects;

public class OldPredicate extends Predicate {
  private Predicate innerPredicate;

  public OldPredicate(Predicate innerPredicate) {
    this.innerPredicate = innerPredicate;
  }

  public Predicate getInnerPredicate() {
    return innerPredicate;
  }

  public void setInnerPredicate(Predicate innerPredicate) {
    this.innerPredicate = innerPredicate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OldPredicate that = (OldPredicate) o;
    return Objects.equals(innerPredicate, that.innerPredicate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(innerPredicate);
  }

  @Override
  public String getFullName() {
    return "old_" + innerPredicate.getFullName();
  }

  /**
   * Old predicate has same table name as inner predicate
   *
   * @return inner predicate table name
   */
  @Override
  public String getTableName() {
    return innerPredicate.getTableName();
  }
}
