package cn.edu.nju.ws.geoinfer.sql;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlStorageEngine {
  private static final Logger LOG = LoggerFactory.getLogger(SqlStorageEngine.class);

  private static SqlStorageEngine ourInstance = new SqlStorageEngine();
  private Connection connection;

  private SqlStorageEngine() {
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

  public void initialize(
      String jdbcUrl, String username, String password) {
    try {
      if (connection != null) {
        connection.close();
      }
      connection = DriverManager.getConnection(jdbcUrl, username, password);
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new IllegalStateException("", e);
    }

    executeSql("set names utf8");
    executeSql("set character set utf8");
    executeSql("set character_set_connection=utf8");
  }

  public void bootstrap() {
    // Temporarily do nothing
  }

  public Connection getConnection() {
    return connection;
  }
}
