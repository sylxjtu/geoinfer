package cn.edu.nju.ws.geoinfer.transformer;

import cn.edu.nju.ws.geoinfer.data.program.Program;

/**
 * Identity transformer
 */
public class IdTransformer implements Transformer {
  @Override
  public Program transform(Program program) {
    return program;
  }
}
