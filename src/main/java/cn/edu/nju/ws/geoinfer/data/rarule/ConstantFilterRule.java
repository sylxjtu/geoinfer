package cn.edu.nju.ws.geoinfer.data.rarule;

import java.util.Objects;

public class ConstantFilterRule extends FilterRule {
  private int columnId;
  private String value;

  public ConstantFilterRule(int columnId, String value) {
    this.columnId = columnId;
    this.value = value;
  }

  public int getColumnId() {
    return columnId;
  }

  public void setColumnId(int columnId) {
    this.columnId = columnId;
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
    return columnId == that.columnId &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(columnId, value);
  }
}
