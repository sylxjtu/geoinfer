package cn.edu.nju.ws.geoinfer.algorithm.graph;

import java.util.ArrayList;
import java.util.List;

class PostOrderSolver {
  private Graph graph;
  private int n;
  private boolean[] visited;
  private List<Integer> postOrder;

  PostOrderSolver(Graph graph) {
    this.graph = graph;
    this.n = graph.n;
    this.visited = new boolean[n];
    this.postOrder = new ArrayList<>();
  }

  private void dfs(int n) {
    visited[n] = true;
    for (int i : graph.get(n)) {
      if (!visited[i]) dfs(i);
    }
    postOrder.add(n);
  }

  List<Integer> solve() {
    for (int i = 0; i < n; i++) {
      if (!visited[i]) dfs(i);
    }
    return postOrder;
  }
}
