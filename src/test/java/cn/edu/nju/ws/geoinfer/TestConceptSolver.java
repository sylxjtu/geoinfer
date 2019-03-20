package cn.edu.nju.ws.geoinfer;

import cn.edu.nju.ws.geoinfer.testutils.TestUtils;
import cn.edu.nju.ws.geoinfer.utils.Initializer;
import cn.edu.nju.ws.geoinfer.utils.SimpleInferer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestConceptSolver {
  private static final String DB = "test_concept_solver_" + UUID.randomUUID().toString();

  @Test
  public void testConceptSolverTrue() {
    TestUtils.bootstrapDatabase(DB);

    String rule = Initializer.getRuleFromFile("rules/output_true.rul");

    List<List<String>> result = SimpleInferer.infer(rule, "concept_solver");
    Assert.assertEquals(Collections.singletonList(Collections.singletonList("true")), result);

    TestUtils.finalizeDatabase(DB);
  }

  @Test
  public void testConceptSolverFalse() {
    TestUtils.bootstrapDatabase(DB);

    String rule = Initializer.getRuleFromFile("rules/output_false.rul");

    List<List<String>> result = SimpleInferer.infer(rule, "concept_solver");
    Assert.assertEquals(Collections.singletonList(Collections.singletonList("false")), result);

    TestUtils.finalizeDatabase(DB);
  }
}
