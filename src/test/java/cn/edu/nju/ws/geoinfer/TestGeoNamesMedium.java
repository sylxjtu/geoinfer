package cn.edu.nju.ws.geoinfer;

import cn.edu.nju.ws.geoinfer.testutils.TestUtils;
import cn.edu.nju.ws.geoinfer.utils.Initializer;
import cn.edu.nju.ws.geoinfer.utils.SimpleInferer;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class TestGeoNamesMedium {
  private static final String DB = "test_geo_names_medium_" + UUID.randomUUID().toString();
  private static final String RULE_FILE = "rules/geonames_medium.txt";

  @Test
  public void testGeoNamesMedium() {
    TestUtils.bootstrapDatabase(DB);
    TestUtils.importGeoNames();

    String rule = Initializer.getRuleFromFile(RULE_FILE);
    Initializer.registerBuiltins();

    List<List<String>> result = SimpleInferer.infer(rule, "geonames_medium");
    Assert.assertEquals("P", result.get(0).get(0));
    float value = Float.valueOf(result.get(0).get(1));
    Assert.assertTrue(result.get(0).get(1), value > 19 && value < 21);

    TestUtils.finalizeDatabase(DB);
  }
}
