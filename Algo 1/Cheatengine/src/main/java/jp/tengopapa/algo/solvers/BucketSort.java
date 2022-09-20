package jp.tengopapa.algo.solvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BucketSort {
    public static List<BucketData> bucketSort(float[] a) {
        float maxDigits = 0;

        for(float f : a) {
            double digits = Math.floor(Math.log10(f) + 1);
            if(digits > maxDigits) {
                maxDigits = (float) digits;
            }
        }

        float mul = (float) Math.max(1, Math.pow(10, maxDigits));

        for(int i = 0; i < a.length; i++) {
            a[i] = a[i] / mul;
        }

        List<BucketData> bucketData = new ArrayList<>();

        float[] ret = new float[a.length];

        int n = a.length;
        List<Float>[] b = new List[n];

        for(int k = 0; k < n; k++) {
            BucketData bucketData1 = new BucketData();
            bucketData1.afterInsertion = new ArrayList<>();
            bucketData1.afterSorting = new ArrayList<>();
            bucketData1.key = k;

            bucketData.add(bucketData1);
        }

        for(int i = 0; i < n; i++) {
            b[i] = new ArrayList<>();
        }

        for(float i : a) {
            int k = (int) Math.floor(i * n);
            b[k].add(0, i);

            bucketData.get(k).afterInsertion.add(new ArrayList<>(b[k]));
        }

        for(int j = 0; j < n; j++) {
            Collections.sort(b[j]);

            bucketData.get(j).afterSorting = new ArrayList<>(b[j]);
        }

        int idx = 0;
        for(List<Float> fl : b) {
            for(float f : fl) {
                ret[idx++] = f * mul;
            }
        }

        System.arraycopy(ret, 0, a, 0, a.length);

        return bucketData;
    }

    public static class BucketData {
        public int key;
        public List<List<Float>> afterInsertion;
        public List<Float> afterSorting;
    }
}
