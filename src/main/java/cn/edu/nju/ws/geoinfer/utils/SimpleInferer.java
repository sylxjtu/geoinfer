package cn.edu.nju.ws.geoinfer.utils;

import cn.edu.nju.ws.geoinfer.data.program.Program;
import cn.edu.nju.ws.geoinfer.db.SqlDatabaseManager;
import cn.edu.nju.ws.geoinfer.db.SqlDatabaseTable;
import cn.edu.nju.ws.geoinfer.engine.BasicInferEngine;
import cn.edu.nju.ws.geoinfer.engine.InferEngine;
import cn.edu.nju.ws.geoinfer.parser.Parser;
import cn.edu.nju.ws.geoinfer.parser.Visitor;
import cn.edu.nju.ws.geoinfer.solver.SemiNaiveSolver;
import cn.edu.nju.ws.geoinfer.transformer.ExtractFactTransformer;
import cn.edu.nju.ws.geoinfer.transformer.SelfRecursionAvoidTransformer;
import cn.edu.nju.ws.geoinfer.transformer.Transformer;
import cn.edu.nju.ws.geoinfer.transformer.TransformerCombinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * All-in-one inference engine
 */
public class SimpleInferer {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleInferer.class);

  private SimpleInferer() {}

  /**
   * Do all-in-one infer, requires db initialization
   *
   * @param programStr the program (rules & goal) in a string
   * @return the inferred result
   */
  public static List<List<String>> infer(String programStr, String tag) {
    long ts = System.nanoTime();

    Program program = (Program) new Visitor().visit(Parser.parse(programStr).logicRules());
    SqlDatabaseManager dbm = new SqlDatabaseManager();
    Transformer transformer =
        TransformerCombinator.combineTransformer(
            new ExtractFactTransformer(dbm),
            // new SipTransformer(dbm),
            // new SupMagicTransformer(),
            new SelfRecursionAvoidTransformer());
    InferEngine engine = new BasicInferEngine(transformer, new SemiNaiveSolver());
    engine.initialize(program);
    SqlDatabaseTable table = engine.solve(dbm);

    long te = System.nanoTime();
    LOG.warn("Inference of {} elapsed {} ms", tag, (te - ts) / 1000000);
    return dbm.getData(table);
  }
}
