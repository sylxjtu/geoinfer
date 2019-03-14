package cn.edu.nju.ws.geoinfer.db;

import cn.edu.nju.ws.geoinfer.data.rarule.FilterRule;
import cn.edu.nju.ws.geoinfer.data.rarule.JoinRule;
import cn.edu.nju.ws.geoinfer.data.rarule.SelectionRule;

import javax.annotation.Nullable;
import java.util.List;

public interface DatabaseManager<T extends DatabaseTable> {
  T createTable(String tableName, int columnCount, boolean dropExist);

  T getTableWithProvidedArity(String tableName, int arity);

  T insertIntoTable(T table, List<String> row);

  T ensureUnique(T table);

  T filter(T table, List<FilterRule> filterRules);

  /**
   * Filter with pointer inclusive
   *
   * @param table
   * @param filterRules
   * @param start
   * @param end
   * @return
   */
  T filterWithPointer(T table, List<FilterRule> filterRules, int start, int end);

  // TODO: maybe remove duplicate?
  T select(T table, List<SelectionRule> selectionRules);

  List<List<String>> getData(T table);

  T putData(List<List<String>> data, int arity, @Nullable String tableName);

  T union(T unionTo, T unionFrom);

  T join(T leftTable, T rightTable, List<JoinRule> joinRules);

  int getTableTailPointer(T table);

  int getTableSize(T table);

  int getTableSize(String tableName);
}
