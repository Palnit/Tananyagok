package jp.tengopapa.algo.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuickSort {
    private static final Random RANDOM = new Random();

    public enum AxisMethod {
        RANDOM, ALWAYS_FIRST, ALWAYS_LAST
    }

    public static AxisMethod AXIS_METHOD = AxisMethod.RANDOM;

    public List<SortStep> quickSort(double[] array) {
        List<SortStep> steps = quickSort(array, 0, array.length - 1);

        for(int i = 0; i < steps.size(); i++) {
            int totalCompare = 0;

            for(int j = i - 1; j >= 0; j--) {
                SortStep step = steps.get(j);

                totalCompare += step.comparisons;
            }

            SortStep step = steps.get(i);
            step.totalComparisons = totalCompare + step.comparisons;
        }

        return steps;
    }

    private List<SortStep> quickSort(double[] array, int p, int r) {
        List<SortStep> steps = new ArrayList<>();

        if(p < r) {
            PartitionResult result = partition(array, p, r);
            int q = result.axis;

            double[] partition = new double[r - p + 1];
            System.arraycopy(array, p, partition, 0, partition.length);
            steps.add(new SortStep(q, result.comparisons, 0, partition, array, p));

            steps.addAll(quickSort(array, p, q - 1));
            steps.addAll(quickSort(array, q + 1, r));
        }

        return steps;
    }

    private PartitionResult partition(double[] array, int p, int r) {
        int comparisons = 0;
        int i;

        switch (AXIS_METHOD) {
            case ALWAYS_FIRST:
                i = p;
                break;

            case ALWAYS_LAST:
                i = r;
                break;

            case RANDOM:
            default:
                i = RANDOM.nextInt(r - p) + p;
                break;
        }

        double x = array[i];
        array[i] = array[r];

        i = p;

        while(i < r) {
            comparisons++;

            if(array[i] <= x) {
                i++;
            } else {
                break;
            }
        }

        if(i < r) {
            int j = i + 1;

            while(j < r) {
                comparisons++;
                if(array[j] < x) {
                    double temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;

                    i++;
                }

                j++;
            }

            array[r] = array[i];
            array[i] = x;
        } else {
            array[r] = x;
        }

        return new PartitionResult(i, comparisons);
    }

    public static class SortStep {
        public int axis;
        public int comparisons;
        public int totalComparisons;
        public int partitionStart;

        public double[] partition;
        public double[] data;

        public SortStep(int axis, int comparisons, int totalComparisons, double[] partition, double[] data, int partitionStart) {
            this.axis = axis;
            this.comparisons = comparisons;
            this.totalComparisons = totalComparisons;

            this.partition = new double[partition.length];
            System.arraycopy(partition, 0, this.partition, 0, partition.length);

            this.data = new double[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);

            this.partitionStart = partitionStart;
        }
    }

    private static class PartitionResult {
        private final int axis;
        private final int comparisons;

        public PartitionResult(int axis, int comparisons) {
            this.axis = axis;
            this.comparisons = comparisons;
        }
    }
}
