package cn.edu.nju.ws.geoinfer.lowlevelstorage.builtindb;

import java.util.List;

public class Row {
  private int id;
  private List<String> data;

  public Row(int id, List<String> data) {
    this.id = id;
    this.data = data;
  }

  public Row(List<String> data) {
    this(-1, data);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<String> getData() {
    return data;
  }

  public void setData(List<String> data) {
    this.data = data;
  }
}
