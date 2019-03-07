package cn.edu.nju.ws.geoinfer.data.program;

public class ForceSipPredicate extends RawPredicate {
  public ForceSipPredicate(String name) {
    super(name);
  }

  @Override
  public String toString() {
    return "!" + super.toString();
  }
}
