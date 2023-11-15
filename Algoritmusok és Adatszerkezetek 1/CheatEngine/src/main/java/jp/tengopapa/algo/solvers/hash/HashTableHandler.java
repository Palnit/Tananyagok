package jp.tengopapa.algo.solvers.hash;

import javax.swing.*;

public interface HashTableHandler {
    boolean needsConfig();
    void showConfigPanel(JFrame frame);
    JTable getTable();
    void handle(Operation operation, int k);
    String getName();
    String getInformation();
    void defaultState(int[] values, boolean[] deleted, boolean[] empty);
    int getTableSize();

    enum Operation {
        INSERT("Beszúr"), DELETE("Töröl"), SEARCH("Keres");

        private final String name;

        Operation(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
