package cn.edu.nju.ws.geoinfer.data.rarule;

import java.util.Objects;

public class ConstantSelectionRule extends SelectionRule {
  private String value;

  public ConstantSelectionRule(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ConstantSelectionRule that = (ConstantSelectionRule) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
