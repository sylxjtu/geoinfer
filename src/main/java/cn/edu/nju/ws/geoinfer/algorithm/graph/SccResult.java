package cn.edu.nju.ws.geoinfer.algorithm.graph;

/**
 * The result of SCC solving algorithm
 */
public class SccResult {
  private int[] belong;
  private int count;

  SccResult(int[] belong, int count) {
    this.belong = belong;
    this.count = count;
  }

  /**
   * Get the belong of each vertex
   *
   * @return the belong of each vertex
   */
  public int[] getBelong() {
    return belong;
  }

  /**
   * Get vertex count
   *
   * @return vertex count
   */
  public int getCount() {
    return count;
  }
}
