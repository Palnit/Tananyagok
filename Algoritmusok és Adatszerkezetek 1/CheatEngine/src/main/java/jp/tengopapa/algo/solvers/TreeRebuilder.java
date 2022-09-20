package jp.tengopapa.algo.solvers;

import jp.tengopapa.algo.data.TreeNode;

import java.util.Arrays;
import java.util.HashSet;

public class TreeRebuilder {
    private static int preIndex = 0;

    private static TreeNode<String> buildTreeInPre(String[] inorder, String[] preorder, int inStart, int inEnd, TreeNode<String> parent) {
        if(inStart > inEnd) {
            return null;
        }

        TreeNode<String> node = new TreeNode<>(preorder[preIndex++], null, null, parent);

        if(inStart == inEnd) {
            return node;
        }

        int inIndex = search(inorder, inStart, inEnd, node.data);

        node.left(buildTreeInPre(inorder, preorder, inStart, inIndex - 1, node));
        node.right(buildTreeInPre(inorder, preorder, inIndex + 1, inEnd, node));

        return node;
    }

    private static TreeNode<String> buildTreeInPost(String[] inorder, String[] postorder, int inStart, int inEnd, int postStart, int postEnd, TreeNode<String> parent) {
        if(inStart > inEnd) {
            return null;
        }

        TreeNode<String> node = new TreeNode<>(postorder[postEnd], null, null, parent);

        if(inStart == inEnd) {
            return node;
        }

        int iIndex = search(inorder, inStart, inEnd, node.data);

        node.left(buildTreeInPost(inorder, postorder, inStart, iIndex - 1, postStart, postStart - inStart + iIndex - 1, node));
        node.right(buildTreeInPost(inorder, postorder, iIndex + 1, inEnd, postEnd - inEnd + iIndex, postEnd - 1, node));

        return node;
    }

    private static TreeNode<String> buildTreeInLevel(String[] inorder, String[] levelOrder, int iStart, int iEnd, int n, TreeNode<String> parent) {
        if(n <= 0) {
            return null;
        }

        TreeNode<String> node = new TreeNode<>(levelOrder[0], null, null, parent);

        int index = -1;
        for(int i = iStart; i <= iEnd; i++) {
            if(levelOrder[0].equals(inorder[i])) {
                index = i;
                break;
            }
        }

        HashSet<String> s = new HashSet<>(Arrays.asList(inorder).subList(iStart, index));

        String[] lLevel = new String[s.size()];
        String[] rLevel = new String[iEnd - iStart - s.size()];

        int li = 0, ri = 0;

        for(int i = 1; i < n; i++) {
            if(s.contains(levelOrder[i])) {
                lLevel[li++] = levelOrder[i];
            } else {
                rLevel[ri++] = levelOrder[i];
            }
        }

        node.left(buildTreeInLevel(inorder, lLevel, iStart, index - 1, index - iStart, node));
        node.right(buildTreeInLevel(inorder, rLevel, index + 1, iEnd, iEnd - index, node));

        return node;
    }

    private static int search(String[] arr, int s, int e, String st) {
        for(int i = s; i < e; i++) {
            if(arr[i].equals(st)) {
                return i;
            }
        }

        return e;
    }

    public static TreeNode<String> tryRebuild(String inorder, String preorder, String postorder, String levelOrder) {
        boolean hasInOrder = inorder.trim().length() > 0;
        boolean hasPreOrder = preorder.trim().length() > 0;
        boolean hasPostOrder = postorder.trim().length() > 0;
        boolean hasLevelOrder = levelOrder.trim().length() > 0;

        TreeNode<String> ret = null;

        try {
            if (hasInOrder && hasPreOrder) {
                preIndex = 0;

                String[] inOrder = split(inorder);
                String[] preOrder = split(preorder);

                ret = buildTreeInPre(inOrder, preOrder, 0, inOrder.length - 1, null);
            } else if (hasPostOrder && hasInOrder) {
                String[] inOrder = split(inorder);
                String[] postOrder = split(postorder);

                ret = buildTreeInPost(inOrder, postOrder, 0, inOrder.length - 1, 0, inOrder.length - 1, null);
            } else if (hasInOrder && hasLevelOrder) {
                String[] inOrder = split(inorder);
                String[] levelOrderA = split(levelOrder);

                ret = buildTreeInLevel(inOrder, levelOrderA, 0, inOrder.length - 1, inOrder.length, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static String[] split(String input) {
        return input.replace(",", " ").replaceAll("\\s+", " ").trim().split(" ");
    }
}
