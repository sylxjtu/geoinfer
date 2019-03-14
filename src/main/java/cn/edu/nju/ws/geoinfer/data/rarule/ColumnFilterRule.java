package cn.edu.nju.ws.geoinfer.data.rarule;

import java.util.Objects;

public class ColumnFilterRule extends FilterRule {
  private int columnId;
  private int anotherColumnId;

  public ColumnFilterRule(int columnId, int anotherColumnId) {
    this.columnId = columnId;
    this.anotherColumnId = anotherColumnId;
  }

  public int getColumnId() {
    return columnId;
  }

  public void setColumnId(int columnId) {
    this.columnId = columnId;
  }

  public int getAnotherColumnId() {
    return anotherColumnId;
  }

  public void setAnotherColumnId(int anotherColumnId) {
    this.anotherColumnId = anotherColumnId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ColumnFilterRule that = (ColumnFilterRule) o;
    return columnId == that.columnId && anotherColumnId == that.anotherColumnId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(columnId, anotherColumnId);
  }
}
