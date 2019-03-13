package cn.edu.nju.ws.geoinfer.db;

import cn.edu.nju.ws.geoinfer.data.miscellaneous.TablePointerPair;
import cn.edu.nju.ws.geoinfer.data.rarule.*;
import cn.edu.nju.ws.geoinfer.sql.SqlStorageEngine;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SqlDatabaseManager implements DatabaseManager<SqlDatabaseTable> {
  private static final Logger LOG = LoggerFactory.getLogger(SqlDatabaseManager.class);
  private static final int BATCH_SIZE = 1024;

  /**
   * Create sql table
   *
   * <p>Example SQL
   *
   * <p>DROP TABLE IF EXISTS `magic_haha_b`;
   *
   * <p>CREATE TABLE `magic_haha_b` ( `id` INT NOT NULL AUTO_INCREMENT, `_0` VARCHAR(32) NULL, `_1`
   * VARCHAR(32) NULL, `_2` VARCHAR(32) NULL, `_3` VARCHAR(32) NULL, `_4` VARCHAR(32) NULL, PRIMARY
   * KEY (`id`)) ENGINE = InnoDB;
   *
   * <p>ALTER TABLE `magic_haha_b` ADD UNIQUE( `_0`, `_1`, `_2`, `_3`, `_4` );
   *
   * @param tableName
   * @param columnCount
   * @param dropExist
   * @return
   */
  @Override
  public SqlDatabaseTable createTable(String tableName, int columnCount, boolean dropExist) {
    StringBuilder sql = new StringBuilder();
    if (dropExist) {
      sql.append("DROP TABLE IF EXISTS `").append(tableName).append("`;");
      executeSql(sql.toString());
      sql.setLength(0);
    }

    // Ignored check because only one statement is executed
    // if (!dropExist && SqlStorageEngine.getInstance().checkTableExist(tableName))

    sql.append("CREATE TABLE");
    if (!dropExist) {
      sql.append(" IF NOT EXISTS");
    }
    sql.append(" `").append(tableName).append("`");
    sql.append(" ( `id` INT NOT NULL AUTO_INCREMENT, `uniq` CHAR(32) NOT NULL UNIQUE,");
    for (int i = 0; i < columnCount; i++) {
      sql.append(" `_").append(i).append("` VARCHAR(32) NULL,");
      sql.append("INDEX (`_").append(i).append("`),");
    }
    sql.append(" PRIMARY KEY (`id`)) ENGINE = MEMORY;");
    executeSql(sql.toString());

    // Ignore this, we use create clause to create index
    // createIndexes(tableName, columnCount);

    return new SqlDatabaseRefTable(tableName, columnCount);
  }

  @Override
  public SqlDatabaseTable getTableWithProvidedArity(String tableName, int arity) {
    return new SqlDatabaseRefTable(tableName, arity);
  }

  /**
   * Insert a row into table
   *
   * <p>Example SQL
   *
   * <p>INSERT IGNORE INTO `magic_haha_b` (`id`, `_0`, `_1`, `_2`, `_3`, `_4`) VALUES (NULL,
   * 'adasdsa', 'sdas', 'dasds', 'asdas', 'asdas');
   *
   * @param table
   * @param row
   * @return
   */
  @Override
  public SqlDatabaseTable insertIntoTable(SqlDatabaseTable table, List<String> row) {
    executeSql(getInsertSql(table, Collections.singletonList(row)));
    return table;
  }

  @Override
  public SqlDatabaseTable ensureUnique(SqlDatabaseTable table) {
    // SQL uniqueness is always ensured
    return table;
  }

  /**
   * Filter rows from table
   *
   * <p>Example SQL
   *
   * <p>CREATE VIEW `025a33bc-0b02-417b-bbc4-6f937ae53dc6` AS SELECT * FROM `magic_haha_b` WHERE
   * `_1`=`_2` AND `_3`='haha';
   *
   * @param table
   * @param filterRules
   * @return
   */
  @Override
  public SqlDatabaseTable filter(SqlDatabaseTable table, List<FilterRule> filterRules) {
    if (filterRules.isEmpty()) return table;

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT * FROM ").append(table.getFullRef());
    sql.append(" WHERE");
    for (int i = 0; i < filterRules.size(); i++) {
      FilterRule filterRule = filterRules.get(i);
      if (filterRule instanceof ColumnFilterRule) {
        int lhs = ((ColumnFilterRule) filterRule).getRowId();
        int rhs = ((ColumnFilterRule) filterRule).getAnotherRowId();
        sql.append(" `_").append(lhs).append("`=`_").append(rhs).append("`");
      } else if (filterRule instanceof ConstantFilterRule) {
        int rowId = ((ConstantFilterRule) filterRule).getRowId();
        String value = ((ConstantFilterRule) filterRule).getValue();
        sql.append(" `_").append(rowId).append("`='").append(value).append("'");
      } else {
        throw new IllegalArgumentException();
      }
      if (i != filterRules.size() - 1) {
        sql.append(" AND");
      }
    }

    return new SqlDatabaseQueryTable(sql.toString(), table.getArity());
  }

  @Override
  public SqlDatabaseTable filterWithPointer(
      SqlDatabaseTable table, List<FilterRule> filterRules, int start, int end) {
    if (filterRules.isEmpty()) return table;

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT * FROM ").append(table.getFullRef());
    sql.append(" WHERE");
    for (FilterRule rule : filterRules) {
      if (rule instanceof ColumnFilterRule) {
        int lhs = ((ColumnFilterRule) rule).getRowId();
        int rhs = ((ColumnFilterRule) rule).getAnotherRowId();
        sql.append(" `_").append(lhs).append("`=`_").append(rhs).append("`");
      } else if (rule instanceof ConstantFilterRule) {
        int rowId = ((ConstantFilterRule) rule).getRowId();
        String value = ((ConstantFilterRule) rule).getValue();
        sql.append(" `_").append(rowId).append("`='").append(value).append("'");
      } else {
        throw new IllegalArgumentException();
      }
      sql.append(" AND");
    }
    sql.append(" `id` BETWEEN ").append(start).append(" AND ").append(end);
    executeSql(sql.toString());

    return new SqlDatabaseQueryTable(sql.toString(), table.getArity());
  }

  /**
   * Select columns from a table
   *
   * <p>Example SQL
   *
   * <p>CREATE VIEW `9b0666ae-406e-4967-80a1-09d9397298fd` AS SELECT `id`, `_0` AS `_0`, 'haha' AS
   * `_1`, NULL AS `_2` FROM `magic_haha_b`;
   *
   * @param table
   * @param selectionRules
   * @return
   */
  @Override
  public SqlDatabaseTable select(SqlDatabaseTable table, List<SelectionRule> selectionRules) {
    StringBuilder sql = new StringBuilder();
    if (selectionRules.isEmpty()) {
      sql.append("SELECT 0 AS `id`");
    } else {
      sql.append("SELECT 0 AS `id`");
      for (int i = 0; i < selectionRules.size(); i++) {
        sql.append(", ");
        SelectionRule rule = selectionRules.get(i);
        if (rule instanceof NullSelectionRule) {
          sql.append("NULL");
        } else if (rule instanceof ConstantSelectionRule) {
          sql.append("'").append(((ConstantSelectionRule) rule).getValue()).append("'");
        } else if (rule instanceof VariableSelectionRule) {
          sql.append("`_").append(((VariableSelectionRule) rule).getVariableIndex()).append("`");
        }
        sql.append(" AS `_").append(i).append("`");
      }
      sql.append(" FROM ").append(table.getFullRef());
    }
    return new SqlDatabaseQueryTable(sql.toString(), selectionRules.size());
  }

  @Override
  public List<List<String>> getData(SqlDatabaseTable table) {
    LOG.debug("Get data from table {}", table.getFullRef());
    List<List<String>> ret = new ArrayList<>();

    Connection connection = SqlStorageEngine.getInstance().getConnection();

    try (ResultSet resultSet =
             connection.createStatement().executeQuery("SELECT * FROM " + table.getFullRef() + ";")) {
      int dataColumnCount = getTableColumnCount(table);
      while (resultSet.next()) {
        List<String> row = new ArrayList<>();
        for (int i = 0; i < dataColumnCount; i++) {
          row.add(resultSet.getString("_" + i));
        }
        ret.add(row);
      }
    } catch (SQLException cause) {
      throw new IllegalStateException("", cause);
    }

    LOG.debug("Got {} rows", ret.size());
    return ret;
  }

  @Override
  public SqlDatabaseTable putData(List<List<String>> data, int arity, String tableName) {
    if (tableName == null) tableName = generateTempTableName();

    SqlDatabaseTable table = createTable(tableName, arity, true);
    for (List<List<String>> partition : Lists.partition(data, BATCH_SIZE)) {
      executeSql(getInsertSql(table, partition));
    }

    return table;
  }

  @Override
  public SqlDatabaseTable union(SqlDatabaseTable unionTo, SqlDatabaseTable unionFrom) {
    int columnCount = getTableColumnCount(unionTo);

    StringBuilder sql = new StringBuilder();
    sql.append("INSERT IGNORE INTO");
    sql.append(" ").append(unionTo.getFullRef());
    sql.append(" (`id`, `uniq`");
    for (int i = 0; i < columnCount; i++) {
      sql.append(", `_").append(i).append("`");
    }
    sql.append(")");
    sql.append(" SELECT NULL, ").append(getRowMd5Sql(columnCount));
    for (int i = 0; i < columnCount; i++) {
      sql.append(", `_").append(i).append("`");
    }
    sql.append(" FROM ").append(unionFrom.getFullRef()).append(";");
    executeSql(sql.toString());
    return unionTo;
  }

  @Override
  public SqlDatabaseTable join(
      SqlDatabaseTable leftTable, SqlDatabaseTable rightTable, List<JoinRule> joinRules) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT 0 AS `id`");

    int leftColumnCount = getTableColumnCount(leftTable);
    int rightColumnCount = getTableColumnCount(rightTable);
    for (int i = 0; i < leftColumnCount; i++) {
      sql.append(", `left`.`_").append(i).append("` AS `_").append(i).append("`");
    }
    for (int i = 0; i < rightColumnCount; i++) {
      sql.append(", `right`.`_")
          .append(i)
          .append("` AS `_")
          .append(leftColumnCount + i)
          .append("`");
    }
    sql.append(" FROM ")
        .append(leftTable.getRef())
        .append(" AS `left`, ")
        .append(rightTable.getRef())
        .append(" AS `right`");
    if (!joinRules.isEmpty()) {
      sql.append(" WHERE");
      for (int i = 0; i < joinRules.size(); i++) {
        JoinRule rule = joinRules.get(i);
        int leftIndex = rule.getLeftIndex();
        int rightIndex = rule.getRightIndex();
        sql.append(" `left`.`_")
            .append(leftIndex)
            .append("`=`right`.`_")
            .append(rightIndex)
            .append("`");
        if (i != joinRules.size() - 1) sql.append(" AND");
      }
    }
    return new SqlDatabaseQueryTable(sql.toString(), leftColumnCount + rightColumnCount);
  }

  @Override
  public TablePointerPair getTablePointer(SqlDatabaseTable table) {
    String tableName = getRefTableName(table);

    Connection connection = SqlStorageEngine.getInstance().getConnection();

    try (ResultSet resultSet =
             connection
                 .createStatement()
                 .executeQuery(
                     "SELECT last, current FROM `_table_pointer` WHERE `table_name`='"
                         + tableName
                         + "';")) {
      // Has any result
      if (!resultSet.next()) {
        return new TablePointerPair(0, 0);
      } else {
        return new TablePointerPair(resultSet.getInt("last"), resultSet.getInt("current"));
      }
    } catch (SQLException cause) {
      throw new IllegalStateException("", cause);
    }
  }

  @Override
  public int getTableTailPointer(SqlDatabaseTable table) {
    String tableName = getRefTableName(table);
    Connection connection = SqlStorageEngine.getInstance().getConnection();
    String sql = "SELECT MAX(id) AS tail FROM `" + tableName + "`";
    LOG.debug(sql);

    try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
      if (!resultSet.next()) return 0;
      return resultSet.getInt("tail");
    } catch (SQLException cause) {
      throw new IllegalStateException("", cause);
    }
  }

  private String getRefTableName(SqlDatabaseTable table) {
    if (!(table instanceof SqlDatabaseRefTable)) {
      throw new IllegalArgumentException();
    }
    return ((SqlDatabaseRefTable) table).getName();
  }

  @Override
  public int getTableSize(SqlDatabaseTable table) {
    String tableName = getRefTableName(table);
    return getTableSize(tableName);
  }

  @Override
  public int getTableSize(String tableName) {
    Connection connection = SqlStorageEngine.getInstance().getConnection();
    String sql = "SELECT COUNT(id) AS size FROM `" + tableName + "`";
    LOG.debug(sql);

    try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
      if (!resultSet.next()) return 0;
      return resultSet.getInt("size");
    } catch (SQLException cause) {
      throw new IllegalStateException("", cause);
    }
  }

  @Override
  public void setTablePointer(SqlDatabaseTable table, TablePointerPair newTablePointer) {
    String tableName = getRefTableName(table);

    int last = newTablePointer.getLastPointer();
    int current = newTablePointer.getCurrentPointer();
    String sql =
        "INSERT INTO `_table_pointer` (`table_name`, `last`, `current`) VALUES ('"
            + tableName
            + "', "
            + last
            + ", "
            + current
            + ") ON DUPLICATE KEY UPDATE last="
            + last
            + ", current="
            + current
            + ";";
    executeSql(sql);
  }

  @Override
  public void initializeTablePointer() {
    executeSql("TRUNCATE `_table_pointer`");
  }

  private int getTableColumnCount(SqlDatabaseTable table) {
    return table.getArity();
//    Connection connection = SqlStorageEngine.getInstance().getConnection();
//
//    try (ResultSet resultSet =
//        connection
//            .createStatement()
//            .executeQuery("SELECT * FROM " + table.getFullRef() + " LIMIT 0")) {
//      int allColumnCount = resultSet.getMetaData().getColumnCount();
//      return NumberUtils.toInt(
//              resultSet.getMetaData().getColumnName(allColumnCount).substring(1), -1)
//          + 1;
//    } catch (SQLException cause) {
//      throw new IllegalStateException("", cause);
//    }
  }

  private void executeSql(String sql) {
    SqlStorageEngine.getInstance().executeSql(sql);
  }

  private void createIndexes(String tableName, int columnCount) {
    for (int i = 0; i < columnCount; i++) {
      executeSql("ALTER TABLE `" + tableName + "` ADD INDEX (`_" + i + "`);");
    }
  }

  private String getRowMd5Sql(int size) {
    StringBuilder query = new StringBuilder();
    query.append("MD5(CONCAT(''");
    for (int i = 0; i < size; i++) {
      query.append(", `_").append(i).append("`");
    }
    query.append("))");
    return query.toString();
  }

  private String getRowMd5Sql(List<String> row) {
    StringBuilder query = new StringBuilder();
    query.append("MD5(CONCAT(''");
    for (String cell : row) {
      query.append(", '").append(cell).append("'");
    }
    query.append("))");
    return query.toString();
  }

  private String generateTempTableName() {
    String tempTableName = UUID.randomUUID().toString();
    SqlStorageEngine.getInstance().addCleanTable(tempTableName);
    return tempTableName;
  }

  private String getInsertSql(SqlDatabaseTable table, List<List<String>> rows) {
    if (rows.isEmpty()) {
      throw new IllegalArgumentException();
    }
    StringBuilder sql = new StringBuilder();
    sql.append("INSERT IGNORE INTO");
    sql.append(" ").append(table.getFullRef());
    sql.append(" (`id`, `uniq`");
    for (int i = 0; i < rows.get(0).size(); i++) {
      sql.append(", `_").append(i).append("`");
    }
    sql.append(") VALUES");
    for (List<String> row : rows) {
      sql.append(" (NULL, ").append(getRowMd5Sql(row));
      for (String s : row) {
        sql.append(", '").append(s).append("'");
      }
      sql.append("),");
    }
    sql.setLength(sql.length() - 1);
    return sql.toString();
  }
}
