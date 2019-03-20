package cn.edu.nju.ws.geoinfer.data.program;

import java.util.Objects;

public class TwinPredicate extends Predicate {
  private Predicate innerPredicate;

  public TwinPredicate(Predicate innerPredicate) {
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
    TwinPredicate that = (TwinPredicate) o;
    return Objects.equals(innerPredicate, that.innerPredicate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(innerPredicate);
  }

  @Override
  public String getFullName() {
    return innerPredicate.getFullName() + "_twin";
  }
}
