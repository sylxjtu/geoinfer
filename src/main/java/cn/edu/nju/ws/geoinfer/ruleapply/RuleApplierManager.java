package cn.edu.nju.ws.geoinfer.ruleapply;

import cn.edu.nju.ws.geoinfer.builtin.BuiltinRegistry;
import cn.edu.nju.ws.geoinfer.data.miscellaneous.SolveStatus;
import cn.edu.nju.ws.geoinfer.data.miscellaneous.TablePointerPair;
import cn.edu.nju.ws.geoinfer.data.program.*;
import cn.edu.nju.ws.geoinfer.data.rarule.*;
import cn.edu.nju.ws.geoinfer.db.DatabaseManager;
import cn.edu.nju.ws.geoinfer.db.DatabaseTable;
import cn.edu.nju.ws.geoinfer.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RuleApplierManager<T extends DatabaseTable> {
  private static final String NON_VARIABLE_FIELD = "*";
  private static final Logger LOG = LoggerFactory.getLogger(RuleApplierManager.class);

  void applyRule(Rule rule, DatabaseManager<T> dbm) {
    LOG.debug("Applying {}", rule);

    T currentTable = null;
    List<String> currentVarFields = new ArrayList<>();

    String name = rule.getHead().getPredicate().getTableName();
    int arity = rule.getHead().getTerms().size();
    T outputTable = dbm.createTable(name, arity, false);

    // Work with fact
    if (rule.getBody().isEmpty()) {
      applyFact(rule.getHead(), dbm, outputTable);
      return;
    }

    for (Atom atom : rule.getBody()) {
      String tableName = atom.getPredicate().getTableName();
      T atomTable = null;

      // Step 0: builtin
      if (atom.getPredicate() instanceof BuiltinPredicate) {
        BuiltinPredicate predicate = (BuiltinPredicate) atom.getPredicate();
        T selectedTable = selectFields(currentTable, currentVarFields, atom, dbm);
        List<List<String>> transformedData =
            BuiltinRegistry.getInstance().call(predicate.getName(), dbm.getData(selectedTable));
        atomTable = dbm.putData(transformedData, BuiltinRegistry.getInstance().getArity(predicate.getName()), null);
      } else {
        atomTable = dbm.getTable(tableName);
      }

      // Step 1: filter
      // P(a, b, ..., X, Y, ...) -> SELECT * FROM P WHERE 1=a and 2=b
      List<String> atomVarFields = getAtomVarFields(atom);
      atomTable = filterAtom(atom, atomVarFields, dbm, atomTable);

      // Step 2: join
      SolveStatus<T> joinResult =
          joinBodyAtom(currentTable, atomTable, currentVarFields, atomVarFields, dbm);
      currentTable = joinResult.getTable();
      currentVarFields = joinResult.getVarFields();

      // Step 3: remove redundant fields
      SolveStatus<T> simplifiedResult = removeRedundantFields(currentTable, currentVarFields, dbm);
      currentTable = simplifiedResult.getTable();
      currentVarFields = simplifiedResult.getVarFields();
    }

    // Step 4: select
    currentTable = selectFields(currentTable, currentVarFields, rule.getHead(), dbm);

    // Step 5: union and save
    dbm.union(outputTable, currentTable);
  }

  /**
   * @param predicate
   * @param dbm
   * @return if semi naive need to continue
   */
  boolean updatePointer(Predicate predicate, DatabaseManager<T> dbm) {
    String tableName = predicate.getTableName();
    if (tableName == null) return false;
    T table = dbm.getTable(tableName);
    TablePointerPair pointerPair = dbm.getTablePointer(table);
    int tailPointer = dbm.getTableTailPointer(table);
    TablePointerPair newPointerPair =
        new TablePointerPair(pointerPair.getCurrentPointer(), tailPointer);
    dbm.setTablePointer(table, newPointerPair);
    LOG.debug("Table {} current pointer {} update to {}", predicate.getTableName(), pointerPair.getCurrentPointer(), tailPointer);
    return pointerPair.getCurrentPointer() != tailPointer;
  }

  private void applyFact(Atom head, DatabaseManager<T> dbm, T table) {
    List<String> row = new ArrayList<>();
    for (int i = 0; i < head.getTerms().size(); i++) {
      Term term = head.getTerms().get(i);
      Utils.termMap(
          term,
          row::add,
          v -> {
            throw new IllegalArgumentException("Unsafe variable " + v);
          });
    }
    dbm.insertIntoTable(table, row);
    dbm.ensureUnique(table);
  }

  private List<String> getAtomVarFields(Atom atom) {
    List<String> atomVarFields = new ArrayList<>();
    for (Term term : atom.getTerms()) {
      Utils.termMap(term, c -> atomVarFields.add(NON_VARIABLE_FIELD), atomVarFields::add);
    }
    return atomVarFields;
  }

  private T filterAtom(Atom atom, List<String> atomVarFields, DatabaseManager<T> dbm, T atomTable) {
    List<FilterRule> filterRules = new ArrayList<>();
    for (int termIndex = 0; termIndex < atom.getTerms().size(); termIndex++) {
      Term term = atom.getTerms().get(termIndex);
      if (term instanceof Constant) {
        filterRules.add(new ConstantFilterRule(termIndex, ((Constant) term).getName()));
      } else if (term instanceof Variable) {
        for (int anotherTermIndex = 0; anotherTermIndex < termIndex; anotherTermIndex++) {
          if (((Variable) term).getName().equals(atomVarFields.get(anotherTermIndex))) {
            filterRules.add(new ColumnFilterRule(termIndex, anotherTermIndex));
          }
        }
      } else {
        throw new IllegalArgumentException();
      }
    }
    if (atom.getPredicate() instanceof DeltaOldPredicate) {
      TablePointerPair pointerPair = dbm.getTablePointer(atomTable);
      return dbm.filterWithPointer(
          atomTable,
          filterRules,
          pointerPair.getLastPointer() + 1,
          pointerPair.getCurrentPointer());
    } else if (atom.getPredicate() instanceof OldPredicate) {
      TablePointerPair pointerPair = dbm.getTablePointer(atomTable);
      return dbm.filterWithPointer(atomTable, filterRules, 0, pointerPair.getLastPointer());
    }
    return dbm.filter(atomTable, filterRules);
  }

  private T selectFields(
      T currentTable, List<String> currentVarFields, Atom selectionAtom, DatabaseManager<T> dbm) {
    List<SelectionRule> selectionRules = new ArrayList<>();
    for (int i = 0; i < selectionAtom.getTerms().size(); i++) {
      Term term = selectionAtom.getTerms().get(i);
      if (term instanceof Constant) {
        selectionRules.add(new ConstantSelectionRule(((Constant) term).getName()));
      } else if (term instanceof Variable) {
        String variableId = ((Variable) term).getName();
        boolean updated = false;
        for (int j = 0; j < currentVarFields.size(); j++) {
          if (variableId.equals(currentVarFields.get(j))) {
            selectionRules.add(new VariableSelectionRule(j));
            updated = true;
            break;
          }
        }
        if (!updated) {
          selectionRules.add(new NullSelectionRule());
        }
      } else {
        throw new IllegalArgumentException();
      }
    }
    return dbm.select(currentTable, selectionRules);
  }

  private SolveStatus<T> joinBodyAtom(
      T currentTable,
      T atomTable,
      List<String> currentVarFields,
      List<String> atomVarFields,
      DatabaseManager<T> dbm) {
    List<JoinRule> joinRules = new ArrayList<>();
    if (currentTable == null) {
      return new SolveStatus<>(atomTable, atomVarFields);
    } else {
      for (int i = 0; i < currentVarFields.size(); i++) {
        if (currentVarFields.get(i).equals(NON_VARIABLE_FIELD)) continue;
        for (int j = 0; j < atomVarFields.size(); j++) {
          if (atomVarFields.get(j).equals(NON_VARIABLE_FIELD)) continue;
          if (atomVarFields.get(j).equals(currentVarFields.get(i))) {
            joinRules.add(new JoinRule(i, j));
          }
        }
      }
      currentTable = dbm.join(currentTable, atomTable, joinRules);
      currentVarFields.addAll(atomVarFields);
      return new SolveStatus<>(currentTable, currentVarFields);
    }
  }

  private SolveStatus<T> removeRedundantFields(
      T currentTable, List<String> currentVarFields, DatabaseManager<T> dbm) {
    boolean[] toRemove = new boolean[currentVarFields.size()];
    List<SelectionRule> selectionRules = new ArrayList<>();
    for (int i = 0; i < currentVarFields.size(); i++) {
      if (toRemove[i] || currentVarFields.get(i).equals(NON_VARIABLE_FIELD)) continue;
      for (int j = i + 1; j < currentVarFields.size(); j++) {
        if (toRemove[j] || currentVarFields.get(j).equals(NON_VARIABLE_FIELD)) continue;
        if (currentVarFields.get(j).equals(currentVarFields.get(i))) toRemove[j] = true;
      }
    }
    for (int i = 0; i < currentVarFields.size(); i++) {
      if (!toRemove[i]) selectionRules.add(new VariableSelectionRule(i));
    }
    currentTable = dbm.select(currentTable, selectionRules);
    List<String> newFields = new ArrayList<>();
    for (int i = 0; i < currentVarFields.size(); i++) {
      if (!toRemove[i]) newFields.add(currentVarFields.get(i));
    }
    currentVarFields = newFields;
    return new SolveStatus<>(currentTable, currentVarFields);
  }
}
