package cn.edu.nju.ws.geoinfer.data.rarule;

import java.util.Objects;

public class ColumnFilterRule extends FilterRule {
  private int rowId;
  private int anotherRowId;

  public ColumnFilterRule(int rowId, int anotherRowId) {
    this.rowId = rowId;
    this.anotherRowId = anotherRowId;
  }

  public int getRowId() {
    return rowId;
  }

  public void setRowId(int rowId) {
    this.rowId = rowId;
  }

  public int getAnotherRowId() {
    return anotherRowId;
  }

  public void setAnotherRowId(int anotherRowId) {
    this.anotherRowId = anotherRowId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ColumnFilterRule that = (ColumnFilterRule) o;
    return rowId == that.rowId && anotherRowId == that.anotherRowId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(rowId, anotherRowId);
  }
}
