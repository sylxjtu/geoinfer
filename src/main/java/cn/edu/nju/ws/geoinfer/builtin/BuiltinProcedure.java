package cn.edu.nju.ws.geoinfer.builtin;

import java.util.List;

public interface BuiltinProcedure {
  List<List<String>> evaluate(List<List<String>> inputData);
}
