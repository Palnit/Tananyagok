package jp.tengopapa.algo.solvers;

import java.util.ArrayList;
import java.util.List;

public class SimpleSorts {
    public List<SortStep> bubble(double[] arr) {
        List<SortStep> steps = new ArrayList<>();

        int length = arr.length;
        int comparisonsTotal = 0;
        int switchesTotal = 0;
        int switches = 0;
        int comparisons = 0;
        int iter = 0;

        for (int i = length; i >= 2; i--) {
            iter++;
            for (int j = 0; j < i - 1; j++) {
                iter++;
                comparisons++;

                if (arr[j] > arr[j + 1]) {
                    switches++;
                    double temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }

            switchesTotal += switches;
            comparisonsTotal += comparisons;

            steps.add(new SortStep(comparisons, switches, arr, comparisonsTotal, switchesTotal, iter));

            comparisons = 0;
            switches = 0;
        }

        return steps;
    }

    public List<SortStep> bubbleProMax(double[] arr) {
        List<SortStep> steps = new ArrayList<>();

        int i = arr.length;
        int comparisonsTotal = 0;
        int switchesTotal = 0;
        int switches = 0;
        int comparisons = 0;
        int iter = 0;

        while (i > 1) {
            iter++;
            int idx = -1;
            for (int j = 0; j < i - 1; j++) {
                iter++;
                comparisons++;

                if (arr[j] > arr[j + 1]) {
                    switches++;
                    double temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    idx = j;
                }
            }

            i = idx + 1;

            comparisonsTotal += comparisons;
            switchesTotal += switches;

            steps.add(new SortStep(comparisons, switches, arr, comparisonsTotal, switchesTotal, iter));

            comparisons = 0;
            switches = 0;
        }

        return steps;
    }

    public List<SortStep> maximum(double[] arr) {
        List<SortStep> steps = new ArrayList<>();

        int comparisons = 0;
        int switches = 0;

        int comparisonsTotal = 0;
        int switchesTotal = 0;

        int iter = 0;

        int length = arr.length;
        for (int i = length - 1; i >= 1; i--) {
            iter++;
            int max_idx = 0;
            for (int j = max_idx + 1; j <= i; j++) {
                iter++;
                comparisons++;

                if (arr[j] > arr[max_idx]) {
                    max_idx = j;
                }
            }

            if (i != max_idx) {
                double temp = arr[i];
                arr[i] = arr[max_idx];
                arr[max_idx] = temp;

                switches++;
            }

            comparisonsTotal += comparisons;
            switchesTotal += switches;

            steps.add(new SortStep(comparisons, switches, arr, comparisonsTotal, switchesTotal, iter));

            comparisons = 0;
            switches = 0;
        }

        return steps;
    }

    public List<SortStep> insertion(double[] arr) {
        List<SortStep> steps = new ArrayList<>();

        int comparisonsTotal = 0;
        int switchesTotal = 0;

        int comparisons = 0;
        int switches = 0;

        int iter = 0;

        int length = arr.length;
        for (int i = 1; i < length; i++) {
            iter++;

            int moves = 0;

            comparisons++;
            if (arr[i - 1] > arr[i]) {
                moves++;
                double x = arr[i];
                arr[i] = arr[i - 1];
                int j = i - 2;
                int k = j;

                while (j >= 0) {
                    comparisons++;
                    if(arr[j] > x) {
                        iter++;
                        moves++;
                        switches++;
                        arr[j + 1] = arr[j];

                        j--;
                    } else {
                        break;
                    }
                }

                comparisons += moves - 1;
                arr[j + 1] = x;
                switches+=3;
            }

            switchesTotal += switches;
            comparisonsTotal += comparisons;

            steps.add(new SortStep(comparisons, switches, arr, comparisonsTotal, switchesTotal, iter));
            comparisons = 0;
            switches = 0;
        }

        return steps;
    }

    public List<SortStep> minimum(double[] arr) {
        List<SortStep> steps = new ArrayList<>();

        int n = arr.length;
        int totalcomparisons = 0;
        int comparisons = 0;
        int totalswitches = 0;
        int switches = 0;
        int iter = 0;

        for (int i = 0; i < n - 1; i++) {
            iter++;
            int min_idx = i;
            for (int j = i + 1; j < n; j++) {
                iter++;
                comparisons++;
                if (arr[j] < arr[min_idx])
                    min_idx = j;
            }

            double temp = arr[min_idx];
            arr[min_idx] = arr[i];
            arr[i] = temp;
            switches++;
            totalcomparisons += comparisons;
            totalswitches += switches;

            steps.add(new SortStep(comparisons, switches, arr, totalcomparisons, totalswitches, iter));

            switches = 0;
            comparisons = 0;
        }

        return steps;
    }

    public List<SortStep> simpleInsertion(double[] arr) {
        List<SortStep> steps = new ArrayList<>();

        int totalcomparisons = 0;
        int comparisons = 0;
        int totalswitches = 0;
        int switches = 0;
        int iter = 0;
        for (int i = 1; i < arr.length; i++) {
            iter++;
            int j = i;
            while (j > 0) {
                iter++;
                comparisons++;
                if(arr[j - 1] > arr[j]) {
                    switches++;
                    double temp = arr[j - 1];
                    arr[j - 1] = arr[j];
                    arr[j] = temp;
                    j--;
                } else {
                    break;
                }
            }
            comparisons++;
            totalcomparisons += comparisons;
            totalswitches += switches;

            steps.add(new SortStep(comparisons, switches, arr, totalcomparisons, totalswitches, iter));

            switches = 0;
            comparisons = 0;
        }

        return steps;
    }

    public static class SortStep {
        public int comparisons;
        public int switches;
        public int comparisonsTotal;
        public int switchesTotal;
        public int totalIter;
        public double[] data;

        public SortStep(int comparisons, int switches, double[] data, int compTotal, int swTotal, int totalIter) {
            this.comparisons = comparisons;
            this.switches = switches;
            this.comparisonsTotal = compTotal;
            this.switchesTotal = swTotal;
            this.totalIter = totalIter;

            this.data = new double[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);
        }
    }
}
