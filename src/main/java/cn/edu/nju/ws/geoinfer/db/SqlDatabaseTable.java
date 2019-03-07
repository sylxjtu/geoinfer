package cn.edu.nju.ws.geoinfer.db;

public class SqlDatabaseTable implements DatabaseTable {
  private String name;

  public SqlDatabaseTable(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRef() {
    return "`" + name + "`";
  }
}
