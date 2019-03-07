package cn.edu.nju.ws.geoinfer.algorithm.graph;

public class GraphUtils {
  private GraphUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static SccResult stronglyConnectedComponent(Graph graph) {
    return new SccSolver(graph).solve();
  }
}
