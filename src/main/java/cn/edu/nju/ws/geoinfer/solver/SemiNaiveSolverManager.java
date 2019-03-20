package cn.edu.nju.ws.geoinfer.solver;

import cn.edu.nju.ws.geoinfer.algorithm.graph.Graph;
import cn.edu.nju.ws.geoinfer.algorithm.graph.GraphUtils;
import cn.edu.nju.ws.geoinfer.algorithm.graph.SccResult;
import cn.edu.nju.ws.geoinfer.data.program.*;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.db.DatabaseTable;
import cn.edu.nju.ws.geoinfer.ruleapply.RuleApplier;
import cn.edu.nju.ws.geoinfer.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SemiNaiveSolverManager<T extends DatabaseTable> {
  private static final Logger LOG = LoggerFactory.getLogger(SemiNaiveSolverManager.class);

  private DatabaseManager<T> dbm;
  private Map<Predicate, Integer> predicateSet;
  private List<Predicate> predicates;
  private Map<Predicate, Integer> predicateToInt;
  private Graph predicateGraph = null;
  private List<List<Integer>> sccList = null;
  private List<Boolean> predicateIsRecursiveMap = null;
  private List<List<Rule>> recursiveRules;
  private List<List<Rule>> nonRecursiveRules;

  SemiNaiveSolverManager() {
    predicates = new ArrayList<>();
    predicateToInt = new HashMap<>();
    predicateSet = new HashMap<>();
  }

  /**
   * Implements semi naive solving strategy
   *
   * @param program the input program
   * @param dbm the db manager
   * @return the table of goal
   */
  T solve(Program program, DatabaseManager<T> dbm) {
    this.dbm = dbm;
    collectPredicates(program);
    createTables();
    predicateGraph = buildPredicateGraph(program);
    SccResult sccResult = GraphUtils.stronglyConnectedComponent(predicateGraph);
    sccList = getSccList(sccResult);
    predicateIsRecursiveMap = buildPredicateRecursiveMap();
    buildPredicateRules(program, sccResult.getBelong());

    for (List<Integer> scc : sccList) {
      generalSemiNaiveScc(scc);
    }

    return applyGoal(program.getGoal());
  }

  /**
   * Collects predicates and save them into a list and a map
   *
   * @param program input program
   */
  private void collectPredicates(Program program) {
    for (Rule rule : program.getRules()) {
      predicateSet.computeIfAbsent(rule.getHead().getPredicate(), predicate -> rule.getHead().getTerms().size());
      for (Atom atom : rule.getBody()) {
        predicateSet.computeIfAbsent(atom.getPredicate(), predicate -> atom.getTerms().size());
      }
    }
    predicateSet.computeIfAbsent(program.getGoal().getPredicate(), predicate -> program.getGoal().getTerms().size());

    predicates.addAll(predicateSet.keySet());
    for (int predicateIndex = 0; predicateIndex < predicates.size(); predicateIndex++) {
      predicateToInt.put(predicates.get(predicateIndex), predicateIndex);
    }
  }

  /**
   * Builds predicate graph for scc input
   *
   * @param program input program
   * @return output predicate graph
   */
  private Graph buildPredicateGraph(Program program) {
    int predicateCount = predicates.size();
    Graph ret = new Graph(predicateCount);
    for (Rule rule : program.getRules()) {
      Atom head = rule.getHead();
      for (Atom bodyAtom : rule.getBody()) {
        ret.addEdge(
            predicateToInt.get(bodyAtom.getPredicate()), predicateToInt.get(head.getPredicate()));
      }
    }
    return ret;
  }

  /**
   * Transform scc result to list of scc
   *
   * @param sccResult the original scc result
   * @return list of scc
   */
  private List<List<Integer>> getSccList(SccResult sccResult) {
    int sccCount = sccResult.getCount();
    int[] sccBelong = sccResult.getBelong();

    List<List<Integer>> ret = new ArrayList<>();
    for (int sccIndex = 0; sccIndex < sccCount; sccIndex++) {
      ret.add(new ArrayList<>());
    }
    for (int vertexIndex = 0; vertexIndex < sccBelong.length; vertexIndex++) {
      ret.get(sccBelong[vertexIndex]).add(vertexIndex);
    }

    return ret;
  }

  /**
   * Computes whether predicate is recursive
   *
   * @return a list of whether predicate is recursive
   */
  private List<Boolean> buildPredicateRecursiveMap() {
    List<Boolean> ret = new ArrayList<>();
    for (int predicateIndex = 0; predicateIndex < predicates.size(); predicateIndex++) {
      ret.add(false);
    }
    for (List<Integer> scc : sccList) {
      if (scc.isEmpty()) {
        throw new IllegalArgumentException();
      } else if (scc.size() == 1) {
        int node = scc.get(0);
        if (predicateGraph.get(node).contains(node)) {
          ret.set(node, true);
        }
      } else {
        for (int node : scc) {
          ret.set(node, true);
        }
      }
    }
    return ret;
  }

  /**
   * Build a list of rules according to scc
   *
   * @param program input program
   * @param sccBelong the belonging of scc predicate
   */
  private void buildPredicateRules(Program program, int[] sccBelong) {
    recursiveRules = new ArrayList<>();
    nonRecursiveRules = new ArrayList<>();
    for (int predicateIndex = 0; predicateIndex < predicates.size(); predicateIndex++) {
      recursiveRules.add(new ArrayList<>());
      nonRecursiveRules.add(new ArrayList<>());
    }
    for (Rule rule : program.getRules()) {
      int headNode = predicateToInt.get(rule.getHead().getPredicate());
      boolean recursive = false;
      for (Atom atom : rule.getBody()) {
        int bodyNode = predicateToInt.get(atom.getPredicate());
        if (sccBelong[headNode] == sccBelong[bodyNode]) {
          recursive = true;
          break;
        }
      }
      List<List<Rule>> ruleSet = recursive ? recursiveRules : nonRecursiveRules;
      ruleSet.get(headNode).add(rule);
    }
  }

  /**
   * Implements general semi naive algorithm for scc
   *
   * @param scc the scc to be evaluated
   */
  private void generalSemiNaiveScc(List<Integer> scc) {
    Long ts = System.nanoTime();

    if (scc.isEmpty()) {
      throw new IllegalArgumentException();
    }
    int firstNode = scc.get(0);
    if (predicateIsRecursiveMap.get(firstNode)) {
      // SCC is recursive node clique
      List<Rule> cliqueRecursiveRuleList = new ArrayList<>();
      for (int node : scc) {
        for (Rule rule : nonRecursiveRules.get(node)) {
          RuleApplier.applyRule(rule, dbm);
        }
        cliqueRecursiveRuleList.addAll(recursiveRules.get(node));
      }
      semiNaiveSolve(cliqueRecursiveRuleList);
    } else {
      // SCC is non-recursive node clique
      for (Rule rule : nonRecursiveRules.get(firstNode)) {
        RuleApplier.applyRule(rule, dbm);
      }
    }

    Long te = System.nanoTime();
    LOG.info("SCC {} Elapsed {} ms", scc.stream().map(id -> predicates.get(id)).toArray(), (te - ts) / 1000000);
  }

  /**
   * Apply semi-naive until reaches fix point
   *
   * @param recursiveRules the rules to be applied
   */
  private void semiNaiveSolve(List<Rule> recursiveRules) {
    LOG.debug("Entering clique");
    for (Rule rule : recursiveRules) {
      LOG.debug("Clique has rule {}", rule);
//      Predicate headPredicate = rule.getHead().getPredicate();
//      int arity = rule.getHead().getTerms().size();
//      dbm.createTable(headPredicate.getTableName(), arity, false);
    }
    while (true) {
      boolean isContinue = false;
      for (Rule rule : recursiveRules) {
        Rule newRule = Utils.clone(rule);
        for (int atomIndex = 0; atomIndex < rule.getBody().size(); atomIndex++) {
          Predicate predicate = newRule.getBody().get(atomIndex).getPredicate();
          if (predicate instanceof BuiltinPredicate) continue;
          Predicate deltaOldPredicate = new DeltaOldPredicate(predicate);
          newRule.getBody().get(atomIndex).setPredicate(deltaOldPredicate);
          RuleApplier.applyRule(newRule, dbm);
          Predicate oldPredicate = new OldPredicate(predicate);
          newRule.getBody().get(atomIndex).setPredicate(oldPredicate);
        }
        isContinue = RuleApplier.updatePointer(rule.getHead().getPredicate(), dbm) || isContinue;
      }
      if (!isContinue) {
        LOG.debug("Exiting clique");
        break;
      }
    }
  }

  private T applyGoal(Atom goal) {
    return RuleApplier.applyGoal(goal, dbm);
  }

  private void createTables() {
    for (Map.Entry<Predicate, Integer> entry : predicateSet.entrySet()) {
      if (entry.getKey() instanceof BuiltinPredicate) continue;
      dbm.createTable(entry.getKey().getTableName(), entry.getValue(), false);
    }
  }
}
