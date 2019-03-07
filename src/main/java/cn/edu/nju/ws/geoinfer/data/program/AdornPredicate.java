package cn.edu.nju.ws.geoinfer.data.program;

import java.util.Objects;

public class AdornPredicate extends Predicate {
  private Predicate innerPredicate;
  private String adorn;

  public AdornPredicate(Predicate innerPredicate, String adorn) {
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
    AdornPredicate that = (AdornPredicate) o;
    return Objects.equals(innerPredicate, that.innerPredicate) &&
        Objects.equals(adorn, that.adorn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(innerPredicate, adorn);
  }

  @Override
  public String getFullName() {
    return innerPredicate.getFullName() + "_" + adorn;
  }
}
