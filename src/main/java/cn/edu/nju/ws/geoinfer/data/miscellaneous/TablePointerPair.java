package cn.edu.nju.ws.geoinfer.data.miscellaneous;

import java.util.Objects;

public class TablePointerPair {
  private int lastPointer;
  private int currentPointer;

  public TablePointerPair(int lastPointer, int currentPointer) {
    this.lastPointer = lastPointer;
    this.currentPointer = currentPointer;
  }

  public int getLastPointer() {
    return lastPointer;
  }

  public void setLastPointer(int lastPointer) {
    this.lastPointer = lastPointer;
  }

  public int getCurrentPointer() {
    return currentPointer;
  }

  public void setCurrentPointer(int currentPointer) {
    this.currentPointer = currentPointer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TablePointerPair that = (TablePointerPair) o;
    return lastPointer == that.lastPointer &&
        currentPointer == that.currentPointer;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lastPointer, currentPointer);
  }
}
