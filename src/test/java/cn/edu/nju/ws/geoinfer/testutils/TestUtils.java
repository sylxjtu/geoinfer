package cn.edu.nju.ws.geoinfer.testutils;

import cn.edu.nju.ws.geoinfer.db.SqlDatabaseManager;
import cn.edu.nju.ws.geoinfer.sql.SqlStorageEngine;
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

  public static void bootstrapDatabase(String dbName) {
    SqlStorageEngine.getInstance().initialize("jdbc:mysql://localhost:3306/", "root", "", false);
    SqlStorageEngine.getInstance().executeSql(String.format("DROP DATABASE IF EXISTS `%s`", dbName));
    SqlStorageEngine.getInstance().executeSql(String.format("CREATE DATABASE `%s`", dbName));
    SqlStorageEngine.getInstance().executeSql(String.format("USE `%s`", dbName));
    SqlStorageEngine.getInstance().bootstrap();
  }

  public static void finalizeDatabase(String dbName) {
    SqlStorageEngine.getInstance().executeSql(String.format("DROP DATABASE `%s`", dbName));
  }
}
