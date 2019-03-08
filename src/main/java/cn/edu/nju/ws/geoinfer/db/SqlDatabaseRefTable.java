package cn.edu.nju.ws.geoinfer.db;

public class SqlDatabaseRefTable extends SqlDatabaseTable {
  private String name;

  public SqlDatabaseRefTable(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getRef() {
    return "`" + name + "`";
  }

  @Override
  public String getFullRef() {
    return "`" + name + "`";
  }
}
