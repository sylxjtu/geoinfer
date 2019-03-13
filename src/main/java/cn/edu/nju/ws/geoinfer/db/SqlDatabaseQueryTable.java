package cn.edu.nju.ws.geoinfer.db;

public class SqlDatabaseQueryTable extends SqlDatabaseTable {
  private String query;
  private int arity;

  public SqlDatabaseQueryTable(String query, int arity) {
    this.query = query;
    this.arity = arity;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  public int getArity() {
    return arity;
  }

  @Override
  public String getRef() {
    return "(" + query + ")";
  }

  @Override
  public String getFullRef() {
    return "(" + query + ") AS T";
  }

  public void setArity(int arity) {
    this.arity = arity;
  }
}
