package cn.edu.nju.ws.geoinfer.data.program;

import java.util.Objects;

public class MagicPredicate extends Predicate {
  private Predicate innerPredicate;
  private String adorn;

  public MagicPredicate(Predicate innerPredicate, String adorn) {
    this.innerPredicate = innerPredicate;
    this.adorn = adorn;
  }

  public Predicate getInnerPredicate() {
    return innerPredicate;
  }

  public void setInnerPredicate(Predicate innerPredicate) {
    this.innerPredicate = innerPredicate;
  }

  public String getAdorn() {
    return adorn;
  }

  public void setAdorn(String adorn) {
    this.adorn = adorn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MagicPredicate that = (MagicPredicate) o;
    return Objects.equals(innerPredicate, that.innerPredicate) &&
        Objects.equals(adorn, that.adorn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(innerPredicate, adorn);
  }

  @Override
  public String getFullName() {
    return "magic_" + innerPredicate.getFullName() + "_" + adorn;
  }
}
