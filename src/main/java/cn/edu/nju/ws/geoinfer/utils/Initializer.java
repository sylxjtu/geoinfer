package cn.edu.nju.ws.geoinfer.utils;

import cn.edu.nju.ws.geoinfer.builtin.BuiltinRegistry;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Initializer {
  public static void registerBuiltins() {

    BuiltinRegistry.getInstance()
        .register(
            "concat",
            inputData ->
                inputData.stream()
                    .map(row -> Arrays.asList(row.get(0), row.get(1), row.get(0) + row.get(1)))
                    .collect(Collectors.toList()),
            3);

    BuiltinRegistry.getInstance()
        .register(
            "minus",
            inputData ->
                inputData.stream()
                    .map(
                        row ->
                            Arrays.asList(
                                row.get(0),
                                row.get(1),
                                String.valueOf(
                                    Float.valueOf(row.get(0)) - Float.valueOf(row.get(1)))))
                    .collect(Collectors.toList()),
            3);

    BuiltinRegistry.getInstance()
        .register(
            "addmod",
            inputData ->
                inputData.stream()
                    .map(
                        row ->
                            Arrays.asList(
                                row.get(0),
                                row.get(1),
                                row.get(2),
                                String.valueOf(
                                    (Float.valueOf(row.get(0)) + Float.valueOf(row.get(1)) + Float.valueOf(row.get(2))) % Float.valueOf(row.get(2))
                                )))
                    .collect(Collectors.toList()),
            4);

    BuiltinRegistry.getInstance()
        .register(
            "cal_time_delta",
            inputData ->
                inputData.stream()
                    .map(
                        row ->
                            Arrays.asList(
                                row.get(0), String.valueOf(Float.valueOf(row.get(0)) / 360 * 24)))
                    .collect(Collectors.toList()),
            2);

    BuiltinRegistry.getInstance()
        .register(
            "greater_than",
            inputData ->
                inputData.stream()
                    .filter(row -> Float.valueOf(row.get(0)) > Float.valueOf(row.get(1)))
                    .collect(Collectors.toList()),
            2);
  }

  public static String getRuleFromFile(String pathName) {
    try {
      return FileUtils.readFileToString(new File(pathName), StandardCharsets.UTF_8);
    } catch (IOException cause) {
      throw new IllegalStateException("Failed to get rules", cause);
    }
  }
}
