package cn.edu.nju.ws.geoinfer.data.rarule;

import java.util.Objects;

public class VariableSelectionRule extends SelectionRule {
  private int variableIndex;

  public VariableSelectionRule(int variableIndex) {
    this.variableIndex = variableIndex;
  }

  public int getVariableIndex() {
    return variableIndex;
  }

  public void setVariableIndex(int variableIndex) {
    this.variableIndex = variableIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VariableSelectionRule that = (VariableSelectionRule) o;
    return variableIndex == that.variableIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(variableIndex);
  }
}
