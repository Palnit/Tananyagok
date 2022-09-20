package jp.tengopapa.algo.solvers.hash;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

public class HashTable {
    private final JFrame frame;
    private final JPanel panel;
    private final JPanel tablePanel;

    private final JLabel temporaryLabel = new JLabel("❌");

    private HashTableHandler hashTableHandler = null;

    public HashTable(JFrame frame) {
        this.frame = frame;
        panel = new JPanel(new BorderLayout());
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("❌"));

        temporaryLabel.setVerticalTextPosition(JLabel.CENTER);
        temporaryLabel.setHorizontalTextPosition(JLabel.CENTER);
        temporaryLabel.setVerticalAlignment(JLabel.CENTER);
        temporaryLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel infoLabel = new JLabel("Használt értékek: ❌");
        tablePanel.add(infoLabel, BorderLayout.PAGE_START);

        tablePanel.add(temporaryLabel, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.CENTER);

        JPanel operationPanel = new JPanel(new GridBagLayout());

        {
            JComboBox<HashTableHandler.Operation> operationJComboBox = new JComboBox<>(HashTableHandler.Operation.values());
            operationPanel.add(operationJComboBox, new GridBagConstraints(0, 0, 1, 1, 0., 0., GridBagConstraints.BASELINE, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            JTextField valueTextField = new JTextField();
            operationPanel.add(valueTextField, new GridBagConstraints(1, 0, 2, 1, 1., 0., GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            JButton operationButton = new JButton("Mehet");
            operationPanel.add(operationButton, new GridBagConstraints(3, 0, 1, 1, 0., 0., GridBagConstraints.BASELINE, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            operationButton.addActionListener((e) -> {
                HashTableHandler.Operation operation = (HashTableHandler.Operation) operationJComboBox.getSelectedItem();
                int value = Integer.parseInt(valueTextField.getText());

                if(hashTableHandler != null) {
                    hashTableHandler.handle(operation, value);
                }
            });
        }

        operationPanel.setBorder(BorderFactory.createTitledBorder("Műveletek"));
        panel.add(operationPanel, BorderLayout.PAGE_END);

        JPanel setupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        {
            setupPanel.setBorder(BorderFactory.createTitledBorder("Beállítások"));

            JButton typeButton = new JButton("Tábla típusa");
            setupPanel.add(typeButton);
            typeButton.addActionListener((e) -> changeTableType());

            JButton defaultStateButton = new JButton("Tábla alaphelyzete");
            setupPanel.add(defaultStateButton);
            defaultStateButton.addActionListener((e) -> {
                setDefaultState();
            });
        }

        panel.add(setupPanel, BorderLayout.PAGE_START);
    }

    private void setDefaultState() {
        if(hashTableHandler == null) {
            return;
        }

        int tableSize = hashTableHandler.getTableSize();

        int[] values = new int[tableSize];
        boolean[] deleted = new boolean[tableSize];
        boolean[] empty = new boolean[tableSize];
        String[] headers = new String[tableSize];

        for(int i = 0; i < tableSize; i++) {
            values[i] = 0;
            deleted[i] = false;
            empty[i] = true;
            headers[i] = String.valueOf(i);
        }

        JDialog dialog = new JDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);

        JTable table = new JTable(1, tableSize);
        table.setShowGrid(true);
        table.setModel(new DefaultTableModel(new Object[0][0], headers));
        JScrollPane jScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setPreferredSize(new Dimension(tableSize * 50, table.getRowHeight() * 3));
        dialog.add(jScrollPane, BorderLayout.PAGE_START);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Mégse");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        ((DefaultTableModel) (table.getModel())).setDataVector(new Object[][] { new Object[tableSize] }, headers);
        dialog.add(buttonPanel, BorderLayout.PAGE_END);

        cancelButton.addActionListener((e) -> dialog.setVisible(false));
        okButton.addActionListener((e) -> {
            Vector<?> data = ((DefaultTableModel) table.getModel()).getDataVector();
            Vector<?> row = (Vector<?>) data.get(0);

            for(int i = 0; i < row.size(); i++) {
                Object o = row.get(i);

                if(o != null) {
                    if(o.equals("D") || o.equals("d")) {
                        deleted[i] = true;
                        empty[i] = false;
                    } else {
                        try {
                            int v = Integer.parseInt(String.valueOf(o));
                            values[i] = v;
                            empty[i] = false;
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            dialog.setVisible(false);
            hashTableHandler.defaultState(values, deleted, empty);
        });

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void changeTableType() {
        Object[] options = TableType.values();
        Object choice = JOptionPane.showInputDialog(frame, "Válassz típust:", frame.getTitle(), JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if((choice instanceof TableType)) {
            TableType tableType = (TableType) choice;

            if(tableType.impl != null) {
                try {
                    hashTableHandler = tableType.impl.getConstructor().newInstance();
                } catch (Exception e) {
                    hashTableHandler = null;
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Nincs megfelelő implementáció ehhez a típushoz!", frame.getTitle(), JOptionPane.ERROR_MESSAGE);
                hashTableHandler = null;
            }

            handlerChanged();
        }
    }

    private void handlerChanged() {
        BorderLayout layout = (BorderLayout) tablePanel.getLayout();
        tablePanel.remove(layout.getLayoutComponent(BorderLayout.CENTER));

        JLabel label = (JLabel) layout.getLayoutComponent(BorderLayout.PAGE_START);

        if(hashTableHandler == null) {
            tablePanel.add(temporaryLabel, BorderLayout.CENTER);
            tablePanel.setBorder(BorderFactory.createTitledBorder("❌"));
            label.setText("Használt értékek: ❌");
            tablePanel.revalidate();
            return;
        }

        if(hashTableHandler.needsConfig()) {
            hashTableHandler.showConfigPanel(frame);
        }

        JTable table = hashTableHandler.getTable();
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Hasítótábla - " + hashTableHandler.getName()));
        label.setText("Használt értékek: " + hashTableHandler.getInformation());
        tablePanel.revalidate();
    }

    public JPanel getPanel() {
        return panel;
    }
}
