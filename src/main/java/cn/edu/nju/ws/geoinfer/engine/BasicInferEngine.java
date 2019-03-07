package cn.edu.nju.ws.geoinfer.engine;

import cn.edu.nju.ws.geoinfer.data.program.BuiltinPredicate;
import cn.edu.nju.ws.geoinfer.data.program.Program;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.db.DatabaseTable;
import cn.edu.nju.ws.geoinfer.solver.Solver;
import cn.edu.nju.ws.geoinfer.transformer.Transformer;

public class BasicInferEngine implements InferEngine {
  private Transformer transformer;
  private Solver solver;
  private Program program;

  public BasicInferEngine(Transformer transformer, Solver solver) {
    this.transformer = transformer;
    this.solver = solver;
  }

  @Override
  public void initialize(Program program) {
    if (!(program.getGoal().getPredicate() instanceof BuiltinPredicate)) {
      this.program = transformer.transform(program);
    } else {
      this.program = program;
    }
  }

  @Override
  public <T extends DatabaseTable> T solve(DatabaseManager<T> dbm) {
    if (!(program.getGoal().getPredicate() instanceof BuiltinPredicate)) {
      return solver.solve(program, dbm);
    } else {
      throw new UnsupportedOperationException("Directly solve builtin predicate is TODO");
    }
  }
}
