package cn.edu.nju.ws.geoinfer.utils;

import cn.edu.nju.ws.geoinfer.data.program.Program;
import cn.edu.nju.ws.geoinfer.db.SqlDatabaseManager;
import cn.edu.nju.ws.geoinfer.db.SqlDatabaseTable;
import cn.edu.nju.ws.geoinfer.engine.BasicInferEngine;
import cn.edu.nju.ws.geoinfer.engine.InferEngine;
import cn.edu.nju.ws.geoinfer.parser.Parser;
import cn.edu.nju.ws.geoinfer.parser.Visitor;
import cn.edu.nju.ws.geoinfer.solver.SemiNaiveSolver;
import cn.edu.nju.ws.geoinfer.transformer.SipTransformer;
import cn.edu.nju.ws.geoinfer.transformer.SupMagicTransformer;
import cn.edu.nju.ws.geoinfer.transformer.Transformer;
import cn.edu.nju.ws.geoinfer.transformer.TransformerCombinator;

import java.util.List;

/**
 * All-in-one inference engine
 */
public class SimpleInferer {
  private SimpleInferer() {
  }

  /**
   * Do all-in-one infer, requires db initialization
   *
   * @param programStr the program (rules & goal) in a string
   * @return the inferred result
   */
  public static List<List<String>> infer(String programStr) {
    Program program = (Program) new Visitor().visit(Parser.parse(programStr).logicRules());
    Transformer transformer =
        TransformerCombinator.combineTransformer(new SipTransformer(), new SupMagicTransformer());
    InferEngine engine = new BasicInferEngine(transformer, new SemiNaiveSolver());
    engine.initialize(program);
    SqlDatabaseManager dbm = new SqlDatabaseManager();
    dbm.initializeTablePointer();
    SqlDatabaseTable table = engine.solve(dbm);
    return dbm.getData(table);
  }
}
