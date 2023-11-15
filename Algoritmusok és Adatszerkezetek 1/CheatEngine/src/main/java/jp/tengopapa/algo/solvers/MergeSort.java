package jp.tengopapa.algo.solvers;

import java.util.ArrayList;
import java.util.List;

public class MergeSort {
    public List<SortStep> mergeSort(double[] array) {
        List<SortStep> steps = ms(array, 0, array.length);

        for(int i = 0; i < steps.size(); i++) {
            int totalCompare = 0;
            int totalMove = 0;

            for(int j = i - 1; j >= 0; j--) {
                SortStep step = steps.get(j);

                totalCompare += step.comparisons;
                totalMove += step.moves;
            }

            SortStep step = steps.get(i);

            step.totalComparisons = totalCompare + step.comparisons;
            step.totalMoves = totalMove + step.moves;
        }

        return steps;
    }

    private List<SortStep> ms(double[] array, int u, int v) {
        List<SortStep> steps = new ArrayList<>();

        if(u < v - 1) {
            int m = (int) Math.floor((u + v) / 2d);

            steps.addAll(ms(array, u, m));
            steps.addAll(ms(array, m, v));

            steps.add(merge(array, u, m, v));
        }

        return steps;
    }

    private SortStep merge(double[] array, int u, int m, int v) {
        int comparisons = 0;
        int switches = 0;

        int d = m - u;

        double[] z = new double[d];
        System.arraycopy(array, u, z, 0, d);

        int k = u;
        int j = 0;
        int i = m;

        while(i < v && j < d) {
            comparisons++;
            switches++;

            if(array[i] < z[j]) {
                array[k] = array[i];
                i++;
            } else {
                array[k] = z[j];
                j++;
            }

            k++;
        }

        while(j < d) {
            switches++;
            array[k] = z[j];
            k++;
            j++;
        }

        double[] merged = new double[v - u];
        System.arraycopy(array, u, merged, 0, merged.length);

        return new SortStep(comparisons, switches, 0, 0, array, merged);
    }

    public static class SortStep {
        public int comparisons;
        public int moves;
        public int totalComparisons;
        public int totalMoves;

        public double[] data;
        public double[] merged;

        public SortStep(int comparisons, int moves, int totalComparisons, int totalMoves, double[] data, double[] merged) {
            this.comparisons = comparisons;
            this.moves = moves;
            this.totalComparisons = totalComparisons;
            this.totalMoves = totalMoves;

            this.data = new double[data.length];
            this.merged = new double[merged.length];
            System.arraycopy(data, 0, this.data, 0, data.length);
            System.arraycopy(merged, 0, this.merged, 0, merged.length);
        }
    }
}
