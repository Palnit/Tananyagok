package jp.tengopapa.algo.solvers;

public class CountingSort {
    public static int[] countingSort(int[] a, int r /* base */, int digit) {
        int[] b = new int[a.length];
        int[] c = new int[r];

        for (int i : a) {
            c[RadixSort.getDigit(i, digit)]++;
        }

        for(int k = 1; k < r; k++) {
            c[k] += c[k - 1];
        }

        for(int i = a.length - 1; i >= 0; i--) {
            int k = RadixSort.getDigit(a[i], digit);
            c[k]--;

            b[c[k]] = a[i];
        }

        return b;
    }
}
