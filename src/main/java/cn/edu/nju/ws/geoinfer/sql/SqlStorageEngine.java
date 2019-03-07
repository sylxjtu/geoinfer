package cn.edu.nju.ws.geoinfer.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlStorageEngine {
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
    // System.out.println(sql);

    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    } catch (SQLException cause) {
      throw new IllegalStateException("", cause);
    }
  }

  public void initialize(String jdbcUrl, String username, String password) throws SQLException {
    connection = DriverManager.getConnection(jdbcUrl, username, password);
    connection.setAutoCommit(false);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  for (String tableName : cleanTables) {
                    executeSql("DROP VIEW IF EXISTS `" + tableName + "`;");
                    executeSql("DROP TABLE IF EXISTS `" + tableName + "`;");
                  }
                  try {
                    connection.commit();
                  } catch (SQLException e) {
                    e.printStackTrace();
                  }
                }));
  }

  public void addCleanTable(String tempTableName) {
    cleanTables.add(tempTableName);
  }

  public Connection getConnection() {
    return connection;
  }
}
