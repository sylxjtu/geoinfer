package cn.edu.nju.ws.geoinfer.lowlevelstorage.builtindb;

import cn.edu.nju.ws.geoinfer.data.rarule.ColumnFilterRule;
import cn.edu.nju.ws.geoinfer.data.rarule.ConstantFilterRule;
import cn.edu.nju.ws.geoinfer.data.rarule.FilterRule;
import cn.edu.nju.ws.geoinfer.db.BuiltinDatabaseTable;
import com.google.common.collect.ArrayListMultimap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexedTable extends BuiltinDatabaseTable {
  private List<ArrayListMultimap<String, Integer>> dataIndex;
  // Use sorted map because we need range query
  private Map<byte[], Integer> uniqueIndex;
  private List<List<String>> tableData;

  /**
   * Normal initialization, initialize a empty table
   */
  public IndexedTable(int arity) {
    dataIndex = new ArrayList<>();
    for (int i = 0; i < arity; i++) {
      dataIndex.add(ArrayListMultimap.create());
    }
    // TODO maybe use trie here
    uniqueIndex = new HashMap<>();
    tableData = new ArrayList<List<String>>();
  }

  @Override
  public List<List<String>> getData() {
    return tableData;
  }

  public void insertRow(List<String> rowData) {
    int newId = tableData.size();
    byte[] rowMd5 = md5(rowData);
    if (uniqueIndex.containsKey(rowMd5)) return;

    for (int i = 0; i < rowData.size(); i++) {
      String cell = rowData.get(i);
      dataIndex.get(i).put(cell, newId);
    }
    uniqueIndex.put(rowMd5, newId);
    tableData.add(rowData);
  }

  /**
   * @param filterRules
   * @param idBegin     Inclusive
   * @param idEnd       Inclusive
   * @return
   */
  public SimpleTable filterWithRange(List<FilterRule> filterRules, int idBegin, int idEnd) {
    List<Integer> rowIdSet = null;
    if (filterRules.isEmpty()) return new SimpleTable(this.getData());
    for (FilterRule rule : filterRules) {
      if (rule instanceof ConstantFilterRule) {
        int columnId = ((ConstantFilterRule) rule).getColumnId();
        String value = ((ConstantFilterRule) rule).getValue();
        List<Integer> rowIds = dataIndex.get(columnId).get(value);
        rowIdSet = mergeList(rowIdSet, rowIds);
      } else if (rule instanceof ColumnFilterRule) {
        int columnId = ((ColumnFilterRule) rule).getColumnId();
        int anotherColumnId = ((ColumnFilterRule) rule).getAnotherColumnId();
        rowIdSet = columnFilter(rowIdSet, columnId, anotherColumnId);
      } else {
        throw new IllegalArgumentException();
      }
    }
    List<List<String>> ret = new ArrayList<>();
    for (int rowIndex : rowIdSet) {
      ret.add(tableData.get(rowIndex));
    }
    return new SimpleTable(ret);
  }

  public SimpleTable filter(List<FilterRule> filterRules) {
    return filterWithRange(filterRules, 0, tableData.size());
  }

  private List<Integer> columnFilter(List<Integer> rowIdSet, int columnId, int anotherColumnId) {
    List<Integer> ret = new ArrayList<>();
    if (rowIdSet == null) {
      for (int i = 0; i < tableData.size(); i++) {
        List<String> row = tableData.get(i);
        if (row.get(columnId).equals(row.get(anotherColumnId))) {
          ret.add(i);
        }
      }
    } else {
      for (int rowIndex : rowIdSet) {
        List<String> row = tableData.get(rowIndex);
        if (row.get(columnId).equals(row.get(anotherColumnId))) {
          ret.add(rowIndex);
        }
      }
    }
    return ret;
  }

  private byte[] md5(List<String> rowData) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      return md.digest(String.join("\0", rowData).getBytes());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("", e);
    }
  }

  private List<Integer> mergeList(List<Integer> lhs, List<Integer> rhs) {
    List<Integer> ret;

    if (lhs == null) {
      ret = rhs;
    } else {
      ret = new ArrayList<>();
      for (int i = 0, j = 0; i < lhs.size() && j < rhs.size(); ) {
        int lv = lhs.get(i);
        int rv = rhs.get(j);
        if (lv == rv) {
          ret.add(lv);
          i++;
          j++;
        } else if (lv < rv) {
          i++;
        } else {
          j++;
        }
      }
    }
    return ret;
  }
}
