package cn.edu.nju.ws.geoinfer.db;

public abstract class SqlDatabaseTable implements DatabaseTable {
  public abstract String getRef();

  public abstract String getFullRef();

  public abstract int getArity();
}
