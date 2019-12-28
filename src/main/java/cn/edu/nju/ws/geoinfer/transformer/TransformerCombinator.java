package cn.edu.nju.ws.geoinfer.transformer;

import cn.edu.nju.ws.geoinfer.backend.LogCollector;

public class TransformerCombinator {
  private TransformerCombinator() {
  }

  public static Transformer combineTransformer(Transformer... transformers) {
    return program -> {
      for (Transformer transformer : transformers) {
        program = transformer.transform(program);
      }
      return program;
    };
  }
}
