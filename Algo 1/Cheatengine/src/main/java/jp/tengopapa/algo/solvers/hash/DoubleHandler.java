package jp.tengopapa.algo.solvers.hash;

import jp.tengopapa.algo.solvers.PolishNotation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoubleHandler implements HashTableHandler {
    private int tableSize = 11;

    private String equationH1 = "k % 11";
    private String equationH2 = "1 + (k % 10)";

    private final TableEntry[] tableData = new TableEntry[tableSize];
    private final List<Object[]> jtableData = new ArrayList<>();

    private JTable table;
    private String[] headers;

    @Override
    public boolean needsConfig() {
        return true;
    }

    @Override
    public void showConfigPanel(JFrame frame) {
        int tableSize = 0;

        while(tableSize <= 0) {
            String v = JOptionPane.showInputDialog(frame, "m =", frame.getTitle(), JOptionPane.QUESTION_MESSAGE);

            try {
                tableSize = Integer.parseInt(v);
            } catch (Exception ignored) { }
        }

        this.tableSize = tableSize;

        String enteredEquation = null;

        while(enteredEquation == null || enteredEquation.length() == 0) {
            enteredEquation = JOptionPane.showInputDialog(frame, "h₁ (k) =", frame.getTitle(), JOptionPane.QUESTION_MESSAGE);
        }

        equationH1 = enteredEquation;
        enteredEquation = null;

        while(enteredEquation == null || enteredEquation.length() == 0) {
            enteredEquation = JOptionPane.showInputDialog(frame, "h₂ (k) =", frame.getTitle(), JOptionPane.QUESTION_MESSAGE);
        }

        equationH2 = enteredEquation;

        table = new JTable();
        table.setShowGrid(true);
        headers = new String[5 + tableSize];
        headers[0] = "Művelet";
        headers[1] = "k";
        headers[2] = "h₁(k)";
        headers[3] = "h₂(k)";
        headers[4] = "Próbasorozat";

        for(int i = 5; i < headers.length; i++) {
            headers[i] = String.valueOf(i - 5);
        }

        table.setModel(new DefaultTableModel(new Object[0][0], headers));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setEnabled(false);

        for(int i = 0; i < tableData.length; i++) {
            tableData[i] = new TableEntry(true, false, 0);
        }

        updateJtable();
    }

    @Override
    public void defaultState(int[] values, boolean[] deleted, boolean[] empty) {
        jtableData.clear();

        if(values.length != deleted.length || deleted.length != empty.length) {
            return;
        }

        for(int i = 0; i < Math.min(values.length, tableData.length); i++) {
            tableData[i] = new TableEntry(empty[i], deleted[i], values[i]);
        }

        Object[] data = new Object[4 + tableSize];
        data[0] = "";
        data[1] = "";
        data[2] = "";
        data[3] = "";
        data[4] = "";

        for (int j = 5; j < data.length; j++) {
            TableEntry entry = tableData[j - 5];
            data[j] = entry.toString();
        }

        jtableData.add(data);
        updateJtable();
    }

    @Override
    public int getTableSize() {
        return tableSize;
    }

    @Override
    public String getInformation() {
        return "m = " + tableSize + ", " +
                "h₁(k) = " + equationH1 + ", " +
                "h₂(k) = " + equationH2 + ", " +
                "h(k, i) = (h₁(k) + i * h₂(k)) % m";
    }

    @Override
    public JTable getTable() {
        return table;
    }

    @Override
    public void handle(Operation operation, int k) {
        Row row;

        switch (operation) {
            case INSERT:
                row = insert(k);
                break;
            case DELETE:
                row = delete(k);
                break;
            case SEARCH:
                row = search(k);
                break;

            default:
                row = null;
        }

        if(row != null) {
            Object[] data = new Object[5 + tableSize];
            data[0] = row.operation;
            data[1] = row.k;
            data[2] = row.h1;
            data[3] = row.h2;
            data[4] = row.checks;

            for (int i = 5; i < data.length; i++) {
                TableEntry entry = tableData[i - 5];
                data[i] = entry.toString();
            }

            jtableData.add(data);
        }

        updateJtable();

        for(TableEntry entry : tableData) {
            entry.marked = false;
        }
    }


    private Row search(int k) {
        StringBuilder checks = new StringBuilder();

        int h1 = h1(k);
        int h2 = h2(k);

        int i = 0;
        int j = performHashing(h1, h2, i);
        int j0 = j;

        checks.append(j).append(", ");

        while(i < tableSize && !(tableData[j].empty || tableData[j].deleted)) {
            if(tableData[j].data == k) {
                tableData[j].marked = true;
                appendFinal(checks, "✔");
                return new Row(Operation.SEARCH, k, h1, h2, checks.toString());
            } else {
                ++i;
                j = performHashing(h1, h2, i);
                checks.append(j).append(", ");
            }
        }

        int ide;

        if(i == tableSize) {
            appendFinal(checks, "❌");
            return new Row(Operation.SEARCH, k, h1, h2, checks.toString());
        } else {
            ide = j;
        }

        while(i < tableSize && !tableData[j].empty) {
            if(tableData[j].data == k) {
                tableData[j].marked = true;
                appendFinal(checks, "✔");
                return new Row(Operation.SEARCH, k, h1, h2, checks.toString());
            } else {
                ++i;
                j = performHashing(h1, h2, i);
                checks.append(j).append(", ");
            }
        }

        tableData[j].marked = true;
        appendFinal(checks, "❌");
        return new Row(Operation.SEARCH, k, h1, h2, checks.toString());
    }

    private Row delete(int k) {
        StringBuilder checks = new StringBuilder();

        int h1 = h1(k);
        int h2 = h2(k);

        int i = 0;
        int j = performHashing(h1, h2, i);
        int j0 = j;

        checks.append(j).append(", ");

        while(i < tableSize && !(tableData[j].empty || tableData[j].deleted)) {
            if(tableData[j].data == k) {
                tableData[j].deleted = true;
                tableData[j].marked = true;
                appendFinal(checks, "✔");
                return new Row(Operation.DELETE, k, h1, h2, checks.toString());
            } else {
                ++i;
                j = performHashing(h1, h2, i);
                checks.append(j).append(", ");
            }
        }

        int ide;

        if(i == tableSize) {
            appendFinal(checks, "❌");
            return new Row(Operation.DELETE, k, h1, h2, checks.toString());
        } else {
            ide = j;
        }

        while(i < tableSize && !tableData[j].empty) {
            if(tableData[j].data == k) {
                tableData[j].deleted = true;
                tableData[j].marked = true;
                appendFinal(checks, "✔");
                return new Row(Operation.DELETE, k, h1, h2, checks.toString());
            } else {
                ++i;
                j = performHashing(h1, h2, i);
                checks.append(j).append(", ");
            }
        }

        tableData[j].marked = true;
        appendFinal(checks, "❌");
        return new Row(Operation.DELETE, k, h1, h2, checks.toString());
    }

    private Row insert(int k) {
        StringBuilder checks = new StringBuilder();

        int h1 = h1(k);
        int h2 = h2(k);

        int i = 0;
        int j = performHashing(h1, h2, i);
        int j0 = j;

        checks.append(j0).append(", ");

        while(i < tableSize && !(tableData[j].empty || tableData[j].deleted)) {
            if(tableData[j].data == k) {
                appendFinal(checks, "❌");
                return new Row(Operation.INSERT, k, h1, h2, checks.toString());
            } else {
                ++i;
                j = performHashing(h1, h2, i);
                checks.append(j).append(", ");
            }
        }

        int ide;

        if(i == tableSize) {
            appendFinal(checks, "❌");
            return new Row(Operation.INSERT, k, h1, h2, checks.toString());
        } else {
            ide = j;
        }

        while(i < tableSize && !tableData[j].empty) {
            if(tableData[j].data == k) {
                appendFinal(checks, "❌");
                return new Row(Operation.INSERT, k, h1, h2, checks.toString());
            } else {
                ++i;
                j = performHashing(h1, h2, i);
                checks.append(j).append(", ");
            }
        }

        tableData[ide] = new TableEntry(false, false, k);
        tableData[ide].marked = true;

        appendFinal(checks, "✔");
        return new Row(Operation.INSERT, k, h1, h2, checks.toString());
    }

    @Override
    public String getName() {
        return "Kettős hasítás";
    }

    private void updateJtable() {
        Object[][] data = new Object[jtableData.size()][];
        for(int i = 0; i < data.length; i++) {
            data[i] = jtableData.get(i);
        }

        ((DefaultTableModel) table.getModel()).setDataVector(data, headers);
        table.revalidate();
    }

    private void appendFinal(StringBuilder stringBuilder, String s) {
        if(stringBuilder.length() > 2) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }

        stringBuilder.append(" ").append(s);
    }

    private int h1(int k) {
        PolishNotation polishNotation = new PolishNotation();
        String transformed = polishNotation.convertToPolishNotation0(equationH1);

        Map<String, Double> variables = new HashMap<>();
        variables.put("k", (double) k);

        return (int) polishNotation.evaluateExpression(transformed, variables);
    }

    private int h2(int k) {
        PolishNotation polishNotation = new PolishNotation();
        String transformed = polishNotation.convertToPolishNotation0(equationH2);

        Map<String, Double> variables = new HashMap<>();
        variables.put("k", (double) k);

        return (int) polishNotation.evaluateExpression(transformed, variables);
    }

    private int performHashing(int h1, int h2, int i) {
        return (h1 + i * h2) % tableSize;
    }

    private static class TableEntry {
        boolean empty = true;
        boolean deleted = false;
        int data = 0;

        boolean marked = false;

        public TableEntry(boolean empty, boolean deleted, int data) {
            this.empty = empty;
            this.deleted = deleted;
            this.data = data;
        }

        @Override
        public String toString() {
            String s = deleted ? "D" : (empty ? "" : String.valueOf(data));

            if(marked) {
                return "[ " + s + " ]";
            } else {
                return s;
            }
        }
    }

    private static class Row {
        public Operation operation;
        public int k;
        public int h1;
        public int h2;
        public String checks;

        public Row(Operation operation, int k, int h1, int h2, String checks) {
            this.operation = operation;
            this.k = k;
            this.h1 = h1;
            this.h2 = h2;
            this.checks = checks;
        }
    }
}
