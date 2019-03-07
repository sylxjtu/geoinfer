package cn.edu.nju.ws.geoinfer.solver;

import cn.edu.nju.ws.geoinfer.data.program.Program;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.db.DatabaseTable;

public class SemiNaiveSolver implements Solver {
  @Override
  public <T extends DatabaseTable> T solve(Program program, DatabaseManager<T> dbm) {
    return new SemiNaiveSolverManager<T>().solve(program, dbm);
  }
}
