package cn.edu.nju.ws.geoinfer.algorithm.graph;

import java.util.Collections;
import java.util.List;

class SccSolver {
  private Graph graph;
  private Graph reverseGraph;
  private int n;

  private boolean[] visited;
  private int[] id;
  private int count;

  SccSolver(Graph graph) {
    this.graph = graph;
    this.n = graph.n;
    visited = new boolean[n];
    id = new int[n];
  }

  private void dfs(int n) {
    visited[n] = true;
    id[n] = count;
    for (int i : reverseGraph.get(n)) {
      if (!visited[i]) dfs(i);
    }
  }

  SccResult solve() {
    reverseGraph = new Graph(n);
    for (int i = 0; i < n; i++) {
      for (int j : graph.get(i)) {
        reverseGraph.get(j).add(i);
      }
    }
    List<Integer> postOrder = new PostOrderSolver(graph).solve();
    Collections.reverse(postOrder);
    for (int i : postOrder) {
      if (!visited[i]) {
        dfs(i);
        count++;
      }
    }
    return new SccResult(id, count);
  }
}
