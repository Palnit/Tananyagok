package jp.tengopapa.algo;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import jp.tengopapa.algo.data.TreeContainer;
import jp.tengopapa.algo.data.TreeNode;
import jp.tengopapa.algo.solvers.*;
import jp.tengopapa.algo.solvers.hash.HashTable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        new Main().run();
    }

    private JFrame mainFrame;

    private void run() {
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);

            FlatMaterialDarkerIJTheme.setup();
            UIManager.put("CheckBox.icon.style", "filled");
            UIManager.put("Table.alternateRowColor", new Color(0x282828));
            UIManager.put("Table.showHorizontalLines", false);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("Component.arrowType", "chevron");
            UIManager.put("ScrollBar.showButtons", true);

            mainFrame = new JFrame("Ásványkincsek Cheat Engine 1.18.2");
            mainFrame.setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("icon.png"))).getImage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to style window!");
        }

        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout(0, 0));

        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));

        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.LEFT);
        tabbedPane.setMinimumSize(new Dimension(800, 500));
        tabbedPane.setPreferredSize(new Dimension(800, 500));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        constructTabs(tabbedPane);
        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        JLabel aboutLabel = new JLabel("\"Bármilyen segédanyag használható\" - Varga Henrik Zoltán");
        aboutLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        aboutLabel.setHorizontalAlignment(JLabel.RIGHT);
        aboutLabel.setForeground(Color.GRAY);
        aboutLabel.setFont(aboutLabel.getFont().deriveFont(Font.ITALIC));
        rootPanel.add(aboutLabel, BorderLayout.PAGE_END);

        mainFrame.add(rootPanel, BorderLayout.CENTER);
        mainFrame.setPreferredSize(tabbedPane.getPreferredSize());
        mainFrame.setMinimumSize(tabbedPane.getMinimumSize());
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setAlwaysOnTop(true);
        mainFrame.setVisible(true);
    }

    private void constructTabs(JTabbedPane tabbedPane) {
        addTab(tabbedPane, "Lengyel forma", "tabs/polish.png", polishNotationTab());
        addTab(tabbedPane, "Infix forma", "tabs/infix.png", infixNotationTab());
        addTab(tabbedPane, "Egyszerű rendezések", "tabs/ssort.png", simpleSortsTab());
        addTab(tabbedPane, "Összefésülő rendezés", "tabs/merge.png", mergeSortTab());
        addTab(tabbedPane, "Quicksort", "tabs/quick.png", quickSortTab());
        addTab(tabbedPane, "Radix rendezés", "tabs/radix.png", radixSortTab());
        addTab(tabbedPane, "Leszámláló rendezés", "tabs/counting.png", countingSortTab());
        addTab(tabbedPane, "Edényrendezés", "tabs/edeny.png", bucketSortTab());
        addTab(tabbedPane, "Fa bejárása / építése", "tabs/tree.png", binaryTreeTab());
        addTab(tabbedPane, "Hasítótábla", "tabs/hash.png", new HashTable(mainFrame).getPanel());
        addTab(tabbedPane, "Kupacműveletek", "tabs/heap.png", heapTab());

        addTab(tabbedPane, "Heapsort", "", new JPanel());
    }

    private void addTab(JTabbedPane tabbedPane, String title, String imagePath, Component component) {
        if (imagePath.length() == 0) {
            imagePath = "tabs/unk.png";
        }

        try {
            tabbedPane.addTab(title, new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(imagePath))), component);
        } catch (Exception e) {
            e.printStackTrace();

            System.err.println("Failed to construct " + title + " tab!");
        }
    }

    private JPanel heapTab() {
        JPanel panel = new JPanel(new BorderLayout());

        TreeContainer<Integer> treeContainer = new TreeContainer<>(new TreeNode<>(0, null, null, null));

        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.setBorder(BorderFactory.createTitledBorder("Ábrázolás"));
        treePanel.add(treeContainer.getPane(), BorderLayout.CENTER);

        panel.add(treePanel, BorderLayout.CENTER);

        JPanel pageEndPanel = new JPanel(new BorderLayout());

        JPanel operationsPanel = new JPanel(new GridBagLayout());

        {
            JTextField numberField = new JTextField();
            JButton addButton = new JButton("Hozzáadás");
            JButton removeButton = new JButton("Max. törlése");

            addButton.addActionListener((e) -> {
                String levelOrder = treeContainer.getJungRoot().walkLevelOrder();
                int[] asArray = parseInts(levelOrder);
                int[] asArrayAdded = new int[asArray.length + 1];
                System.arraycopy(asArray, 0, asArrayAdded, 0, asArray.length);

                int addedNumber = Integer.parseInt(numberField.getText());
                asArrayAdded[asArrayAdded.length - 1] = addedNumber;

                TreeNode<Integer> root = buildHeapTree(asArrayAdded);
                TreeNode<Integer> newlyAdded = root.search(addedNumber);

                if(newlyAdded == null) {
                    return;
                }

                while (newlyAdded.parent() != null && newlyAdded.parent().data < newlyAdded.data) {
                    TreeNode<Integer> parent = newlyAdded.parent();
                    int pv = parent.data;
                    parent.data = newlyAdded.data;
                    newlyAdded.data = pv;
                    newlyAdded = parent;
                }

                newlyAdded.marked(true);

                treeContainer.treeChanged(root);
            });

            removeButton.addActionListener((e) -> {
                String levelOrder = treeContainer.getJungRoot().walkLevelOrder();
                int[] asArray = parseInts(levelOrder);
                int[] asArrayDeleted = new int[asArray.length - 1];

                System.arraycopy(asArray, 0, asArrayDeleted, 0, asArrayDeleted.length);
                asArrayDeleted[0] = asArray[asArray.length - 1];

                TreeNode<Integer> root = buildHeapTree(asArrayDeleted);
                sink(root);

                treeContainer.treeChanged(root);
            });

            JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
            separator.setPreferredSize(new Dimension(separator.getPreferredSize().width, (int) removeButton.getPreferredSize().getHeight()));
            operationsPanel.add(numberField, new GridBagConstraints(0, 0, 1, 1, 1., 1., GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            operationsPanel.add(addButton, new GridBagConstraints(1, 0, 1, 1, 0., 1., GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            operationsPanel.add(separator, new GridBagConstraints(2, 0, 1, 1, 0., 1., GridBagConstraints.BASELINE, GridBagConstraints.VERTICAL, new Insets(0, 5, 0, 5), 0, 0));
            operationsPanel.add(removeButton, new GridBagConstraints(3, 0, 1, 1, 0., 1., GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        }

        operationsPanel.setBorder(BorderFactory.createTitledBorder("Műveletek"));
        pageEndPanel.add(operationsPanel, BorderLayout.PAGE_START);

        JPanel arrayPanel = new JPanel(new GridBagLayout());

        {
            JTextField arrayRepresentationField = new JTextField();
            JButton rebuildButton = new JButton("Felépítés");
            JButton calculateButton = new JButton("Frissítés");

            calculateButton.addActionListener((e) -> {
                String levelOrderTraversal = treeContainer.getJungRoot().walkLevelOrder();
                arrayRepresentationField.setText(levelOrderTraversal);
            });

            rebuildButton.addActionListener((e) -> {
                int[] values = readUserInt(arrayRepresentationField);

                if (values.length == 0) {
                    return;
                }

                TreeNode<Integer> root = buildHeapTree(values);
                treeContainer.treeChanged(root);
            });

            arrayPanel.add(arrayRepresentationField, new GridBagConstraints(0, 0, 2, 1, 1., 1., GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            arrayPanel.add(rebuildButton, new GridBagConstraints(2, 0, 1, 1, 0., 0., GridBagConstraints.BASELINE, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            arrayPanel.add(calculateButton, new GridBagConstraints(3, 0, 1, 1, 0., 0., GridBagConstraints.BASELINE, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        }

        arrayPanel.setBorder(BorderFactory.createTitledBorder("Halmaz-reprezentáció"));
        pageEndPanel.add(arrayPanel, BorderLayout.PAGE_END);

        panel.add(pageEndPanel, BorderLayout.PAGE_END);

        return panel;
    }

    private void sink(TreeNode<Integer> root) {
        TreeNode<Integer> left = root.left();
        TreeNode<Integer> right = root.right();

        TreeNode<Integer> toSwapWith = null;
        if (left != null && right != null) {
            TreeNode<Integer> biggestChild = (left.data > right.data ? left : right);
            if (biggestChild.data > root.data) {
                toSwapWith = biggestChild;
            }
        } else if (left != null && left.data > root.data) {
            toSwapWith = left;
        } else if (right != null && right.data > root.data) {
            toSwapWith = right;
        }

        if (toSwapWith != null) {
            int sv = toSwapWith.data;
            toSwapWith.data = root.data;
            root.data = sv;

            sink(toSwapWith);
        }
    }

    private TreeNode<Integer> buildHeapTree(int[] values) {
        TreeNode<Integer> root = new TreeNode<>(values[0], null, null, null);
        Queue<TreeNode<Integer>> q = new LinkedList<>();

        q.add(root);
        for (int i = 1; i < values.length; i++) {
            insertValue(q, root, values[i]);
        }

        return root;
    }

    private void insertValue(Queue<TreeNode<Integer>> q, TreeNode<Integer> root, int k) {
        TreeNode<Integer> node = new TreeNode<>(k, null, null, null);

        TreeNode<Integer> peek = q.peek();
        if (peek != null) {
            if (peek.left() == null) {
                peek.left(node);
            } else {
                peek.right(node);
                q.remove();
            }

            node.parent(peek);
        }

        q.add(node);
    }

    private JPanel binaryTreeTab() {
        JPanel panel = new JPanel(new BorderLayout());

        TreeNode<String> root = new TreeNode<>("A", null, null, null);
        TreeContainer<String> treeContainer = new TreeContainer<>(root);

        treeContainer.setMouseHandler(((e, s) -> {
            JPopupMenu menu = new JPopupMenu("Dikaz");

            JMenuItem header = new JMenuItem(s + (treeContainer.getJungRoot().equals(s) ? " (gyökér)" : ""));
            header.setEnabled(false);

            menu.add(header);
            menu.addSeparator();

            JMenuItem changeValue = new JMenuItem("Érték módosítása");
            menu.add(changeValue);

            if (s.left() == null) {
                JMenuItem addLeft = new JMenuItem("Bal hozzáadása");
                menu.add(addLeft);

                addLeft.addActionListener((e1) -> {
                    String data = JOptionPane.showInputDialog(mainFrame, "Érték:");
                    s.left(new TreeNode<>(data, null, null, s));
                    treeContainer.treeChanged(treeContainer.getJungRoot());
                });
            }

            if (s.right() == null) {
                JMenuItem addRight = new JMenuItem("Jobb hozzáadása");
                menu.add(addRight);

                addRight.addActionListener((e1) -> {
                    String data = JOptionPane.showInputDialog(mainFrame, "Érték:");
                    s.right(new TreeNode<>(data, null, null, s));
                    treeContainer.treeChanged(treeContainer.getJungRoot());
                });
            }

            if (!treeContainer.getJungRoot().equals(s)) {
                JMenuItem delete = new JMenuItem("Törlés");
                menu.add(delete);

                delete.addActionListener((e1) -> {
                    TreeNode<String> parent = s.parent();

                    if (s.equals(parent.left())) {
                        parent.left(null);
                    } else {
                        parent.right(null);
                    }

                    treeContainer.treeChanged(treeContainer.getJungRoot());
                });
            }

            changeValue.addActionListener((e1) -> {
                s.data = JOptionPane.showInputDialog(mainFrame, "Érték:");

                treeContainer.treeChanged(treeContainer.getJungRoot());
            });

            menu.show(e.getComponent(), e.getX(), e.getY());
        }));

        GraphZoomScrollPane pane = treeContainer.getPane();
        pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Felépítés"), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        panel.add(pane, BorderLayout.CENTER);

        JPanel traversalPane = new JPanel(new GridBagLayout());
        traversalPane.setBorder(BorderFactory.createTitledBorder("Fa bejárása"));

        {
            JPanel inorderPanel = new JPanel(new BorderLayout());
            JTextField inorderField = new JTextField();
            inorderPanel.add(inorderField, BorderLayout.CENTER);
            inorderPanel.setBorder(BorderFactory.createTitledBorder("Inorder"));

            traversalPane.add(inorderPanel, new GridBagConstraints(0, 0, 1, 1, 1., 0., GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            JPanel preorderPanel = new JPanel(new BorderLayout());
            JTextField preorderField = new JTextField();
            preorderPanel.add(preorderField, BorderLayout.CENTER);
            preorderPanel.setBorder(BorderFactory.createTitledBorder("Preorder"));

            traversalPane.add(preorderPanel, new GridBagConstraints(1, 0, 1, 1, 1., 0., GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            JPanel postorderPanel = new JPanel(new BorderLayout());
            JTextField postorderField = new JTextField();
            postorderPanel.add(postorderField, BorderLayout.CENTER);
            postorderPanel.setBorder(BorderFactory.createTitledBorder("Postorder"));

            traversalPane.add(postorderPanel, new GridBagConstraints(0, 1, 1, 1, 1., 0., GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            JPanel levelOrderPanel = new JPanel(new BorderLayout());
            JTextField levelOrderField = new JTextField();
            levelOrderPanel.add(levelOrderField, BorderLayout.CENTER);
            levelOrderPanel.setBorder(BorderFactory.createTitledBorder("Szintfolytonos"));

            traversalPane.add(levelOrderPanel, new GridBagConstraints(1, 1, 1, 1, 1., 0., GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            JPanel buttonsPanel = new JPanel(new GridBagLayout());

            JButton goButton = new JButton("Bejárás");
            JButton buildButton = new JButton("Visszafejtés");
            JButton defaultButton = new JButton("Alaphelyzet");

            buttonsPanel.add(goButton, new GridBagConstraints(0, 0, 1, 1, 0., 1., GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 0, 3, 0), 0, 0));
            buttonsPanel.add(buildButton, new GridBagConstraints(0, 1, 1, 1, 0., 1., GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 0, 3, 0), 0, 0));
            buttonsPanel.add(defaultButton, new GridBagConstraints(0, 2, 1, 1, 0., 1., GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 0, 3, 0), 0, 0));

            traversalPane.add(buttonsPanel, new GridBagConstraints(2, 0, 1, 2, 0., 1., GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

            goButton.addActionListener((e) -> {
                inorderField.setText(root.walkInorder());
                preorderField.setText(root.walkPreorder());
                postorderField.setText(root.walkPostorder());
                levelOrderField.setText(root.walkLevelOrder());
            });

            buildButton.addActionListener((e) -> {
                TreeNode<String> newRoot = TreeRebuilder.tryRebuild(inorderField.getText(), preorderField.getText(), postorderField.getText(), levelOrderField.getText());

                if (newRoot == null) {
                    showErrorDialog("Nem sikerült a fa visszaépítése!");
                    return;
                }

                treeContainer.treeChanged(newRoot);
            });

            defaultButton.addActionListener((e) -> {
                root.data = "A";
                root.left(null);
                root.right(null);

                treeContainer.treeChanged(root);
            });
        }

        panel.add(traversalPane, BorderLayout.PAGE_END);

        return panel;
    }

    private PolishNotationHandler polishNotationHandler0;

    private JPanel bucketSortTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextField arrayField = new JTextField("0.16, 0.32, 0.96, 0.36, 0.78, 0.33, 0.57, 0.88, 0.99, 0.31");
        JButton goButton = new JButton("Mehet");
        {
            JPanel inputWrapper = new JPanel(new GridBagLayout());
            inputWrapper.setBorder(BorderFactory.createTitledBorder("Bemenet"));
            inputWrapper.add(arrayField, new GridBagConstraints(0, 0, 1, 1, 1., 1., GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            inputWrapper.add(goButton, new GridBagConstraints(1, 0, 1, 1, 0., 0., GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            panel.add(inputWrapper, BorderLayout.PAGE_START);
        }

        String[] columnHeaders = new String[]{"×", "Beszúrás után", "Rendezés után"};
        JTable table = new JTable();
        table.setShowGrid(true);
        {
            table.setModel(new DefaultTableModel(new Object[0][0], columnHeaders));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.setEnabled(false);
            JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            JPanel centerWrapper = new JPanel(new BorderLayout());
            centerWrapper.setBorder(BorderFactory.createTitledBorder("Lejátszás"));
            centerWrapper.add(scrollPane, BorderLayout.CENTER);
            panel.add(centerWrapper, BorderLayout.CENTER);
        }

        JLabel finalLabel = new JLabel("Végeredmény:");
        finalLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panel.add(finalLabel, BorderLayout.PAGE_END);

        goButton.addActionListener((e) -> {
            float[] array = readUserFloat(arrayField);

            List<BucketSort.BucketData> data = BucketSort.bucketSort(array);

            Object[][] vector = new Object[data.size()][3];
            int idx = 0;
            for (BucketSort.BucketData bucketData : data) {
                StringBuilder afterInsertion = new StringBuilder();

                for (List<Float> fl : bucketData.afterInsertion) {
                    afterInsertion.append(fl.toString()).append(" | ");
                }

                if (afterInsertion.length() > 0) {
                    afterInsertion.setLength(afterInsertion.length() - 3);
                } else {
                    afterInsertion.append("[]");
                }

                vector[idx++] = new Object[]{bucketData.key, afterInsertion.toString(), bucketData.afterSorting.toString()};
            }

            ((DefaultTableModel) table.getModel()).setDataVector(vector, columnHeaders);

            finalLabel.setText("Végeredmény: " + Arrays.toString(array));
        });

        return panel;
    }

    private JPanel countingSortTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        {
            JTextField arrayField = new JTextField("012, 231, 301, 031, 131, 223, 331, 111, 312, 030");
            JSpinner keySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 99, 1));
            JSpinner baseSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 10, 1));
            JButton goButton = new JButton("Mehet");

            inputPanel.add(arrayField, new GridBagConstraints(0, 0, 1, 1, 1., 0., GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 5, 0));
            inputPanel.add(new JLabel("kulcs="), new GridBagConstraints(1, 0, 1, 1, 0., 1., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 5, 0));
            inputPanel.add(keySpinner, new GridBagConstraints(2, 0, 1, 1, 0., 0., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 5, 0));
            inputPanel.add(new JLabel("r="), new GridBagConstraints(3, 0, 1, 1, 0., 0., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            inputPanel.add(baseSpinner, new GridBagConstraints(4, 0, 1, 1, 0., 0., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            inputPanel.add(goButton, new GridBagConstraints(5, 0, 1, 1, 0., 0., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 5, 0));

            inputPanel.setBorder(BorderFactory.createTitledBorder("Bemenet"));

            goButton.addActionListener((e) -> {
                int[] array = readUserInt(arrayField);

                int[] sorted = CountingSort.countingSort(array, (int) baseSpinner.getValue(), (int) keySpinner.getValue());
                JOptionPane.showMessageDialog(mainFrame, Arrays.toString(sorted), mainFrame.getTitle(), JOptionPane.INFORMATION_MESSAGE);
            });
        }
        panel.add(inputPanel, BorderLayout.PAGE_START);

        try {
            JPanel p0 = new JPanel(new GridBagLayout());

            p0.add(new JPanel() {
                private final BufferedImage grego = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("diq.png")));

                @Override
                public void paint(Graphics g) {
                    g.drawImage(grego, 0, 0, getWidth(), getHeight(), null);
                }
            }, new GridBagConstraints(0, 0, 1, 1, 1., 1., GridBagConstraints.BASELINE, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

            p0.add(new JPanel() {
                private final BufferedImage mineral = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("lilmineral.png")));

                @Override
                public void paint(Graphics g) {
                    g.drawImage(mineral, 0, 0, getWidth(), getHeight(), null);
                }
            }, new GridBagConstraints(1, 0, 1, 1, 1., 1., GridBagConstraints.BASELINE, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

            panel.add(p0, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    private static class RadixSortHandler {
        private List<RadixSort.SortingStep> steps;

        private JLabel stepsLabel;
        private JTable table;
        private int currentStep = 0;

        private void next() {
            currentStep++;

            if (currentStep == steps.size()) {
                currentStep--;
            }

            update();
        }

        private void prev() {
            currentStep--;

            if (currentStep < 0) {
                currentStep = 0;
            }

            update();
        }

        private void update() {
            stepsLabel.setText("(" + (currentStep + 1) + ")");

            RadixSort.SortingStep step = steps.get(currentStep);

            Object[][] rows = new Object[step.buckets.size() + 1][2];

            int idx = 0;
            for (Map.Entry<Integer, List<Integer>> entry : step.buckets.entrySet()) {
                rows[idx++] = new Object[]{String.valueOf(entry.getKey()), entry.getValue().toString()};
            }

            rows[step.buckets.size()] = new Object[]{"L", step.listSoFar.toString()};

            ((DefaultTableModel) table.getModel()).setDataVector(rows, new String[]{"×", "Lista"});
        }
    }

    private RadixSortHandler radixSortHandler;

    private JPanel radixSortTab() {
        JPanel panel = new JPanel(new GridBagLayout());

        JPanel conversionPanel = new JPanel();

        {
            conversionPanel.setLayout(new GridBagLayout());

            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new GridBagLayout());
            inputPanel.setBorder(BorderFactory.createTitledBorder("Bemenet"));

            JTextField inputField = new JTextField("210, 331, 213, 010, 112, 123, 132, 222, 001, 300");
            JSpinner baseInput = new JSpinner(new SpinnerNumberModel(4, 2, 10, 1));
            JSpinner digitsInput = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
            JButton inputButton = new JButton("Mehet");

            {
                inputPanel.add(inputField, new GridBagConstraints(0, 0, 1, 1, 1., 1., GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                inputPanel.add(new JLabel("r="), new GridBagConstraints(1, 0, 1, 1, 0., 0., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                inputPanel.add(baseInput, new GridBagConstraints(2, 0, 1, 1, 0., 0., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                inputPanel.add(new JLabel("d="), new GridBagConstraints(3, 0, 1, 1, 0., 0., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                inputPanel.add(digitsInput, new GridBagConstraints(4, 0, 1, 1, 0., 0., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                inputPanel.add(inputButton, new GridBagConstraints(5, 0, 1, 1, 0., 0., GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            }

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1d;
            gbc.weighty = 0d;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            conversionPanel.add(inputPanel, gbc);

            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.setBorder(BorderFactory.createTitledBorder("Rendezés lépésenként"));

            JPanel stepsPanel = new JPanel(new BorderLayout());
            JPanel visPanel = new JPanel(new BorderLayout());

            JTable table = new JTable();
            table.setShowGrid(true);
            {
                table.setModel(new DefaultTableModel(new Object[0][0], new String[]{"×", "Lista"}));
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                table.setEnabled(false);
                JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                visPanel.add(scrollPane, BorderLayout.CENTER);
            }

            JPanel visPanel1 = new JPanel(new BorderLayout());

            visPanel.add(visPanel1, BorderLayout.PAGE_END);
            stepsPanel.add(visPanel, BorderLayout.CENTER);

            JPanel buttonsPanel = new JPanel(new BorderLayout());

            JButton prevButton = new JButton("<");
            JButton nextButton = new JButton(">");
            JLabel stepLabel = new JLabel("(-)");
            stepLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            stepLabel.setHorizontalAlignment(JLabel.CENTER);
            buttonsPanel.add(prevButton, BorderLayout.LINE_START);
            buttonsPanel.add(nextButton, BorderLayout.LINE_END);
            buttonsPanel.add(stepLabel, BorderLayout.CENTER);
            stepsPanel.add(buttonsPanel, BorderLayout.PAGE_END);

            resultPanel.add(stepsPanel, BorderLayout.CENTER);

            GridBagConstraints gbc1 = new GridBagConstraints();
            gbc1.weightx = 1d;
            gbc1.weighty = 1d;
            gbc1.fill = GridBagConstraints.BOTH;
            gbc1.anchor = GridBagConstraints.NORTH;
            gbc1.gridx = 0;
            gbc1.gridy = 1;
            conversionPanel.add(resultPanel, gbc1);

            inputButton.addActionListener((e) -> {
                int[] array = readUserInt(inputField);

                try {
                    radixSortHandler = new RadixSortHandler();
                    radixSortHandler.stepsLabel = stepLabel;
                    radixSortHandler.steps = RadixSort.radixSort(array, (int) baseInput.getValue(), (int) digitsInput.getValue());
                    radixSortHandler.table = table;

                    radixSortHandler.update();
                } catch (Exception e0) {
                    e0.printStackTrace();

                    showErrorDialog(e0.getClass().getCanonicalName());
                }
            });

            nextButton.addActionListener((e) -> {
                if (radixSortHandler != null) {
                    radixSortHandler.next();
                }
            });

            prevButton.addActionListener((e) -> {
                if (radixSortHandler != null) {
                    radixSortHandler.prev();
                }
            });
        }

        panel.add(conversionPanel, new GridBagConstraints(0, 0, 1, 2, 1d, 1d, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        return panel;
    }

    private JPanel infixNotationTab() {
        JPanel conversionPanel = new JPanel();
        conversionPanel.setBorder(BorderFactory.createTitledBorder("Infix formára alakítás"));

        {
            conversionPanel.setLayout(new GridBagLayout());

            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BorderLayout());

            JTextField inputField = new JTextField("ab*cgh^/+");
            JButton inputButton = new JButton("Mehet");

            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(inputButton, BorderLayout.LINE_END);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1d;
            gbc.weighty = 0d;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            conversionPanel.add(inputPanel, gbc);

            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

            JLabel statsLabel = new JLabel("Összes lépés: -");
            statsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            resultPanel.add(statsLabel, BorderLayout.PAGE_START);

            JPanel stepsPanel = new JPanel(new BorderLayout());

            JPanel visPanel = new JPanel(new BorderLayout());

            JLabel stackLabel = new JLabel("   ");
            stackLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17));
            stackLabel.setHorizontalAlignment(JLabel.CENTER);
            stackLabel.setVerticalAlignment(JLabel.BOTTOM);

            {
                JPanel an2 = new JPanel(new GridBagLayout());

                JPanel an3 = new JPanel(new BorderLayout());
                an3.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 3, 3, 3, Color.BLACK), BorderFactory.createEmptyBorder(25, 25, 3, 25)));
                an3.add(stackLabel, BorderLayout.CENTER);

                GridBagConstraints gbc2 = new GridBagConstraints();
                gbc2.anchor = GridBagConstraints.NORTH;
                gbc2.fill = GridBagConstraints.VERTICAL;
                gbc2.weightx = 0d;
                gbc2.weighty = 1d;
                an2.add(an3, gbc2);

                visPanel.add(an2, BorderLayout.CENTER);
            }

            JPanel visPanel1 = new JPanel(new BorderLayout());

            JLabel an1 = new JLabel("Jelenlegi kimenet:");
            an1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            an1.setHorizontalAlignment(JLabel.CENTER);
            visPanel1.add(an1, BorderLayout.PAGE_START);

            JLabel currentOutLabel = new JLabel(" ");
            currentOutLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            currentOutLabel.setHorizontalAlignment(JLabel.CENTER);
            visPanel1.add(currentOutLabel, BorderLayout.PAGE_END);

            visPanel.add(visPanel1, BorderLayout.PAGE_END);
            stepsPanel.add(visPanel, BorderLayout.CENTER);

            JPanel buttonsPanel = new JPanel(new BorderLayout());

            JButton prevButton = new JButton("<");
            JButton nextButton = new JButton(">");
            JLabel stepLabel = new JLabel("(-)");
            stepLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            stepLabel.setHorizontalAlignment(JLabel.CENTER);
            buttonsPanel.add(prevButton, BorderLayout.LINE_START);
            buttonsPanel.add(nextButton, BorderLayout.LINE_END);
            buttonsPanel.add(stepLabel, BorderLayout.CENTER);
            stepsPanel.add(buttonsPanel, BorderLayout.PAGE_END);

            resultPanel.add(stepsPanel, BorderLayout.CENTER);

            JLabel endLabel = new JLabel("Végső forma: ");
            endLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            resultPanel.add(endLabel, BorderLayout.PAGE_END);

            GridBagConstraints gbc1 = new GridBagConstraints();
            gbc1.weightx = 1d;
            gbc1.weighty = 1d;
            gbc1.fill = GridBagConstraints.BOTH;
            gbc1.anchor = GridBagConstraints.NORTH;
            gbc1.gridx = 0;
            gbc1.gridy = 1;
            conversionPanel.add(resultPanel, gbc1);

            inputButton.addActionListener((e) -> {
                String inputText = inputField.getText();

                if (inputText.length() == 0) {
                    showErrorDialog("Nincs beírva képlet!");
                    return;
                }

                try {
                    PolishNotation polishNotation = new PolishNotation();
                    List<PolishNotation.ConversionStep> conversionSteps = polishNotation.convertToInfixNotation(inputText);

                    statsLabel.setText(String.format("Összes lépés: %d", conversionSteps.size()));
                    endLabel.setText(String.format("Végső forma: %s", polishNotation.stringifyFrames(conversionSteps.get(conversionSteps.size() - 1).getOutputSnapshot())));

                    polishNotationHandler0 = new PolishNotationHandler();
                    polishNotationHandler0.polishNotation = polishNotation;
                    polishNotationHandler0.steps = conversionSteps;
                    polishNotationHandler0.stepLabel = stepLabel;
                    polishNotationHandler0.currentOutLabel = currentOutLabel;
                    polishNotationHandler0.stackLabel = stackLabel;
                    polishNotationHandler0.init();
                } catch (Exception e0) {
                    e0.printStackTrace();

                    showErrorDialog(e0.getClass().getCanonicalName());
                }
            });

            nextButton.addActionListener((e) -> {
                if (polishNotationHandler0 != null) {
                    polishNotationHandler0.next();
                }
            });

            prevButton.addActionListener((e) -> {
                if (polishNotationHandler0 != null) {
                    polishNotationHandler0.prev();
                }
            });
        }

        return conversionPanel;
    }

    private JPanel quickSortTab() {
        JPanel panel = new JPanel(new BorderLayout());

        {
            JPanel inputPanel = new JPanel(new GridBagLayout());
            inputPanel.setBorder(BorderFactory.createTitledBorder("Bemenet"));

            JTextField arrayInput = new JTextField("8,7,13,6,11,3,1,9,4");
            JComboBox<String> axisInput = new JComboBox<>(new String[]{"Random", "Mindig az első", "Mindig az utolsó"});
            JButton goButton = new JButton("Mehet");

            inputPanel.add(arrayInput, new GridBagConstraints(0, 0, 1, 1, 2.75d, 1d, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 2, 0, 2), 0, 0));
            inputPanel.add(axisInput, new GridBagConstraints(1, 0, 1, 1, 1d, 1d, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 2, 0, 2), 0, 0));
            inputPanel.add(goButton, new GridBagConstraints(2, 0, 1, 1, 0d, 1d, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0, 2, 0, 2), 0, 0));

            panel.add(inputPanel, BorderLayout.PAGE_START);

            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.setBorder(BorderFactory.createTitledBorder("Particionálás lépésenként"));

            JTable table = new JTable();
            table.setShowGrid(true);
            table.setModel(new DefaultTableModel(new Object[0][0], new String[]{"#", "Partició", "Tengely", "Tengely index", "Halmaz", "Összehasonlítások", "(Összes)"}));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.setEnabled(false);
            JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            resultPanel.add(scrollPane, BorderLayout.CENTER);

            panel.add(resultPanel, BorderLayout.CENTER);

            goButton.addActionListener((e) -> {
                try {
                    double[] array = readUserDouble(arrayInput);

                    String axisMethod = ((String) Objects.requireNonNull(axisInput.getSelectedItem())).toLowerCase();

                    QuickSort.AxisMethod axisMethod1;
                    switch (axisMethod) {
                        case "random":
                            axisMethod1 = QuickSort.AxisMethod.RANDOM;
                            break;

                        case "mindig az első":
                            axisMethod1 = QuickSort.AxisMethod.ALWAYS_FIRST;
                            break;

                        case "mindig az utolsó":
                            axisMethod1 = QuickSort.AxisMethod.ALWAYS_LAST;
                            break;

                        default:
                            showErrorDialog("Hibás algoritmus??");
                            return;
                    }

                    QuickSort.AXIS_METHOD = axisMethod1;

                    QuickSort quickSort = new QuickSort();
                    List<QuickSort.SortStep> steps = quickSort.quickSort(array);

                    Object[][] rows = new Object[steps.size()][];

                    for (int i = 0; i < steps.size(); i++) {
                        QuickSort.SortStep step = steps.get(i);
                        Object[] columns = new Object[7];

                        columns[0] = i + 1;
                        columns[2] = (int) step.data[step.axis];
                        columns[3] = step.axis;
                        columns[5] = step.comparisons;
                        columns[6] = step.totalComparisons;

                        StringBuilder stringBuilder = new StringBuilder();
                        for (double d : step.data) {
                            stringBuilder.append((int) d).append(',');
                        }
                        stringBuilder.setLength(stringBuilder.length() - 1);
                        columns[4] = stringBuilder.toString();

                        stringBuilder.setLength(0);
                        for (int j = 0; j < step.partition.length; j++) {
                            if (j == step.axis - step.partitionStart) {
                                stringBuilder.append('+');
                            }

                            stringBuilder.append((int) step.partition[j]).append(',');
                        }
                        stringBuilder.setLength(stringBuilder.length() - 1);
                        columns[1] = stringBuilder.toString();

                        rows[i] = columns;
                    }

                    ((DefaultTableModel) table.getModel()).setDataVector(rows, new Object[]{"#", "Partició", "Tengely", "Tengely index", "Halmaz", "Összehasonlítások", "(Összes)"});
                } catch (Exception e1) {
                    e1.printStackTrace();

                    showErrorDialog(e1.getMessage());
                }
            });
        }

        return panel;
    }

    private JPanel mergeSortTab() {
        JPanel panel = new JPanel(new BorderLayout());

        {
            JPanel inputPanel = new JPanel(new GridBagLayout());
            inputPanel.setBorder(BorderFactory.createTitledBorder("Bemenet"));

            JTextField arrayInput = new JTextField("36, 27, 12, 24, 32, 15, 22, 35, 10");
            JButton goButton = new JButton("Mehet");

            inputPanel.add(arrayInput, new GridBagConstraints(0, 0, 1, 1, 3d, 1d, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 2, 0, 2), 0, 0));
            inputPanel.add(goButton, new GridBagConstraints(1, 0, 1, 1, 0d, 1d, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0, 2, 0, 2), 0, 0));

            panel.add(inputPanel, BorderLayout.PAGE_START);

            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.setBorder(BorderFactory.createTitledBorder("Rekurzív felbontások"));

            JTable table = new JTable();
            table.setShowGrid(true);
            table.setModel(new DefaultTableModel(new Object[0][0], new String[]{"#", "Rekurzív", "Halmaz", "Összehasonlítások", "(Összes)", "Mozgatások", "(Összes)"}));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.setEnabled(false);
            JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            resultPanel.add(scrollPane, BorderLayout.CENTER);

            panel.add(resultPanel, BorderLayout.CENTER);

            goButton.addActionListener((e) -> {
                try {
                    double[] array = readUserDouble(arrayInput);

                    MergeSort mergeSort = new MergeSort();
                    List<MergeSort.SortStep> steps = mergeSort.mergeSort(array);

                    Object[][] rows = new Object[steps.size()][];

                    for (int i = 0; i < steps.size(); i++) {
                        MergeSort.SortStep step = steps.get(i);
                        Object[] columns = new Object[7];

                        columns[0] = i + 1;
                        columns[3] = step.comparisons;
                        columns[4] = step.totalComparisons;
                        columns[5] = step.moves;
                        columns[6] = step.totalMoves;

                        StringBuilder stringBuilder = new StringBuilder();
                        for (double d : step.merged) {
                            stringBuilder.append((int) d).append(',');
                        }
                        stringBuilder.setLength(stringBuilder.length() - 1);
                        columns[1] = stringBuilder.toString();

                        stringBuilder.setLength(0);
                        for (double d : step.data) {
                            stringBuilder.append((int) d).append(',');
                        }
                        stringBuilder.setLength(stringBuilder.length() - 1);
                        columns[2] = stringBuilder.toString();

                        rows[i] = columns;
                    }

                    ((DefaultTableModel) table.getModel()).setDataVector(rows, new Object[]{"#", "Rekurzív", "Halmaz", "Összehasonlítások", "(Összes)", "Mozgatások", "(Összes)"});
                } catch (Exception e1) {
                    e1.printStackTrace();

                    showErrorDialog(e1.getMessage());
                }
            });
        }

        return panel;
    }

    private JPanel simpleSortsTab() {
        JPanel panel = new JPanel(new BorderLayout());

        {
            JPanel inputPanel = new JPanel(new GridBagLayout());
            inputPanel.setBorder(BorderFactory.createTitledBorder("Bemenet"));

            JTextField arrayInput = new JTextField("1 8 6 34 9 1 -74 2 9");
            JComboBox<String> algorithmInput = new JComboBox<>(new String[]{"Buborékos", "Fejlesztett buborékos", "Maximum-kiválasztás", "Minimum-kiválasztás", "Beszúró", "Naiv beszúró"});
            JButton goButton = new JButton("Mehet");

            inputPanel.add(arrayInput, new GridBagConstraints(0, 0, 1, 1, 2.75d, 1d, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 2, 0, 2), 0, 0));
            inputPanel.add(algorithmInput, new GridBagConstraints(1, 0, 1, 1, 1d, 1d, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 2, 0, 2), 0, 0));
            inputPanel.add(goButton, new GridBagConstraints(2, 0, 1, 1, 0d, 1d, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0, 2, 0, 2), 0, 0));

            panel.add(inputPanel, BorderLayout.PAGE_START);

            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.setBorder(BorderFactory.createTitledBorder("Algoritmus lépésenként"));

            JTable table = new JTable();
            table.setShowGrid(true);
            table.setModel(new DefaultTableModel(new Object[0][0], new String[]{"#", "Halmaz", "Összehasonlítások", "(Összes)", "Cserék", "(Összes)", "Ciklus"}));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.setEnabled(false);
            JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            resultPanel.add(scrollPane, BorderLayout.CENTER);

            panel.add(resultPanel, BorderLayout.CENTER);

            goButton.addActionListener((e) -> {
                try {
                    double[] array = readUserDouble(arrayInput);

                    String algorithm = ((String) Objects.requireNonNull(algorithmInput.getSelectedItem())).toLowerCase();

                    SimpleSorts simpleSorts = new SimpleSorts();
                    List<SimpleSorts.SortStep> steps;

                    switch (algorithm) {
                        case "buborékos":
                            steps = simpleSorts.bubble(array);
                            break;

                        case "fejlesztett buborékos":
                            steps = simpleSorts.bubbleProMax(array);
                            break;

                        case "maximum-kiválasztás":
                            steps = simpleSorts.maximum(array);
                            break;

                        case "beszúró":
                            steps = simpleSorts.insertion(array);
                            break;

                        case "minimum-kiválasztás":
                            steps = simpleSorts.minimum(array);
                            break;

                        case "naiv beszúró":
                            steps = simpleSorts.simpleInsertion(array);
                            break;

                        default:
                            showErrorDialog("Hibás algoritmus??");
                            return;
                    }

                    Object[][] rows = new Object[steps.size()][];

                    for (int i = 0; i < steps.size(); i++) {
                        SimpleSorts.SortStep step = steps.get(i);
                        Object[] columns = new Object[7];

                        columns[0] = i + 1;
                        columns[2] = step.comparisons;
                        columns[3] = step.comparisonsTotal;
                        columns[4] = step.switches;
                        columns[5] = step.switchesTotal;
                        columns[6] = step.totalIter;

                        StringBuilder stringBuilder = new StringBuilder();

                        for (double d : step.data) {
                            stringBuilder.append((int) d).append(',');
                        }

                        stringBuilder.setLength(stringBuilder.length() - 1);

                        columns[1] = stringBuilder.toString();

                        rows[i] = columns;
                    }

                    ((DefaultTableModel) table.getModel()).setDataVector(rows, new Object[]{"#", "Halmaz", "Összehasonlítások", "(Összes)", "Cserék", "(Összes)", "Ciklus"});
                } catch (Exception e1) {
                    e1.printStackTrace();

                    showErrorDialog(e1.getMessage());
                }
            });
        }

        return panel;
    }

    private static class PolishNotationHandler {
        private int currentStep = 0;
        private PolishNotation polishNotation;
        private JLabel stepLabel;
        private JLabel currentOutLabel;
        private JLabel stackLabel;
        private List<PolishNotation.ConversionStep> steps;

        private void init() {
            currentStep = 0;

            update();
        }

        private void next() {
            if (currentStep + 1 < steps.size()) {
                currentStep++;
            }

            update();
        }

        private void prev() {
            if (currentStep - 1 >= 0) {
                currentStep--;
            }

            update();
        }

        private void update() {
            PolishNotation.EquationFrame nextFrame = steps.get(currentStep).getNextFrame();
            String nextFrameString = nextFrame == null ? null : nextFrame.toString();
            stepLabel.setText("(" + (currentStep + 1) + (nextFrameString != null ? " | " + nextFrame : "") + ")");

            currentOutLabel.setText(polishNotation.stringifyFrames(steps.get(currentStep).getOutputSnapshot()));

            List<PolishNotation.EquationFrame> frames = new ArrayList<>(steps.get(currentStep).getStackSnapshot());
            Collections.reverse(frames);

            if (frames.size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("<html><center>");
                for (PolishNotation.EquationFrame frame : frames) {
                    stringBuilder.append(frame.toString()).append("<br>");
                }
                stringBuilder.append("</center></html>");

                stackLabel.setText(stringBuilder.toString());
            } else {
                stackLabel.setText("   ");
            }
        }
    }

    private PolishNotationHandler polishNotationHandler;

    private JPanel polishNotationTab() {
        JPanel panel = new JPanel(new GridBagLayout());

        JPanel conversionPanel = new JPanel();
        conversionPanel.setBorder(BorderFactory.createTitledBorder("Lengyel formára alakítás"));

        {
            conversionPanel.setLayout(new GridBagLayout());

            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BorderLayout());

            JTextField inputField = new JTextField("a + b * c - x ^ ( 2 * i - j )");
            JButton inputButton = new JButton("Mehet");

            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(inputButton, BorderLayout.LINE_END);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1d;
            gbc.weighty = 0d;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            conversionPanel.add(inputPanel, gbc);

            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

            JLabel statsLabel = new JLabel("Összes lépés: -");
            statsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            resultPanel.add(statsLabel, BorderLayout.PAGE_START);

            JPanel stepsPanel = new JPanel(new BorderLayout());

            JPanel visPanel = new JPanel(new BorderLayout());

            JLabel stackLabel = new JLabel("   ");
            stackLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17));
            stackLabel.setHorizontalAlignment(JLabel.CENTER);
            stackLabel.setVerticalAlignment(JLabel.BOTTOM);

            {
                JPanel an2 = new JPanel(new GridBagLayout());

                JPanel an3 = new JPanel(new BorderLayout());
                an3.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 3, 3, 3, Color.BLACK), BorderFactory.createEmptyBorder(25, 25, 3, 25)));
                an3.add(stackLabel, BorderLayout.CENTER);

                GridBagConstraints gbc2 = new GridBagConstraints();
                gbc2.anchor = GridBagConstraints.NORTH;
                gbc2.fill = GridBagConstraints.VERTICAL;
                gbc2.weightx = 0d;
                gbc2.weighty = 1d;
                an2.add(an3, gbc2);

                visPanel.add(an2, BorderLayout.CENTER);
            }

            JPanel visPanel1 = new JPanel(new BorderLayout());

            JLabel an1 = new JLabel("Jelenlegi kimenet:");
            an1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            an1.setHorizontalAlignment(JLabel.CENTER);
            visPanel1.add(an1, BorderLayout.PAGE_START);

            JLabel currentOutLabel = new JLabel(" ");
            currentOutLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            currentOutLabel.setHorizontalAlignment(JLabel.CENTER);
            visPanel1.add(currentOutLabel, BorderLayout.PAGE_END);

            visPanel.add(visPanel1, BorderLayout.PAGE_END);
            stepsPanel.add(visPanel, BorderLayout.CENTER);

            JPanel buttonsPanel = new JPanel(new BorderLayout());

            JButton prevButton = new JButton("<");
            JButton nextButton = new JButton(">");
            JLabel stepLabel = new JLabel("(-)");
            stepLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            stepLabel.setHorizontalAlignment(JLabel.CENTER);
            buttonsPanel.add(prevButton, BorderLayout.LINE_START);
            buttonsPanel.add(nextButton, BorderLayout.LINE_END);
            buttonsPanel.add(stepLabel, BorderLayout.CENTER);
            stepsPanel.add(buttonsPanel, BorderLayout.PAGE_END);

            resultPanel.add(stepsPanel, BorderLayout.CENTER);

            JLabel endLabel = new JLabel("Végső forma: ");
            endLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            resultPanel.add(endLabel, BorderLayout.PAGE_END);

            GridBagConstraints gbc1 = new GridBagConstraints();
            gbc1.weightx = 1d;
            gbc1.weighty = 1d;
            gbc1.fill = GridBagConstraints.BOTH;
            gbc1.anchor = GridBagConstraints.NORTH;
            gbc1.gridx = 0;
            gbc1.gridy = 1;
            conversionPanel.add(resultPanel, gbc1);

            inputButton.addActionListener((e) -> {
                String inputText = inputField.getText();

                if (inputText.length() == 0) {
                    showErrorDialog("Nincs beírva képlet!");
                    return;
                }

                try {
                    PolishNotation polishNotation = new PolishNotation();
                    List<PolishNotation.ConversionStep> conversionSteps = polishNotation.convertToPolishNotation(inputText);

                    statsLabel.setText(String.format("Összes lépés: %d", conversionSteps.size()));
                    endLabel.setText(String.format("Végső forma: %s", polishNotation.stringifyFrames(conversionSteps.get(conversionSteps.size() - 1).getOutputSnapshot())));

                    polishNotationHandler = new PolishNotationHandler();
                    polishNotationHandler.polishNotation = polishNotation;
                    polishNotationHandler.steps = conversionSteps;
                    polishNotationHandler.stepLabel = stepLabel;
                    polishNotationHandler.currentOutLabel = currentOutLabel;
                    polishNotationHandler.stackLabel = stackLabel;
                    polishNotationHandler.init();
                } catch (Exception e0) {
                    e0.printStackTrace();

                    showErrorDialog(e0.getClass().getCanonicalName());
                }
            });

            nextButton.addActionListener((e) -> {
                if (polishNotationHandler != null) {
                    polishNotationHandler.next();
                }
            });

            prevButton.addActionListener((e) -> {
                if (polishNotationHandler != null) {
                    polishNotationHandler.prev();
                }
            });
        }

        panel.add(conversionPanel, new GridBagConstraints(0, 0, 1, 2, 1d, 1d, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        JPanel evaluatePanel = new JPanel(new GridBagLayout());
        evaluatePanel.setBorder(BorderFactory.createTitledBorder("Kiértékelés"));

        {
            JButton inputButton = new JButton("Mehet");
            JTextField exprField = new JTextField();
            exprField.setText("ab+");
            JTextField assignField = new JTextField();
            assignField.setText("a=4; b=6");

            evaluatePanel.add(exprField, new GridBagConstraints(0, 0, 1, 1, 1.75d, 1d, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 2, 0, 2), 3, 3));
            evaluatePanel.add(assignField, new GridBagConstraints(1, 0, 1, 1, 1d, 1d, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 2, 0, 2), 3, 3));
            evaluatePanel.add(inputButton, new GridBagConstraints(2, 0, 1, 1, 0d, 1d, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0, 2, 0, 2), 3, 3));

            inputButton.addActionListener((e) -> {
                String inputString = exprField.getText();
                String assignString = assignField.getText();

                if (inputString.length() == 0) {
                    showErrorDialog("Nincs képlet :(");
                    return;
                }

                try {
                    Map<String, Double> assignments = new HashMap<>();
                    String[] assignmentParts = assignString.split(";");

                    if (assignmentParts.length > 0 && assignString.length() > 0) {
                        for (String s : assignmentParts) {
                            String[] ap = s.split("=");

                            if (ap.length == 2) {
                                assignments.put(ap[0].trim(), Double.parseDouble(ap[1]));
                            }
                        }
                    }

                    double result = new PolishNotation().evaluateExpression(inputString, assignments);
                    JOptionPane.showMessageDialog(mainFrame, result, mainFrame.getTitle(), JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e1) {
                    e1.printStackTrace();

                    showErrorDialog(e1.getMessage() != null ? e1.getMessage() : e1.getClass().getCanonicalName());
                }
            });
        }

        panel.add(evaluatePanel, new GridBagConstraints(0, 2, 1, 1, 1d, 0d, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        return panel;
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, mainFrame.getTitle(), JOptionPane.ERROR_MESSAGE);
    }

    private float[] readUserFloat(JTextField field) {
        double[] array = readUserDouble(field);

        float[] a = new float[array.length];

        for (int i = 0; i < array.length; i++) {
            a[i] = (float) array[i];
        }

        return a;
    }

    private int[] readUserInt(JTextField field) {
        double[] array = readUserDouble(field);

        int[] a = new int[array.length];

        for (int i = 0; i < array.length; i++) {
            a[i] = (int) array[i];
        }

        return a;
    }

    private double[] readUserDouble(JTextField field) {
        String src = cleanUserInput(field.getText());
        String[] numS = src.split(" ");

        double[] array = new double[numS.length];

        if (src.length() > 0) {
            for (int i = 0; i < array.length; i++) {
                array[i] = Double.parseDouble(numS[i]);
            }
        } else {
            array = new double[0];
        }

        return array;
    }

    private int[] parseInts(String s) {
        s = cleanUserInput(s);
        String[] numS = s.split(" ");
        int[] array = new int[numS.length];

        if (s.length() > 0) {
            for (int i = 0; i < array.length; i++) {
                array[i] = Integer.parseInt(numS[i]);
            }
        } else {
            array = new int[0];
        }

        return array;
    }

    private String cleanUserInput(String input) {
        return input.replace(",", " ").replaceAll("\\s+", " ").trim();
    }
}
