package cn.edu.nju.ws.geoinfer.testutils;

import cn.edu.nju.ws.geoinfer.db.SqlDatabaseManager;
import com.google.common.collect.Streams;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
  public static void importGeoNames() {
    List<List<String>> data = null;
    try {
      data = CSVParser.parse(new File("data/cities500.csv"), StandardCharsets.UTF_8, CSVFormat.DEFAULT)
          .getRecords().stream().map(record -> Streams.stream(record.iterator()).collect(Collectors.toList())).collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalStateException("", e);
    }

    SqlDatabaseManager databaseManager = new SqlDatabaseManager();
    databaseManager.putData(data, data.get(0).size(), "geonames");
  }
}
