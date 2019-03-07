package cn.edu.nju.ws.geoinfer.utils;

import cn.edu.nju.ws.geoinfer.data.program.Constant;
import cn.edu.nju.ws.geoinfer.data.program.Term;
import cn.edu.nju.ws.geoinfer.data.program.Variable;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

public class Utils {
  private Utils() {
    throw new IllegalStateException();
  }

  public static <T extends Serializable> T clone(T object) {
    return SerializationUtils.clone(object);
  }

  public static <T> String joinAsString(String delimiter, List<T> objects) {
    return StringUtils.join(objects, delimiter);
  }

  public static <O> O termMap(Term term, Function<String, O> mapConst, Function<String, O> mapVar) {
    if (term instanceof Constant) {
      return mapConst.apply(((Constant) term).getName());
    } else if (term instanceof Variable) {
      return mapVar.apply(((Variable) term).getName());
    } else {
      throw new IllegalArgumentException();
    }
  }

  public static <O> O termMapIfConst(Term term, Function<String, O> mapConst) {
    return termMap(term, mapConst, ign -> null);
  }

  public static <O> O termMapIfVar(Term term, Function<String, O> mapVar) {
    return termMap(term, ign -> null, mapVar);
  }
}
