package cn.edu.nju.ws.geoinfer.parser;

import cn.edu.nju.ws.geoinfer.antlr.DatalogBaseVisitor;
import cn.edu.nju.ws.geoinfer.antlr.DatalogParser;
import cn.edu.nju.ws.geoinfer.data.program.*;

import java.util.ArrayList;
import java.util.List;

public class Visitor extends DatalogBaseVisitor<Object> {
  // 解析常量
  @Override
  public Object visitConstant(DatalogParser.ConstantContext ctx) {
    String id = ctx.Str().getSymbol().getText().substring(1, ctx.Str().getSymbol().getText().length() - 1);
    return new Constant(id);
  }

  // 解析变量
  @Override
  public Object visitVariable(DatalogParser.VariableContext ctx) {
    String id = ctx.ID().getSymbol().getText();
    return new Variable(id);
  }

  // 解析字符串
  @Override
  public String visitStringLike(DatalogParser.StringLikeContext ctx) {
    if (ctx.Str() != null) {
      String ret = ctx.Str().getSymbol().getText();
      return ret.replace("\"", "");
    } else {
      return ctx.ID().getSymbol().getText();
    }
  }

  // 解析内建谓词
  @Override
  public Object visitBuiltInPredicate(DatalogParser.BuiltInPredicateContext ctx) {
    return new BuiltinPredicate(visitStringLike(ctx.stringLike()));
  }

  // 解析普通谓词
  @Override
  public Object visitRawPredicate(DatalogParser.RawPredicateContext ctx) {
    return new RawPredicate(visitStringLike(ctx.stringLike()));
  }

  // 解析强制计算顺序谓词
  @Override
  public Object visitForceSipPredicate(DatalogParser.ForceSipPredicateContext ctx) {
    return new ForceSipPredicate(visitStringLike(ctx.stringLike()));
  }

  // 解析文字
  @Override
  public Object visitLiteral(DatalogParser.LiteralContext ctx) {
    Predicate predicate = (Predicate) visit(ctx.predicate());
    List<Term> terms = new ArrayList<>();
    for (DatalogParser.ElementContext element : ctx.element()) {
      terms.add((Term) visit(element));
    }
    return new Atom(predicate, terms);
  }

  // 解析规则
  @Override
  public Object visitLogicRule(DatalogParser.LogicRuleContext ctx) {
    Atom dstLiteral = (Atom) visit(ctx.dstLiteral());
    List<Atom> srcLiterals = new ArrayList<>();
    for (DatalogParser.LiteralContext literalContext : ctx.srcLiteralList().literal()) {
      srcLiterals.add((Atom) visit(literalContext));
    }
    return new Rule(dstLiteral, srcLiterals);
  }

  // 解析事实
  @Override
  public Object visitLogicFact(DatalogParser.LogicFactContext ctx) {
    Atom dstLiteral = (Atom) visit(ctx.literal());
    List<Atom> srcLiterals = new ArrayList<>();
    return new Rule(dstLiteral, srcLiterals);
  }

  // 解析规则集
  @Override
  public Object visitLogicRules(DatalogParser.LogicRulesContext ctx) {
    List<Rule> rules = new ArrayList<>();
    for (DatalogParser.StatementContext statementContext : ctx.statement()) {
      rules.add((Rule) visit(statementContext));
    }
    Program program = new Program(rules, null);
    if (ctx.goal() != null) {
      program.setGoal((Atom) visit(ctx.goal().literal()));
    }
    return program;
  }
}
