package cn.edu.nju.ws.geoinfer.data.rarule;

import java.util.Objects;

public class ConstantFilterRule extends FilterRule {
  private int rowId;
  private String value;

  public ConstantFilterRule(int rowId, String value) {
    this.rowId = rowId;
    this.value = value;
  }

  public int getRowId() {
    return rowId;
  }

  public void setRowId(int rowId) {
    this.rowId = rowId;
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
    ConstantFilterRule that = (ConstantFilterRule) o;
    return rowId == that.rowId &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rowId, value);
  }
}
