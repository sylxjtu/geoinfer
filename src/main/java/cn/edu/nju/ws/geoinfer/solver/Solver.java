package cn.edu.nju.ws.geoinfer.solver;

import cn.edu.nju.ws.geoinfer.data.program.Program;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.db.DatabaseTable;

public interface Solver {
  <T extends DatabaseTable> T solve(Program program, DatabaseManager<T> dbm);
}
