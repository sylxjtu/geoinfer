package cn.edu.nju.ws.geoinfer.main;

import cn.edu.nju.ws.geoinfer.builtin.BuiltinRegistry;
import cn.edu.nju.ws.geoinfer.data.program.Program;
import cn.edu.nju.ws.geoinfer.db.SqlDatabaseManager;
import cn.edu.nju.ws.geoinfer.db.SqlDatabaseTable;
import cn.edu.nju.ws.geoinfer.engine.BasicInferEngine;
import cn.edu.nju.ws.geoinfer.engine.InferEngine;
import cn.edu.nju.ws.geoinfer.parser.Parser;
import cn.edu.nju.ws.geoinfer.parser.Visitor;
import cn.edu.nju.ws.geoinfer.solver.SemiNaiveSolver;
import cn.edu.nju.ws.geoinfer.sql.SqlStorageEngine;
import cn.edu.nju.ws.geoinfer.transformer.SipTransformer;
import cn.edu.nju.ws.geoinfer.transformer.SupMagicTransformer;
import cn.edu.nju.ws.geoinfer.transformer.Transformer;
import cn.edu.nju.ws.geoinfer.transformer.TransformerCombinator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    Long t1 = System.nanoTime();

    try {
      SqlStorageEngine.getInstance().initialize("jdbc:mysql://localhost:3306/geoinfer", "root", "");
      // SqlStorageEngine.getInstance().initialize("jdbc:sqlite:geoinfer.db", "", "");
    } catch (SQLException cause) {
      throw new IllegalStateException("Failed to connect db", cause);
    }

    String ruleStr;
    try {
      ruleStr = FileUtils.readFileToString(new File("rules/rule_1.txt"), StandardCharsets.UTF_8);
    } catch (IOException cause) {
      throw new IllegalStateException("Failed to get rules", cause);
    }

    BuiltinRegistry.getInstance()
        .register(
            "concat",
            inputData ->
                inputData.stream()
                    .map(row -> Arrays.asList(row.get(0), row.get(1), row.get(0) + row.get(1)))
                    .collect(Collectors.toList()),
            3);

    BuiltinRegistry.getInstance()
        .register(
            "minus",
            inputData ->
                inputData.stream()
                    .map(
                        row ->
                            Arrays.asList(
                                row.get(0),
                                row.get(1),
                                String.valueOf(
                                    Float.valueOf(row.get(0)) - Float.valueOf(row.get(1)))))
                    .collect(Collectors.toList()),
            3);

    BuiltinRegistry.getInstance()
        .register(
            "addmod",
            inputData ->
                inputData.stream()
                    .map(
                        row ->
                            Arrays.asList(
                                row.get(0),
                                row.get(1),
                                row.get(2),
                                String.valueOf(
                                    (Float.valueOf(row.get(0)) + Float.valueOf(row.get(1)) + Float.valueOf(row.get(2))) % Float.valueOf(row.get(2))
                                )))
                    .collect(Collectors.toList()),
            4);

    BuiltinRegistry.getInstance()
        .register(
            "cal_time_delta",
            inputData ->
                inputData.stream()
                    .map(
                        row ->
                            Arrays.asList(
                                row.get(0), String.valueOf(Float.valueOf(row.get(0)) / 360 * 24)))
                    .collect(Collectors.toList()),
            2);

    BuiltinRegistry.getInstance()
        .register(
            "greater_than",
            inputData ->
                inputData.stream()
                    .filter(row -> Float.valueOf(row.get(0)) > Float.valueOf(row.get(1)))
                    .collect(Collectors.toList()),
            2);

    Program program = (Program) new Visitor().visit(Parser.parse(ruleStr).logicRules());

    Transformer transformer =
        TransformerCombinator.combineTransformer(new SipTransformer(), new SupMagicTransformer());
    Program transformed = transformer.transform(program);

    LOG.debug("Original program:\n{}", program);
    LOG.debug("Transformed program:\n{}", transformed);
    transformer =
        TransformerCombinator.combineTransformer(new SipTransformer(), new SupMagicTransformer());
    InferEngine engine = new BasicInferEngine(transformer, new SemiNaiveSolver());
    engine.initialize(program);
    SqlDatabaseManager dbm = new SqlDatabaseManager();
    dbm.initializeTablePointer();
    // dbm.createTable("geonames", 19, false);
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

    Long t2 = System.nanoTime();
    LOG.info("Elapsed {} ms", (t2 - t1) / 1000000);
  }
}
