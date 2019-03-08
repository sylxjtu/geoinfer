package cn.edu.nju.ws.geoinfer.data.program;

import java.util.Objects;

public class RawPredicate extends Predicate {
  private String name;

  public RawPredicate(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RawPredicate that = (RawPredicate) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String getFullName() {
    return getName();
  }
}
