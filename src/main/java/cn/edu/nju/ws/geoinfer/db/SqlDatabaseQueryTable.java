package cn.edu.nju.ws.geoinfer.db;

public class SqlDatabaseQueryTable extends SqlDatabaseTable {
  private String query;

  public SqlDatabaseQueryTable(String query) {
    this.query = query;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  public String getRef() {
    return "(" + query + ")";
  }

  @Override
  public String getFullRef() {
    return "(" + query + ") AS T";
  }
}
