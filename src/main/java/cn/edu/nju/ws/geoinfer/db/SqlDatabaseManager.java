package cn.edu.nju.ws.geoinfer.db;

import cn.edu.nju.ws.geoinfer.data.miscellaneous.TablePointerPair;
import cn.edu.nju.ws.geoinfer.data.rarule.*;
import cn.edu.nju.ws.geoinfer.sql.SqlStorageEngine;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqlDatabaseManager implements DatabaseManager<SqlDatabaseTable> {
  private static final Logger LOG = LoggerFactory.getLogger(SqlDatabaseManager.class);

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

    if (!dropExist && SqlStorageEngine.getInstance().checkTableExist(tableName))
      return new SqlDatabaseRefTable(tableName);

    sql.append("CREATE TABLE");
    if (!dropExist) {
      sql.append(" IF NOT EXISTS");
    }
    sql.append(" `").append(tableName).append("`");
    sql.append(" ( `id` INT NOT NULL AUTO_INCREMENT, `uniq` TINYINT(1) NOT NULL DEFAULT 1,");
    for (int i = 0; i < columnCount; i++) {
      sql.append(" `_").append(i).append("` VARCHAR(32) NULL,");
    }
    sql.append(" PRIMARY KEY (`id`)) ENGINE = MEMORY;");
    executeSql(sql.toString());
    sql.setLength(0);

    sql.append("ALTER TABLE");
    sql.append(" `").append(tableName).append("`");
    sql.append(" ADD UNIQUE (`uniq`");
    for (int i = 0; i < columnCount; i++) {
      sql.append(", `_").append(i).append("`");
    }
    sql.append(");");
    executeSql(sql.toString());

    return new SqlDatabaseRefTable(tableName);
  }

  @Override
  public SqlDatabaseTable getTable(String tableName) {
    return new SqlDatabaseRefTable(tableName);
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
    StringBuilder sql = new StringBuilder();
    sql.append("INSERT IGNORE INTO");
    sql.append(" ").append(table.getFullRef());
    sql.append(" (`id`");
    for (int i = 0; i < row.size(); i++) {
      sql.append(", `_").append(i).append("`");
    }
    sql.append(") VALUES (NULL");
    for (String s : row) {
      sql.append(", '").append(s).append("'");
    }
    sql.append(");");

    executeSql(sql.toString());
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

    return new SqlDatabaseQueryTable(sql.toString());
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

    return new SqlDatabaseQueryTable(sql.toString());
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
      sql.append("SELECT `id` FROM ").append(table.getFullRef());
    } else {
      sql.append("SELECT `id`");
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
    return new SqlDatabaseQueryTable(sql.toString());
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
  public SqlDatabaseTable putData(List<List<String>> data, int arity) {
    String tableName = generateTempTableName();

    SqlDatabaseTable table = createTable(tableName, arity, true);
    for (List<String> row : data) {
      insertIntoTable(table, row);
    }
    return table;
  }

  @Override
  public SqlDatabaseTable union(SqlDatabaseTable unionTo, SqlDatabaseTable unionFrom) {
    int columnCount = getTableColumnCount(unionTo);

    StringBuilder sql = new StringBuilder();
    sql.append("INSERT IGNORE INTO");
    sql.append(" ").append(unionTo.getFullRef());
    sql.append(" (`id`");
    for (int i = 0; i < columnCount; i++) {
      sql.append(", `_").append(i).append("`");
    }
    sql.append(")");
    sql.append(" SELECT NULL");
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
    sql.append("SELECT '0' AS `id`");

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
    return new SqlDatabaseQueryTable(sql.toString());
  }

  @Override
  public TablePointerPair getTablePointer(SqlDatabaseTable table) {
    if (!(table instanceof SqlDatabaseRefTable)) {
      throw new IllegalArgumentException();
    }
    String tableName = ((SqlDatabaseRefTable) table).getName();

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
    if (!(table instanceof SqlDatabaseRefTable)) {
      throw new IllegalArgumentException();
    }
    String tableName = ((SqlDatabaseRefTable) table).getName();
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

  @Override
  public void setTablePointer(SqlDatabaseTable table, TablePointerPair newTablePointer) {
    if (!(table instanceof SqlDatabaseRefTable)) {
      throw new IllegalArgumentException();
    }
    String tableName = ((SqlDatabaseRefTable) table).getName();

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
    Connection connection = SqlStorageEngine.getInstance().getConnection();

    try (ResultSet resultSet =
             connection
                 .createStatement()
                 .executeQuery("SELECT * FROM " + table.getFullRef() + " LIMIT 0")) {
      int allColumnCount = resultSet.getMetaData().getColumnCount();
      return NumberUtils.toInt(
          resultSet.getMetaData().getColumnName(allColumnCount).substring(1), -1)
          + 1;
    } catch (SQLException cause) {
      throw new IllegalStateException("", cause);
    }
  }

  private void executeSql(String sql) {
    SqlStorageEngine.getInstance().executeSql(sql);
  }

  private String generateTempTableName() {
    String tempTableName = UUID.randomUUID().toString();
    SqlStorageEngine.getInstance().addCleanTable(tempTableName);
    return tempTableName;
  }
}
