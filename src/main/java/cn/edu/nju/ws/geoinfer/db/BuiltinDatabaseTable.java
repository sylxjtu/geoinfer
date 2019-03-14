package cn.edu.nju.ws.geoinfer.db;

import java.util.List;

public abstract class BuiltinDatabaseTable implements DatabaseTable {
  public abstract List<List<String>> getData();
}
