package cn.edu.nju.ws.geoinfer.data.program;

import cn.edu.nju.ws.geoinfer.utils.Utils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Program implements Serializable {
  private List<Rule> rules;
  private Atom goal;

  public Program(List<Rule> rules, Atom goal) {
    this.rules = rules;
    this.goal = goal;
  }

  public List<Rule> getRules() {
    return rules;
  }

  public void setRules(List<Rule> rules) {
    this.rules = rules;
  }

  public Atom getGoal() {
    return goal;
  }

  public void setGoal(Atom goal) {
    this.goal = goal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Program program = (Program) o;
    return Objects.equals(rules, program.rules) &&
        Objects.equals(goal, program.goal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rules, goal);
  }

  @Override
  public String toString() {
    return Utils.joinAsString("\n", rules) + "\n" + "?- " + goal;
  }
}
