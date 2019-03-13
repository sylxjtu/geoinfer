package cn.edu.nju.ws.geoinfer.seminaive;

import cn.edu.nju.ws.geoinfer.data.miscellaneous.TablePointerPair;

import java.util.HashMap;
import java.util.Map;

public class TablePointerRegistry {
  private static final TablePointerRegistry instance = new TablePointerRegistry();
  private Map<String, TablePointerPair> tablePointerMap;

  private TablePointerRegistry() {
    tablePointerMap = new HashMap<>();
  }

  public static TablePointerRegistry getInstance() {
    return instance;
  }

  public void initialize() {
    tablePointerMap.clear();
  }

  public void setTablePointer(String tableName, TablePointerPair newTablePointer) {
    tablePointerMap.put(tableName, newTablePointer);
  }

  public TablePointerPair getTablePointer(String tableName) {
    return tablePointerMap.getOrDefault(tableName, new TablePointerPair(0, 0));
  }
}
