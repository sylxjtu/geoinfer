package cn.edu.nju.ws.geoinfer.data.program;

import cn.edu.nju.ws.geoinfer.utils.Utils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Atom implements Serializable {
  private Predicate predicate;
  private List<Term> terms;

  public Atom(Predicate predicate, List<Term> terms) {
    this.predicate = predicate;
    this.terms = terms;
  }

  public Predicate getPredicate() {
    return predicate;
  }

  public void setPredicate(Predicate predicate) {
    this.predicate = predicate;
  }

  public List<Term> getTerms() {
    return terms;
  }

  public void setTerms(List<Term> terms) {
    this.terms = terms;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Atom atom = (Atom) o;
    return Objects.equals(predicate, atom.predicate) &&
        Objects.equals(terms, atom.terms);
  }

  @Override
  public int hashCode() {
    return Objects.hash(predicate, terms);
  }

  @Override
  public String toString() {
    return predicate + "(" + Utils.joinAsString(", ", terms) + ")";
  }
}
