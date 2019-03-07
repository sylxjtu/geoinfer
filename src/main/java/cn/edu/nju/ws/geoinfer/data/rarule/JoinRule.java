package cn.edu.nju.ws.geoinfer.data.rarule;

import java.util.Objects;

public class JoinRule {
  private int leftIndex;
  private int rightIndex;

  public JoinRule(int leftIndex, int rightIndex) {
    this.leftIndex = leftIndex;
    this.rightIndex = rightIndex;
  }

  public int getLeftIndex() {
    return leftIndex;
  }

  public void setLeftIndex(int leftIndex) {
    this.leftIndex = leftIndex;
  }

  public int getRightIndex() {
    return rightIndex;
  }

  public void setRightIndex(int rightIndex) {
    this.rightIndex = rightIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JoinRule joinRule = (JoinRule) o;
    return leftIndex == joinRule.leftIndex &&
        rightIndex == joinRule.rightIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(leftIndex, rightIndex);
  }
}
