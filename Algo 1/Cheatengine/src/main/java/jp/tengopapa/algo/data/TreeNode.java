package jp.tengopapa.algo.data;

import java.util.Objects;

public class TreeNode <T> {
    public T data;

    private TreeNode<T> left;
    private TreeNode<T> right;
    private TreeNode<T> parent;

    private boolean marked = false;

    public TreeNode(T data, TreeNode<T> left, TreeNode<T> right, TreeNode<T> parent) {
        this.data = data;
        this.left = left;
        this.right = right;
        this.parent = parent;
    }

    public TreeNode<T> search(T value) {
        if(Objects.equals(value, this.data)) {
            return this;
        }

        if(this.left != null) {
            TreeNode<T> left = this.left.search(value);
            if(left != null) {
                return left;
            }
        }

        if(this.right != null) {
            return this.right.search(value);
        }

        return null;
    }

    public String walkInorder() {
        StringBuilder stringBuilder = new StringBuilder();
        printInorder(stringBuilder, this);
        return removeTrailing(stringBuilder);
    }

    public String walkPostorder() {
        StringBuilder stringBuilder = new StringBuilder();
        printPostorder(stringBuilder, this);
        return removeTrailing(stringBuilder);
    }

    public String walkPreorder() {
        StringBuilder stringBuilder = new StringBuilder();
        printPreorder(stringBuilder, this);
        return removeTrailing(stringBuilder);
    }

    private void printPreorder(StringBuilder stringBuilder, TreeNode<T> node) {
        if(node == null) {
            return;
        }

        stringBuilder.append(node.data).append(", ");
        printPreorder(stringBuilder, node.left);
        printPreorder(stringBuilder, node.right);
    }

    private void printPostorder(StringBuilder stringBuilder, TreeNode<T> node) {
        if(node == null) {
            return;
        }

        printPostorder(stringBuilder, node.left);
        printPostorder(stringBuilder, node.right);
        stringBuilder.append(node.data).append(", ");
    }

    private void printInorder(StringBuilder stringBuilder, TreeNode<T> node) {
        if(node == null) {
            return;
        }

        printInorder(stringBuilder, node.left);
        stringBuilder.append(node.data).append(", ");
        printInorder(stringBuilder, node.right);
    }

    public String walkLevelOrder() {
        StringBuilder stringBuilder = new StringBuilder();
        printLevelOrder(stringBuilder, this);
        return removeTrailing(stringBuilder);
    }

    private void printLevelOrder(StringBuilder stringBuilder, TreeNode<T> node) {
        int h = height(node);

        for(int i = 1; i <= h; i++) {
            printCurrentLevel(stringBuilder, node, i);
        }
    }

    private void printCurrentLevel(StringBuilder stringBuilder, TreeNode<T> node, int level) {
        if(node == null) {
            return;
        }

        if(level == 1) {
            stringBuilder.append(node.data).append(", ");
        } else {
            printCurrentLevel(stringBuilder, node.left, level - 1);
            printCurrentLevel(stringBuilder, node.right, level - 1);
        }
    }

    public int height() {
        return height(this);
    }

    private int height(TreeNode<T> node) {
        if(node == null) {
            return 0;
        }

        int lHeight = height(node.left);
        int rHeight = height(node.right);

        return Math.max(lHeight, rHeight) + 1;
    }

    private String removeTrailing(StringBuilder stringBuilder) {
        if(stringBuilder.length() >= 2) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }

        return stringBuilder.toString();
    }

    public void copyFrom(TreeNode<T> from) {
        this.data = from.data;
        this.marked = from.marked;

        this.left = from.left;
        this.right = from.right;
    }

    @Override
    public String toString() {
        return Objects.toString(data);
    }

    public TreeNode<T> left() {
        return left;
    }

    public TreeNode<T> right() {
        return right;
    }

    public TreeNode<T> parent() {
        return parent;
    }

    public void left(TreeNode<T> n) {
        left = n;
    }

    public void right(TreeNode<T> n) {
        right = n;
    }

    public void parent(TreeNode<T> n) {
        parent = n;
    }

    public boolean marked() {
        return marked;
    }

    public void marked(boolean val) {
        this.marked = val;
    }
}
