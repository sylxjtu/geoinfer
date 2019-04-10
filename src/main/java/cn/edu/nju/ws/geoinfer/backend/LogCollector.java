package cn.edu.nju.ws.geoinfer.backend;

import java.util.ArrayList;
import java.util.List;

public class LogCollector {
  private static LogCollector ourInstance = new LogCollector();
  private List<String> logs;

  private LogCollector() {
  }

  public static LogCollector getInstance() {
    return ourInstance;
  }

  public boolean initialized() {
    return logs != null;
  }

  public void initialize() {
    logs = new ArrayList<>();
  }

  public void output(String s) {
    logs.add(s);
  }

  public List<String> getLogs() {
    return logs;
  }
}
