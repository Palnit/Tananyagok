package jp.tengopapa.algo.data;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class TreeContainer<R> {
    public static final int MASK_MOVEMENT = 1 << 16;

    private final TreeNode<R> root;
    private final DelegateTree<TreeNode<R>, Integer> tree;
    private final VisualizationViewer<TreeNode<R>, Integer> visualizationViewer;
    private final GraphZoomScrollPane pane;
    private MouseHandler<R> mouseHandler;

    public TreeContainer(TreeNode<R> root) {
        this.root = root;

        tree = new DelegateTree<>(new DirectedOrderedSparseMultigraph<>());
        buildTree(root, tree);

        TreeLayout<TreeNode<R>, Integer> layout = new TreeLayout<>(tree, 40, 40);
        visualizationViewer = new VisualizationViewer<>(layout);

        visualizationViewer.setForeground(Color.BLACK);
        visualizationViewer.getRenderContext().setEdgeShapeTransformer(input -> (input >= 0 ? shapeMask(EdgeShape.line(tree).apply(input), EdgeShape.cubicCurve(tree).apply(input), MASK_MOVEMENT, input): new Line2D.Float(0.0f, 0.0f, 0.0f, 0.0f)));
        visualizationViewer.getRenderContext().setVertexLabelTransformer(input -> (input.data != null ? String.valueOf(input.data) : ""));
        visualizationViewer.getRenderContext().setVertexFillPaintTransformer(input -> (input.data != null ? Color.WHITE : new Color(0, 0, 0, 0)));
        visualizationViewer.getRenderContext().setVertexShapeTransformer(input -> new Ellipse2D.Float(-15f, -15f, 30f, 30f));
        visualizationViewer.getRenderContext().setVertexStrokeTransformer(input -> (input.data != null ? new BasicStroke(2) : new BasicStroke(0)));
        visualizationViewer.getRenderContext().setVertexDrawPaintTransformer(input -> (input.data != null ? (input.marked() ? Color.RED : Color.LIGHT_GRAY) : new Color(0, 0, 0, 0)));
        visualizationViewer.getRenderContext().setEdgeDrawPaintTransformer(input -> (input >= 0 ? colorMask(Color.LIGHT_GRAY, Color.RED, MASK_MOVEMENT, input) : new Color(0, 0, 0, 0)));
        visualizationViewer.getRenderContext().setArrowDrawPaintTransformer(input -> (input >= 0 ? colorMask(Color.LIGHT_GRAY, Color.RED, MASK_MOVEMENT, input) : new Color(0, 0, 0, 0)));
        visualizationViewer.getRenderContext().setArrowFillPaintTransformer(input -> (input >= 0 ? colorMask(Color.LIGHT_GRAY, Color.RED, MASK_MOVEMENT, input) : new Color(0, 0, 0, 0)));
        visualizationViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        visualizationViewer.setAlignmentX(Component.CENTER_ALIGNMENT);
        visualizationViewer.setAlignmentY(Component.CENTER_ALIGNMENT);

        pane = new GraphZoomScrollPane(visualizationViewer);

        visualizationViewer.setGraphMouse(new PluggableGraphMouse() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                TreeNode<R> s = visualizationViewer.getPickSupport().getVertex(visualizationViewer.getGraphLayout(), e.getX(), e.getY());

                if (s != null && tree.getInEdges(s).stream().noneMatch(it -> it < 0) && mouseHandler != null) {
                    mouseHandler.mouseClicked(e, s);
                }
            }
        });
    }

    private Shape shapeMask(Shape shape1, Shape shape2, int mask, int input) {
        if((input & mask) != 0) {
            return shape2;
        } else {
            return shape1;
        }
    }

    private Color colorMask(Color color1, Color color2, int mask, int input) {
        if((input & mask) != 0) {
            return color2;
        } else {
            return color1;
        }
    }

    public void treeChanged(TreeNode<R> newRoot) {
        root.copyFrom(newRoot);

        tree.getChildren(root).forEach(tree::removeChild);
        addLeaves(root, tree);

        visualizationViewer.setGraphLayout(new TreeLayout<>(tree, 40, 40));
        visualizationViewer.repaint();
    }

    private void buildTree(TreeNode<R> root, DelegateTree<TreeNode<R>, Integer> jungTree) {
        jungTree.setRoot(root);

        addLeaves(root, jungTree);
    }

    private void addLeaves(TreeNode<R> localRoot, DelegateTree<TreeNode<R>, Integer> jungTree) {
        if (localRoot == null) {
            return;
        }

        TreeNode<R> left = localRoot.left();
        TreeNode<R> right = localRoot.right();

        if (left != null) {
            jungTree.addChild(jungTree.getEdgeCount(), localRoot, left);
            addLeaves(left, jungTree);
        } else {
            jungTree.addChild(-1 * jungTree.getEdgeCount() - 1, localRoot, new TreeNode<>(null, null, null, localRoot));
        }

        if (right != null) {
            jungTree.addChild(jungTree.getEdgeCount(), localRoot, right);
            addLeaves(right, jungTree);
        } else {
            jungTree.addChild(-1 * jungTree.getEdgeCount() - 1, localRoot, new TreeNode<>(null, null, null, localRoot));
        }
    }

    public TreeNode<R> getJungRoot() {
        return tree.getRoot();
    }

    public GraphZoomScrollPane getPane() {
        return pane;
    }

    public void setMouseHandler(MouseHandler<R> mouseHandler) {
        this.mouseHandler = mouseHandler;
    }

    public interface MouseHandler<S> {
        void mouseClicked(MouseEvent mouseEvent, TreeNode<S> node);
    }
}
