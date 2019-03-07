package cn.edu.nju.ws.geoinfer.builtin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltinRegistry {
  private static BuiltinRegistry ourInstance = new BuiltinRegistry();
  private Map<String, BuiltinProcedure> builtinMap;
  private Map<String, Integer> builtinArityMap;

  private BuiltinRegistry() {
    builtinMap = new HashMap<>();
    builtinArityMap = new HashMap<>();
  }

  public static BuiltinRegistry getInstance() {
    return ourInstance;
  }

  public void register(String name, BuiltinProcedure procedure, int arity) {
    builtinMap.put(name, procedure);
    builtinArityMap.put(name, arity);
  }

  public int getArity(String name) {
    return builtinArityMap.get(name);
  }

  public List<List<String>> call(String name, List<List<String>> data) {
    return builtinMap.get(name).evaluate(data);
  }
}
