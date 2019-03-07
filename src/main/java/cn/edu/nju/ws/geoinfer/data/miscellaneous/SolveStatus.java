package cn.edu.nju.ws.geoinfer.data.miscellaneous;

import cn.edu.nju.ws.geoinfer.db.DatabaseTable;

import java.util.List;
import java.util.Objects;

public class SolveStatus<T extends DatabaseTable> {
  private T table;
  private List<String> varFields;

  public SolveStatus(T table, List<String> varFields) {
    this.table = table;
    this.varFields = varFields;
  }

  public T getTable() {
    return table;
  }

  public void setTable(T table) {
    this.table = table;
  }

  public List<String> getVarFields() {
    return varFields;
  }

  public void setVarFields(List<String> varFields) {
    this.varFields = varFields;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SolveStatus<?> that = (SolveStatus<?>) o;
    return Objects.equals(table, that.table) &&
        Objects.equals(varFields, that.varFields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(table, varFields);
  }
}
