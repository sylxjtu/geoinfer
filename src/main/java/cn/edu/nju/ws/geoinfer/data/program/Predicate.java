package cn.edu.nju.ws.geoinfer.data.program;

import java.io.Serializable;

public abstract class Predicate implements Serializable {
  /**
   * Returns full name (e.g. with magic mark and adornment) of the predicate.
   *
   * @return full name of the predicate
   */
  public abstract String getFullName();

  /**
   * Returns referenced table name
   *
   * @return table name
   */
  public String getTableName() {
    return getFullName();
  }

  @Override
  public String toString() {
    return getFullName();
  }
}
