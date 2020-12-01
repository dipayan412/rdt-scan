package edu.washington.cs.ubicomplab.rdt_reader.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.IntStream;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class findPeaks {
    /**
     * numpy's find diff function
     *
     * @param inArr
     * @return diff Array
     */
    private static double[] npDiff(double[] inArr) {
        int arrLen = inArr.length - 1;
        double[] outArr = new double[arrLen];
        for (int i = 0; i < arrLen; i++) {
            outArr[i] = inArr[i + 1] - inArr[i];
        }

        return outArr;
    }

    /**
     * find index of certain values in the array
     *
     * @param dy
     * @param value
     * @return
     */

    private static ArrayList<Integer> findValue(double[] dy, double value) {
        ArrayList<Integer> zero_index = new ArrayList<>();
        for (int i = 0; i < dy.length; i++) {
            if (dy[i] == value) {
                zero_index.add(i);
            }
        }
        return zero_index;
    }

    /**
     * find index of elements excluding certain values in the array
     *
     * @param dy
     * @param value
     * @return
     */

    private static ArrayList<Integer> findNotValue(double[] dy, double value) {
        ArrayList<Integer> zero_index = new ArrayList<>();
        for (int i = 0; i < dy.length; i++) {
            if (dy[i] != value) {
                zero_index.add(i);
            }
        }
        return zero_index;
    }

    /**
     * convert arraylist to array
     *
     * @param arrayList
     * @return
     */

    private static double[] list2array(ArrayList<Integer> arrayList) {
        int arrlen = arrayList.size();
        double[] outArr = new double[arrlen];
        for (int i = 0; i < arrlen; i++) {
            outArr[i] = arrayList.get(i).doubleValue();
        }
        return outArr;
    }

    /**
     * convert double array to int array
     *
     * @param dbarr
     * @return
     */
    private static int[] double2int(double[] dbarr) {
        int arrlen = dbarr.length;
        int[] outArr = new int[arrlen];
        for (int i = 0; i < arrlen; i++) {
            outArr[i] = (int) dbarr[i];
        }
        return outArr;
    }

    /**
     * find plateau sections in the 1D signal
     *
     * @param zeros
     * @param zeros_diff_not_one
     * @return
     */
    private static ArrayList<int[]> findPlateaus(int[] zeros, int[] zeros_diff_not_one) {
        int newArrlen = zeros_diff_not_one.length + 2;
        int[] indexArr = new int[newArrlen];
        indexArr[0] = 0;
        indexArr[newArrlen - 1] = zeros.length;

        for (int i = 0; i < zeros_diff_not_one.length; i++) {
            indexArr[i + 1] = zeros_diff_not_one[i];
        }

        ArrayList<int[]> plateaus = new ArrayList<>();
        for (int i = 0; i < indexArr.length - 1; i++) {
            int startIndex = indexArr[i];
            int endIndex = indexArr[i + 1];
            int[] slice = new int[endIndex - startIndex];
            for (int j = 0; j < slice.length; j++) {
                slice[j] = zeros[startIndex + j];
            }
            plateaus.add(slice);

        }
        return plateaus;
    }

    /**
     * calculate the median of the array.
     *
     * @param arr
     * @return
     */
    private static double median(int[] arr) {
        // sort array
        Arrays.sort(arr);
        double median;
        // get count of scores
        int totalElements = arr.length;
        // check if total number of scores is even
        if (totalElements % 2 == 0) {
            double sumOfMiddleElements = arr[totalElements / 2] + arr[totalElements / 2 - 1];
            // calculate average of middle elements
            median = sumOfMiddleElements / 2;
        } else {
            // get the middle element
            median = arr[totalElements / 2];
        }
        return median;
    }

    /**
     * add element to end
     *
     * @param srcArray
     * @param elementToAdd
     * @return
     */
    public static double[] addElementatEnd(double[] srcArray, int elementToAdd) {
        double[] destArray = new double[srcArray.length + 1];

        for (int i = 0; i < srcArray.length; i++) {
            destArray[i] = srcArray[i];
        }

        destArray[destArray.length - 1] = elementToAdd;
        return destArray;
    }

    /**
     * add element to beginning
     *
     * @param srcArray
     * @param elementToAdd
     * @return
     */
    public static double[] addElementatStart(double[] srcArray, int elementToAdd) {
        double[] destArray = new double[srcArray.length + 1];

        for (int i = 0; i < srcArray.length; i++) {
            destArray[i + 1] = srcArray[i];
        }

        destArray[0] = elementToAdd;
        return destArray;
    }

    /**
     * @param inArr
     * @param value
     * @return
     */

    public static boolean[] greaterValue(double[] inArr, double value) {
        boolean[] outArr = new boolean[inArr.length];
        for (int i = 0; i < inArr.length; i++) {
            if (inArr[i] > value) {
                outArr[i] = true;
            } else {
                outArr[i] = false;
            }
        }
        return outArr;
    }

    /**
     * @param inArr
     * @param value
     * @return
     */

    public static boolean[] lesserValue(double[] inArr, double value) {
        boolean[] outArr = new boolean[inArr.length];
        for (int i = 0; i < inArr.length; i++) {
            if (inArr[i] < value) {
                outArr[i] = true;
            } else {
                outArr[i] = false;
            }
        }
        return outArr;
    }

    public static void flipud(double[] arr) {

        for (int i = 0; i < arr.length; i++) {
            arr[i] = 255 - arr[i];

        }
    }

    private static void filterDetectedPeaks(ArrayList<double[]> detectedPeaks, double threshold) {
        Iterator<double[]> iter=detectedPeaks.iterator();
        while (iter.hasNext()) {
            double testLine=iter.next()[3];
            if (testLine <= threshold) {
                iter.remove();
            }
        }
    }

    /**
     * This is a java implementation of simplified find_peaks method in scipy python
     *
     * @param yarr:      array of fitted values
     * @param threshold: peak height threshold values in the raw y array
     * @param min_dist   : minimal distance in terms of data points between neighboring peaks
     * @return a List of [peak_idx, peak_value, peak_width] for all detected peaks/troughs
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ArrayList<double[]> find_Peaks(double[] yarr, double threshold, int min_dist, boolean upPeak) {
        // flip the array upside down
        if (!upPeak) {
            flipud(yarr);
        }

        int arrLen = yarr.length;
        double[] dy = new double[arrLen - 1];

        double[] zerosArr;
        int[] zerosArr_int;
        double[] zeros_diff = new double[dy.length - 1];

        ArrayList<Integer> zeros;
        ArrayList<Integer> zeros_diff_not_one_list;

        double[] zeros_diff_not_one_double;
        int[] zeros_diff_not_one;

        ArrayList<int[]> zero_plateaus = new ArrayList<>();

        dy = npDiff(yarr);
        zeros = findValue(dy, 0);


        if (zeros.size() == dy.length) {
            return null;
        }

        if (zeros.size() > 0) {
            zerosArr = list2array(zeros);
            zeros_diff = npDiff(zerosArr);
        }

        zeros_diff_not_one_list = findNotValue(zeros_diff, 1);
        zeros_diff_not_one_double = list2array(zeros_diff_not_one_list);
        zeros_diff_not_one = double2int(zeros_diff_not_one_double);

        for (int i = 0; i < zeros_diff_not_one.length; i++) {
            zeros_diff_not_one[i] += 1;
        }

        zerosArr = list2array(zeros);
        zerosArr_int = double2int(zerosArr);

        if (zerosArr_int.length != 0) {
            zero_plateaus = findPlateaus(zerosArr_int, zeros_diff_not_one);

            for (int[] p : zero_plateaus) {
                for (int v : p
                ) {
                    System.out.println(v);
                }
            }

            // fix if leftmost value in dy is zero
            if (zero_plateaus.get(0)[0] == 0) {
                int[] head_index = zero_plateaus.get(0);
                int fix_index = zero_plateaus.get(0)[head_index.length - 1] + 1;
                double fixvalue = dy[fix_index];
                for (int i = 0; i < head_index.length; i++) {
                    dy[head_index[i]] = fixvalue;
                }
                zero_plateaus.remove(0);
            }

            // fix if rightmost value of dy is zero
            int zero_plateus_size = zero_plateaus.size();
            int[] tail_index = zero_plateaus.get(zero_plateus_size - 1);

            if (zero_plateus_size > 0 && tail_index[tail_index.length - 1] == dy.length - 1) {
                double fixvalue = dy[tail_index[0] - 1];
                for (int i = 0; i < tail_index.length; i++) {
                    dy[tail_index[i]] = fixvalue;
                }
                zero_plateaus.remove(zero_plateus_size - 1);
            }

            // for each chain of zero indexes

            for (int i = 0; i < zero_plateaus.size(); i++) {
                int[] plateau_index = zero_plateaus.get(i);
                double plateau_median = median(plateau_index);
                for (double index : plateau_index) {
                    if (index < plateau_median) {
                        dy[(int) index] = dy[plateau_index[0] - 1];
                    } else {
                        dy[(int) index] = dy[plateau_index[plateau_index.length - 1] + 1];
                    }

                }
            }

        }

        // find peak locations
        double[] dy1 = new double[dy.length + 1];
        double[] dy2 = new double[dy.length + 1];
        dy1 = addElementatEnd(dy, 0);
        dy2 = addElementatStart(dy, 0);

        boolean[] c1 = lesserValue(dy1, 0);
        boolean[] c2 = greaterValue(dy2, 0);
        boolean[] c3 = greaterValue(yarr, threshold);

        boolean[] peak_bool = new boolean[c1.length];
        ArrayList<Integer> peaks = new ArrayList<Integer>();
        ArrayList<Double> yatPeaks = new ArrayList<>();
        ArrayList<Integer> peak_index = new ArrayList<>();

        for (int i = 0; i < c1.length; i++) {
            peak_bool[i] = c1[i] && c2[i] && c3[i];
            if (peak_bool[i]) {
                peaks.add(i);
                yatPeaks.add(yarr[i]);
            }
        }

        if (peaks.size() > 1 && min_dist > 1) {
            ArrayList<Integer> sortedList = new ArrayList<>(peaks);
            Integer[] highest = IntStream.range(0, peaks.size()).boxed().sorted((i, j) -> Double.
                    compare(yatPeaks.get(i), yatPeaks.get(j))).map(i -> peaks.get(i)).toArray(x -> new Integer[x]);
            Collections.reverse(Arrays.asList(highest));
            boolean[] rem = new boolean[yarr.length];
            Arrays.fill(rem, true);
            // rem[peaks]=False
            for (int peak : peaks) {
                rem[peak] = false;
            }

            for (int peak : highest) {
                if (!rem[peak]) {
                    int startIndex = max(0, peak - min_dist);
                    int endIndex = min(peak + min_dist + 1, yarr.length - 1);
                    for (int i = startIndex; i < endIndex; i++) {
                        if (i != peak) {
                            rem[i] = true;
                        }
                    }
                }
            }
            for (int i = 0; i < rem.length; i++) {
                if (!rem[i]) {
                    peak_index.add(i);
                }
            }
        }

//        double [] testarr= {1.0,5.0,8.0,6,9};
//        double median=median(testarr);
//        System.out.println("median is: "+ median);

//        for (int v : zeros_diff_not_one) {
//            System.out.println(v);
//        }
//        for (double zd : zeros_diff) {
//            System.out.println(zd);
//        }

        ArrayList<double[]> outputPeaks = new ArrayList<>();

        for (int max_idx : peak_index) {
            double max_val = yarr[max_idx];
            double peakWidth = ImageUtil.measurePeakWidth(yarr, max_idx, true);
            double baseCorrectedPeakHeight = ImageUtil.peakLinearBaselineCorrected(yarr, peakWidth, max_idx);
            outputPeaks.add(new double[]{max_idx, max_val, peakWidth, baseCorrectedPeakHeight});
        }
        filterDetectedPeaks(outputPeaks,threshold);
        return outputPeaks;

    }

}
