package cn.edu.nju.ws.geoinfer.transformer;

import cn.edu.nju.ws.geoinfer.data.program.Program;

/**
 * Transforms a program (list of rules) to another program
 */
public interface Transformer {
  /**
   * Do transform
   *
   * @param program input program
   * @return new program
   */
  Program transform(Program program);
}
