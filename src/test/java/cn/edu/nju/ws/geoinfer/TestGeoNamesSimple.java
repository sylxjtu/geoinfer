package cn.edu.nju.ws.geoinfer;

import cn.edu.nju.ws.geoinfer.testutils.TestUtils;
import cn.edu.nju.ws.geoinfer.utils.Initializer;
import cn.edu.nju.ws.geoinfer.utils.SimpleInferer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestGeoNamesSimple {
  private static final String DB = "test_geo_names_simple_" + UUID.randomUUID().toString();
  private static final String RULE_FILE = "rules/geonames_simple.txt";

  @Test
  public void testGeoNamesSimple() {
    TestUtils.bootstrapDatabase(DB);
    TestUtils.importGeoNamesToDatabase();

    String rule = Initializer.getRuleFromFile(RULE_FILE);
    Initializer.registerBuiltins();

    List<List<String>> result = SimpleInferer.infer(rule, "geonames_simple");
    Assert.assertEquals(Collections.singletonList(Collections.singletonList("Beijing")), result);

    TestUtils.finalizeDatabase(DB);
  }
}
