package cn.edu.nju.ws.geoinfer.db;

import cn.edu.nju.ws.geoinfer.data.rarule.*;
import cn.edu.nju.ws.geoinfer.lowlevelstorage.builtindb.IndexedTable;
import cn.edu.nju.ws.geoinfer.lowlevelstorage.builtindb.SimpleTable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BuiltinDatabaseManager implements DatabaseManager<BuiltinDatabaseTable> {
  private Map<String, BuiltinDatabaseTable> tables;

  public BuiltinDatabaseManager() {
    tables = new HashMap<>();
  }

  @Override
  public BuiltinDatabaseTable createTable(String tableName, int columnCount, boolean dropExist) {
    if (dropExist || !tables.containsKey(tableName)) {
      tables.put(tableName, new IndexedTable(columnCount));
    }
    return tables.get(tableName);
  }

  @Override
  public BuiltinDatabaseTable getTableWithProvidedArity(String tableName, int arity) {
    return tables.get(tableName);
  }

  @Override
  public BuiltinDatabaseTable insertIntoTable(BuiltinDatabaseTable table, List<String> row) {
    if (!(table instanceof IndexedTable)) throw new IllegalArgumentException();
    ((IndexedTable) table).insertRow(row);
    return table;
  }

  @Override
  public BuiltinDatabaseTable ensureUnique(BuiltinDatabaseTable table) {
    // Do nothing because we have index
    return table;
  }

  @Override
  public BuiltinDatabaseTable filter(BuiltinDatabaseTable table, List<FilterRule> filterRules) {
    if (!(table instanceof IndexedTable)) {
      throw new IllegalArgumentException();
    }
    return ((IndexedTable) table).filter(filterRules);
  }

  @Override
  public BuiltinDatabaseTable filterWithPointer(
      BuiltinDatabaseTable table, List<FilterRule> filterRules, int start, int end) {
    if (!(table instanceof IndexedTable)) throw new IllegalArgumentException();
    return ((IndexedTable) table).filterWithRange(filterRules, start, end);
  }

  @Override
  public BuiltinDatabaseTable select(
      BuiltinDatabaseTable table, List<SelectionRule> selectionRules) {
    return new SimpleTable(doSelect(table.getData(), selectionRules));
  }

  @Override
  public List<List<String>> getData(BuiltinDatabaseTable table) {
    return table.getData();
  }

  @Override
  public BuiltinDatabaseTable putData(
      List<List<String>> data, int arity, @Nullable String tableName) {
    IndexedTable newTable = new IndexedTable(arity);
    for (List<String> row : data) {
      newTable.insertRow(row);
    }
    if (tableName != null) {
      tables.put(tableName, newTable);
    }
    return newTable;
  }

  @Override
  public BuiltinDatabaseTable union(BuiltinDatabaseTable unionTo, BuiltinDatabaseTable unionFrom) {
    if (!(unionTo instanceof IndexedTable)) throw new IllegalArgumentException();

    for (List<String> row : unionFrom.getData()) {
      ((IndexedTable) unionTo).insertRow(row);
    }

    return unionTo;
  }

  @Override
  public BuiltinDatabaseTable join(
      BuiltinDatabaseTable leftTable, BuiltinDatabaseTable rightTable, List<JoinRule> joinRules) {
    return new SimpleTable(doJoin(leftTable.getData(), rightTable.getData(), joinRules));
  }

  @Override
  public int getTableTailPointer(BuiltinDatabaseTable table) {
    return table.getData().size();
  }

  @Override
  public int getTableSize(BuiltinDatabaseTable table) {
    return table.getData().size();
  }

  @Override
  public int getTableSize(String tableName) {
    return getTableSize(tables.get(tableName));
  }

  private List<List<String>> doSelect(
      List<List<String>> input, List<SelectionRule> selectionRules) {
    return input.stream()
        .map(
            row ->
                (selectionRules.stream()
                    .map(
                        field -> {
                          if (field instanceof ConstantSelectionRule)
                            return ((ConstantSelectionRule) field).getValue();
                          else if (field instanceof NullSelectionRule) return null;
                          else if (field instanceof VariableSelectionRule)
                            return row.get(((VariableSelectionRule) field).getVariableIndex());
                          else throw new IllegalArgumentException();
                        })
                    .collect(Collectors.toList())))
        .collect(Collectors.toList());
  }

  private List<List<String>> doJoin(
      List<List<String>> leftTable, List<List<String>> rightTable, List<JoinRule> joinRules) {
    return leftTable.stream()
        .flatMap(
            leftRow ->
                rightTable.stream()
                    .filter(
                        rightRow ->
                            joinRules.stream()
                                .allMatch(
                                    joinRule ->
                                        leftRow
                                            .get(joinRule.getLeftIndex())
                                            .equals(rightRow.get(joinRule.getRightIndex())))).map(rightRow -> concat(leftRow, rightRow)
                )
        )
        .collect(Collectors.toList());
  }

  @SafeVarargs
  private final <T> List<T> concat(List<T>... args) {
    List<T> ret = new ArrayList<>();
    for (List<T> arg : args) {
      ret.addAll(arg);
    }
    return ret;
  }
}
