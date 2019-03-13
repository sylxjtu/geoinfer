package cn.edu.nju.ws.geoinfer;

import cn.edu.nju.ws.geoinfer.testutils.TestUtils;
import cn.edu.nju.ws.geoinfer.utils.SimpleInferer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestManyRecursive {
  private static final String DB = "test_many_recursive_" + UUID.randomUUID().toString();

  @Test
  public void testManyRecursive() {
    TestUtils.bootstrapDatabase(DB);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append(String.format("edge(\"%d\",\"%d\").\n", i, i + 1));
    }
    sb.append("conn(X, Y) :- edge(X, Y).\n");
    sb.append("conn(X, Z) :- edge(X, Y), conn(Y, Z).\n");
    sb.append("?- conn(\"0\", \"1000\").\n");
    String rule = sb.toString();
    List<List<String>> result = SimpleInferer.infer(rule, "many_recursive");
    Assert.assertEquals(Collections.singletonList(Arrays.asList("0", "1000")), result);

    TestUtils.finalizeDatabase(DB);
  }
}
