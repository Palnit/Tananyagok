package jp.tengopapa.algo.solvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RadixSort {
    public static int getDigit(int n, int place) {
        String s = String.format("%d", n);

        if(s.length() < place) {
            return 0;
        } else {
            return Integer.parseInt(String.valueOf(s.charAt(s.length() - place)));
        }
    }

    public static List<SortingStep> radixSort(int[] a, int r /* base */, int d /* num_digits */) {
        List<SortingStep> ret = new ArrayList<>();
        for(int i = 1; i <= d; i++) {
            SortingStep step = new SortingStep();
            step.buckets = distributionSort(a, r, i);
            step.listSoFar = Arrays.stream(a).boxed().collect(Collectors.toList());

            ret.add(step);
        }
        return ret;
    }

    private static HashMap<Integer, List<Integer>> distributionSort(int[] a, int r /* base */, int place) {
        HashMap<Integer, List<Integer>> buckets = new HashMap<>();

        for(int k = 0; k < r; k++) {
            buckets.put(k, new ArrayList<>());
        }

        for(int n : a) {
            int p = getDigit(n, place);
            buckets.get(p).add(n);
        }

        int idx = 0;
        for(int k = 0; k < r; k++) {
            List<Integer> bucket = buckets.get(k);

            for(int s : bucket) {
                a[idx++] = s;
            }
        }

        return buckets;
    }

    public static class SortingStep {
        public List<Integer> listSoFar;
        public HashMap<Integer, List<Integer>> buckets;
    }
}
