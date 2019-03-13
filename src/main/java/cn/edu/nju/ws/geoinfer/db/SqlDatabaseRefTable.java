package cn.edu.nju.ws.geoinfer.db;

public class SqlDatabaseRefTable extends SqlDatabaseTable {
  private String name;
  private int arity;

  public SqlDatabaseRefTable(String name, int arity) {
    this.name = name;
    this.arity = arity;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int getArity() {
    return arity;
  }

  @Override
  public String getRef() {
    return "`" + name + "`";
  }

  @Override
  public String getFullRef() {
    return "`" + name + "`";
  }

  public void setArity(int arity) {
    this.arity = arity;
  }
}
