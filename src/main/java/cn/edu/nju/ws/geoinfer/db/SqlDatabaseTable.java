package cn.edu.nju.ws.geoinfer.db;

public abstract class SqlDatabaseTable implements DatabaseTable {
  abstract public String getRef();

  abstract public String getFullRef();
}
