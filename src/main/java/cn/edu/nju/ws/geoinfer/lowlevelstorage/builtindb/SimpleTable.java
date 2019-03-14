package cn.edu.nju.ws.geoinfer.lowlevelstorage.builtindb;

import cn.edu.nju.ws.geoinfer.db.BuiltinDatabaseTable;

import java.util.List;

public class SimpleTable extends BuiltinDatabaseTable {
  private List<List<String>> data;

  public SimpleTable(List<List<String>> data) {
    this.data = data;
  }

  public List<List<String>> getData() {
    return data;
  }
}
