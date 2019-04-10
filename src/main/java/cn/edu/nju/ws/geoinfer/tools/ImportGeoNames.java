package cn.edu.nju.ws.geoinfer.tools;

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

public class ImportGeoNames {
  private static void importGeoNames() {
    List<List<String>> data = null;
    try {
      data = CSVParser.parse(new File("data/cities500.csv"), StandardCharsets.UTF_8, CSVFormat.DEFAULT)
          .getRecords().stream().map(record -> Streams.stream(record.iterator()).collect(Collectors.toList())).collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalStateException("", e);
    }

    SqlDatabaseManager databaseManager = new SqlDatabaseManager(false);
    databaseManager.putData(data, data.get(0).size(), "geonames");
  }

  public static void main(String[] args) {
    String db = args.length > 1 ? args[1] : "geonames";
    SqlStorageEngine.getInstance()
        .initialize("jdbc:mysql://localhost:3306/?characterEncoding=utf8", "root", "dhf19700101");
    SqlStorageEngine.getInstance()
        .executeSql(
            String.format(
                "CREATE DATABASE IF NOT EXISTS `%s` character set UTF8 collate utf8_bin", db));
    SqlStorageEngine.getInstance().executeSql(String.format("USE `%s`", db));
    importGeoNames();
  }
}
