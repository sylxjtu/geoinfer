package cn.edu.nju.ws.geoinfer.sql;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlStorageEngine {
  private static final Logger LOG = LoggerFactory.getLogger(SqlStorageEngine.class);

  private static SqlStorageEngine ourInstance = new SqlStorageEngine();
  private Connection connection;
  private List<String> cleanTables;

  private SqlStorageEngine() {
    cleanTables = new ArrayList<>();
  }

  public static SqlStorageEngine getInstance() {
    return ourInstance;
  }

  public void executeSql(String sql) {
    LOG.debug(sql);
    long ts = System.nanoTime();

    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    } catch (SQLException cause) {
      throw new IllegalStateException("", cause);
    }
    long te = System.nanoTime();
    long ms = (te - ts) / 1000000;
    if (ms >= 100) {
      LOG.warn("SQL {}, elapsed {} ms", StringUtils.substring(sql, 0, 50), (te - ts) / 1000000);
    }
  }

  public boolean checkTableExist(String tableName) {
    try (Statement statement = connection.createStatement()) {
      statement.execute("SELECT NULL FROM `" + tableName + "` LIMIT 0");
      return true;
    } catch (SQLException cause) {
      return false;
    }
  }

  public void initialize(String jdbcUrl, String username, String password) {
    try {
      connection = DriverManager.getConnection(jdbcUrl, username, password);
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new IllegalStateException("", e);
    }

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  for (String tableName : cleanTables) {
                    executeSql("DROP TABLE IF EXISTS `" + tableName + "`;");
                  }
                  try {
                    connection.commit();
                  } catch (SQLException e) {
                    throw new IllegalStateException("", e);
                  }
                }));
  }

  public void bootstrap() {
    executeSql(
        "CREATE TABLE IF NOT EXISTS `_table_pointer` (\n"
            + "  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,\n"
            + "  `table_name` varchar(255) NOT NULL,\n"
            + "  `last` int(11) NOT NULL,\n"
            + "  `current` int(11) NOT NULL\n"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n");
  }

  public void addCleanTable(String tempTableName) {
    cleanTables.add(tempTableName);
  }

  public Connection getConnection() {
    return connection;
  }
}
