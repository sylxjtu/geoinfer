package cn.edu.nju.ws.geoinfer;

import cn.edu.nju.ws.geoinfer.sql.SqlStorageEngine;
import cn.edu.nju.ws.geoinfer.utils.SimpleInferer;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

public class TestLongInfer {
  @Test
  public void testLongInfer() {
    try {
      SqlStorageEngine.getInstance().initialize("jdbc:mysql://localhost:3306/geoinfer_test", "root", "");
    } catch (SQLException cause) {
      throw new IllegalStateException("Failed to connect db", cause);
    }

    SqlStorageEngine.getInstance().bootstrap();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append(String.format("test_%d(X) :- test_%d(X).\n", i + 1, i));
    }
    sb.append("test_0(\"excited\").\n");
    sb.append("?- test_1000(X).\n");
    String rule = sb.toString();
    List<List<String>> result = SimpleInferer.infer(rule);
    Assert.assertEquals("excited", result.get(0).get(0));
  }
}
