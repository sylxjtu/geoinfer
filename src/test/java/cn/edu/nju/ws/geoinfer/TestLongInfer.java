package cn.edu.nju.ws.geoinfer;

import cn.edu.nju.ws.geoinfer.sql.SqlStorageEngine;
import cn.edu.nju.ws.geoinfer.utils.SimpleInferer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestLongInfer {
  private static final String DB = "test_long_infer_" + UUID.randomUUID().toString();

  @Test
  public void testLongInfer() {
    SqlStorageEngine.getInstance().initialize("jdbc:mysql://localhost:3306/", "root", "", false);
    SqlStorageEngine.getInstance().executeSql(String.format("DROP DATABASE IF EXISTS `%s`", DB));
    SqlStorageEngine.getInstance().executeSql(String.format("CREATE DATABASE `%s`", DB));
    SqlStorageEngine.getInstance().executeSql(String.format("USE `%s`", DB));
    SqlStorageEngine.getInstance().bootstrap();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      sb.append(String.format("test_%d(X) :- test_%d(X).\n", i + 1, i));
    }
    sb.append("test_0(\"excited\").\n");
    sb.append("?- test_1000(X).\n");
    String rule = sb.toString();
    List<List<String>> result = SimpleInferer.infer(rule, "longinfer");
    Assert.assertEquals(Collections.singletonList(Collections.singletonList("excited")), result);
    SqlStorageEngine.getInstance().executeSql(String.format("DROP DATABASE `%s`", DB));
  }
}
