package cn.edu.nju.ws.geoinfer.main;

import cn.edu.nju.ws.geoinfer.data.program.Program;
import cn.edu.nju.ws.geoinfer.db.SqlDatabaseManager;
import cn.edu.nju.ws.geoinfer.db.SqlDatabaseTable;
import cn.edu.nju.ws.geoinfer.engine.BasicInferEngine;
import cn.edu.nju.ws.geoinfer.engine.InferEngine;
import cn.edu.nju.ws.geoinfer.parser.Parser;
import cn.edu.nju.ws.geoinfer.parser.Visitor;
import cn.edu.nju.ws.geoinfer.solver.SemiNaiveSolver;
import cn.edu.nju.ws.geoinfer.sql.SqlStorageEngine;
import cn.edu.nju.ws.geoinfer.transformer.*;
import cn.edu.nju.ws.geoinfer.utils.Initializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    boolean compileOnly = Arrays.asList(args).contains("-compile-only");

    long t1 = System.nanoTime();

    if (!compileOnly) {
      SqlStorageEngine.getInstance()
          .initialize("jdbc:mysql://localhost:3306/geoinfer", "root", "", true);
    }

    String ruleStr = Initializer.getRuleFromFile("rules/geonames_medium_force_sip.txt");
    Initializer.registerBuiltins();

    Program program = (Program) new Visitor().visit(Parser.parse(ruleStr).logicRules());

    LOG.debug("Original program:\n{}", program);

    Transformer transformer = new SipTransformer();
    Program transformed = transformer.transform(program);

    LOG.debug("Transformed 1 program:\n{}", transformed);

    transformer = new SupMagicTransformer();
    transformed = transformer.transform(transformed);

    LOG.debug("Transformed 2 program:\n{}", transformed);

    if (!compileOnly) {
      SqlDatabaseManager dbm = new SqlDatabaseManager();
      transformer =
          TransformerCombinator.combineTransformer(new ExtractFactTransformer(dbm), new SipTransformer(dbm), new SupMagicTransformer());
      InferEngine engine = new BasicInferEngine(transformer, new SemiNaiveSolver());
      engine.initialize(program);
      SqlDatabaseTable table = engine.solve(dbm);
      List<List<String>> data = dbm.getData(table);
      StringBuilder dataStr = new StringBuilder();
      for (List<String> row : data) {
        for (String cell : row) {
          dataStr.append(cell).append(", ");
        }
        dataStr.append("\n");
      }
      LOG.info("Result:\n{}", dataStr);
    }

    long t2 = System.nanoTime();
    LOG.info("Elapsed {} ms", (t2 - t1) / 1000000);
  }
}
