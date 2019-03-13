package cn.edu.nju.ws.geoinfer.data.program;

import java.util.Objects;

public class BuiltinPredicate extends Predicate {
  private String name;

  public BuiltinPredicate(String name) {
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
    BuiltinPredicate that = (BuiltinPredicate) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String getFullName() {
    return "@" + name;
  }

  /**
   * Builtin predicate has no table name
   *
   * @return null
   */
  @Override
  public String getTableName() {
    throw new UnsupportedOperationException();
  }
}
