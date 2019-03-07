package cn.edu.nju.ws.geoinfer.algorithm.graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {
  int n;
  private List<Integer>[] adjacentTable;

  @SuppressWarnings("unchecked")
  public Graph(int n) {
    this.adjacentTable = new List[n];
    this.n = n;
    for (int i = 0; i < n; i++) {
      this.adjacentTable[i] = new ArrayList<>();
    }
  }

  public List<Integer> get(int index) {
    return adjacentTable[index];
  }

  public int getOrder() {
    return n;
  }

  public void addEdge(int x, int y) {
    adjacentTable[x].add(y);
  }
}
