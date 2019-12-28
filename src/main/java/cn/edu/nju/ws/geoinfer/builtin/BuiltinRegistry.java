package cn.edu.nju.ws.geoinfer.builtin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltinRegistry {
  private static BuiltinRegistry ourInstance = new BuiltinRegistry();
  private Map<String, BuiltinProcedure> builtinMap;
  private Map<String, Integer> builtinArityMap;

  // 单例模式创建实例
  private BuiltinRegistry() {
    builtinMap = new HashMap<>();
    builtinArityMap = new HashMap<>();
  }

  // 单例模式获取示例
  public static BuiltinRegistry getInstance() {
    return ourInstance;
  }

  // 注册内建谓词
  public void register(String name, BuiltinProcedure procedure, int arity) {
    builtinMap.put(name, procedure);
    builtinArityMap.put(name, arity);
  }

  // 获取内建谓词操作数个数
  public int getArity(String name) {
    return builtinArityMap.get(name);
  }

  // 调用内建谓词
  public List<List<String>> call(String name, List<List<String>> data) {
    return builtinMap.get(name).evaluate(data);
  }
}
