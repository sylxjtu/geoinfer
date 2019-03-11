package cn.edu.nju.ws.geoinfer.db;

import cn.edu.nju.ws.geoinfer.data.miscellaneous.TablePointerPair;
import cn.edu.nju.ws.geoinfer.data.rarule.FilterRule;
import cn.edu.nju.ws.geoinfer.data.rarule.JoinRule;
import cn.edu.nju.ws.geoinfer.data.rarule.SelectionRule;

import javax.annotation.Nullable;
import java.util.List;

public interface DatabaseManager<T extends DatabaseTable> {
  T createTable(String tableName, int columnCount, boolean dropExist);

  T getTable(String tableName);

  T insertIntoTable(T table, List<String> row);

  T ensureUnique(T table);

  T filter(T table, List<FilterRule> filterRules);

  T filterWithPointer(T table, List<FilterRule> filterRules, int start, int end);

  T select(T table, List<SelectionRule> selectionRules);

  List<List<String>> getData(T table);

  T putData(List<List<String>> data, int arity, @Nullable String tableName);

  T union(T unionTo, T unionFrom);

  T join(T leftTable, T rightTable, List<JoinRule> joinRules);

  TablePointerPair getTablePointer(T table);

  int getTableTailPointer(T table);

  void setTablePointer(T table, TablePointerPair newTablePointer);

  void initializeTablePointer();
}
