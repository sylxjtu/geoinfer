package cn.edu.nju.ws.geoinfer.data.program;

import cn.edu.nju.ws.geoinfer.utils.Utils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Rule implements Serializable {
  private Atom head;
  private List<Atom> body;

  public Rule(Atom head, List<Atom> body) {
    this.head = head;
    this.body = body;
  }

  public Atom getHead() {
    return head;
  }

  public void setHead(Atom head) {
    this.head = head;
  }

  public List<Atom> getBody() {
    return body;
  }

  public void setBody(List<Atom> body) {
    this.body = body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Rule rule = (Rule) o;
    return Objects.equals(head, rule.head) &&
        Objects.equals(body, rule.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(head, body);
  }

  @Override
  public String toString() {
    return head + " :- " + Utils.joinAsString(", ", body);
  }
}
