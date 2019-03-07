package cn.edu.nju.ws.geoinfer.engine;

import cn.edu.nju.ws.geoinfer.data.program.Program;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.db.DatabaseTable;

public interface InferEngine {
  void initialize(Program program);

  <T extends DatabaseTable> T solve(DatabaseManager<T> dbm);
}
