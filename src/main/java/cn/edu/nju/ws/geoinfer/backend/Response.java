package cn.edu.nju.ws.geoinfer.backend;

import java.util.List;

class Response {
  private List<String> logs;
  private List<List<String>> result;

  Response(List<String> logs, List<List<String>> result) {
    this.logs = logs;
    this.result = result;
  }

  public List<String> getLogs() {
    return logs;
  }

  public List<List<String>> getResult() {
    return result;
  }
}
