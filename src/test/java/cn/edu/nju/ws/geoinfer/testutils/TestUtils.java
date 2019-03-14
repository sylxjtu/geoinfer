package cn.edu.nju.ws.geoinfer.testutils;

import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.db.SqlDatabaseManager;
import cn.edu.nju.ws.geoinfer.lowlevelstorage.sql.SqlStorageEngine;
import cn.edu.nju.ws.geoinfer.seminaive.TablePointerRegistry;
import com.google.common.collect.Streams;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
  public static void importGeoNames(DatabaseManager<?> dbm) {
    List<List<String>> data = null;
    try {
      data = CSVParser.parse(new File("data/cities500.csv"), StandardCharsets.UTF_8, CSVFormat.DEFAULT)
          .getRecords().stream().map(record -> Streams.stream(record.iterator()).collect(Collectors.toList())).collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalStateException("", e);
    }
    dbm.putData(data, data.get(0).size(), "geonames");
  }

  public static void importGeoNamesToDatabase() {
    importGeoNames(new SqlDatabaseManager());
  }

  public static void bootstrapDatabase(String dbName) {
    SqlStorageEngine.getInstance().initialize("jdbc:mysql://localhost:3306/", "root", "", false);
    SqlStorageEngine.getInstance().executeSql(String.format("DROP DATABASE IF EXISTS `%s`", dbName));
    SqlStorageEngine.getInstance().executeSql(String.format("CREATE DATABASE `%s`", dbName));
    SqlStorageEngine.getInstance().executeSql(String.format("USE `%s`", dbName));
    SqlStorageEngine.getInstance().bootstrap();
    TablePointerRegistry.getInstance().initialize();
  }

  public static void finalizeDatabase(String dbName) {
    SqlStorageEngine.getInstance().executeSql(String.format("DROP DATABASE `%s`", dbName));
  }
}
