package cn.edu.nju.ws.geoinfer.parser;

import cn.edu.nju.ws.geoinfer.antlr.DatalogLexer;
import cn.edu.nju.ws.geoinfer.antlr.DatalogParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Parser {
  private Parser() {
  }

  public static DatalogParser parse(String s) {
    CharStream stream = CharStreams.fromString(s);
    DatalogLexer lexer = new DatalogLexer(stream);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);

    return new DatalogParser(tokenStream);
  }
}
