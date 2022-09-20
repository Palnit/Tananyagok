package jp.tengopapa.algo.solvers.hash;

import jp.tengopapa.algo.solvers.PolishNotation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SquareHandler implements HashTableHandler {
    private int tableSize = 8;

    private double c1 = 0.5d;
    private double c2 = 0.5d;

    private final TableEntry[] tableData = new TableEntry[tableSize];
    private final List<Object[]> jtableData = new ArrayList<>();

    private JTable table;
    private String[] headers;

    @Override
    public boolean needsConfig() {
        return true;
    }

    private int readInt(JFrame frame, String message) {
        return (int) readDouble(frame, message);
    }

    private double readDouble(JFrame frame, String message) {
        double val = 0;

        while(val <= 0) {
            String v = JOptionPane.showInputDialog(frame, message, frame.getTitle(), JOptionPane.QUESTION_MESSAGE);

            try {
                val = Double.parseDouble(v);
            } catch (Exception ignored) { }
        }

        return val;
    }

    @Override
    public void showConfigPanel(JFrame frame) {
        this.tableSize = readInt(frame, "m =");
        c1 = readDouble(frame, "c₁ =");
        c2 = readDouble(frame, "c₂ =");

        table = new JTable();
        table.setShowGrid(true);
        headers = new String[4 + tableSize];
        headers[0] = "Művelet";
        headers[1] = "k";
        headers[2] = "h₁(k)";
        headers[3] = "Próbasorozat";

        for(int i = 4; i < headers.length; i++) {
            headers[i] = String.valueOf(i - 4);
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

        for (int j = 4; j < data.length; j++) {
            TableEntry entry = tableData[j - 4];
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
                "c₁ = " + c1 + ", " +
                "c₂ = " + c2 + ", " +
                "h₁(k, i) = (k + c₁ * i + c₂ * i²) % m";
    }

    @Override
    public JTable getTable() {
        return table;
    }

    @Override
    public void handle(Operation operation, int k) {
        Row row = null;

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
            Object[] data = new Object[4 + tableSize];
            data[0] = row.operation;
            data[1] = row.k;
            data[2] = row.h1;
            data[3] = row.checks;

            for (int i = 4; i < data.length; i++) {
                TableEntry entry = tableData[i - 4];
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

        int j = performHashing(k, 0);
        int j0 = j;
        int i = 0;

        checks.append(j).append(", ");

        while(i < tableSize && !(tableData[j].empty || tableData[j].deleted)) {
            if(tableData[j].data == k) {
                tableData[j].marked = true;
                appendFinal(checks, "✔");
                return new Row(Operation.SEARCH, k, j0, checks.toString());
            } else {
                ++i;
                j = performHashing(k, i);
                checks.append(j).append(", ");
            }
        }

        int ide;

        if(i == tableSize) {
            appendFinal(checks, "❌");
            return new Row(Operation.SEARCH, k, j0, checks.toString());
        } else {
            ide = j;
        }

        while(i < tableSize && !tableData[j].empty) {
            if(tableData[j].data == k) {
                tableData[j].marked = true;
                appendFinal(checks, "✔");
                return new Row(Operation.SEARCH, k, j0, checks.toString());
            } else {
                ++i;
                j = performHashing(k, i);
                checks.append(j).append(", ");
            }
        }

        tableData[j].marked = true;
        appendFinal(checks, "❌");
        return new Row(Operation.SEARCH, k, j0, checks.toString());
    }

    private Row delete(int k) {
        StringBuilder checks = new StringBuilder();

        int j = performHashing(k, 0);
        int j0 = j;
        int i = 0;

        checks.append(j).append(", ");

        while(i < tableSize && !(tableData[j].empty || tableData[j].deleted)) {
            if(tableData[j].data == k) {
                tableData[j].deleted = true;
                tableData[j].marked = true;
                appendFinal(checks, "✔");
                return new Row(Operation.DELETE, k, j0, checks.toString());
            } else {
                ++i;
                j = performHashing(k, i);
                checks.append(j).append(", ");
            }
        }

        int ide;

        if(i == tableSize) {
            appendFinal(checks, "❌");
            return new Row(Operation.DELETE, k, j0, checks.toString());
        } else {
            ide = j;
        }

        while(i < tableSize && !tableData[j].empty) {
            if(tableData[j].data == k) {
                tableData[j].deleted = true;
                tableData[j].marked = true;
                appendFinal(checks, "✔");
                return new Row(Operation.DELETE, k, j0, checks.toString());
            } else {
                ++i;
                j = performHashing(k, i);
                checks.append(j).append(", ");
            }
        }

        tableData[j].marked = true;
        appendFinal(checks, "❌");
        return new Row(Operation.DELETE, k, j0, checks.toString());
    }

    private Row insert(int k) {
        StringBuilder checks = new StringBuilder();

        int i = 0;
        int j = performHashing(k, i);
        int j0 = j;

        checks.append(j).append(", ");

        while(i < tableSize && !(tableData[j].empty || tableData[j].deleted)) {
            if(tableData[j].data == k) {
                appendFinal(checks, "❌");
                return new Row(Operation.INSERT, k, j0, checks.toString());
            } else {
                ++i;
                j = performHashing(k, i);
                checks.append(j).append(", ");
            }
        }

        int ide;

        if(i == tableSize) {
            appendFinal(checks, "❌");
            return new Row(Operation.INSERT, k, j0, checks.toString());
        } else {
            ide = j;
        }

        while(i < tableSize && !tableData[j].empty) {
            if(tableData[j].data == k) {
                appendFinal(checks, "❌");
                return new Row(Operation.INSERT, k, j0, checks.toString());
            } else {
                ++i;
                j = performHashing(k, i);
                checks.append(j).append(", ");
            }
        }

        tableData[ide] = new TableEntry(false, false, k);
        tableData[ide].marked = true;

        appendFinal(checks, "✔");
        return new Row(Operation.INSERT, k, j0, checks.toString());
    }

    private void updateJtable() {
        Object[][] data = new Object[jtableData.size()][];
        for(int i = 0; i < data.length; i++) {
            data[i] = jtableData.get(i);
        }

        ((DefaultTableModel) table.getModel()).setDataVector(data, headers);
        table.revalidate();
    }

    private int performHashing(int k, int i) {
        String equation = "(k + c1 * i + c2 * i^2) % m";

        PolishNotation polishNotation = new PolishNotation();
        String transformedEquation = polishNotation.convertToPolishNotation0(equation);

        HashMap<String, Double> variables = new HashMap<>();
        variables.put("k", (double) k);
        variables.put("i", (double) i);
        variables.put("c1", c1);
        variables.put("c2", c2);
        variables.put("m", (double) tableSize);

        return (int) polishNotation.evaluateExpression(transformedEquation, variables);
    }

    private void appendFinal(StringBuilder stringBuilder, String s) {
        if(stringBuilder.length() > 2) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }

        stringBuilder.append(" ").append(s);
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
        public String checks;

        public Row(Operation operation, int k, int h1, String checks) {
            this.operation = operation;
            this.k = k;
            this.h1 = h1;
            this.checks = checks;
        }
    }

    @Override
    public String getName() {
        return "Négyzetes próba";
    }
}
