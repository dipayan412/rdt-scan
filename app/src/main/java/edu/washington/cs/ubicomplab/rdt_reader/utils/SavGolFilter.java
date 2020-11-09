package edu.washington.cs.ubicomplab.rdt_reader.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import org.apache.commons.math3.util.MathArrays;

import java.util.Arrays;
import java.util.stream.IntStream;


public class SavGolFilter {

    public static double[] coefficients;

    private static double[] calculateExponential(int[] arr, int exp) {
        int len=arr.length;
        double[] output=new double[len];
        for (int i = 0; i < len; i++) {
            double dval=arr[i];
            double dvalExp = Math.pow(dval, exp);
            output[i]=dvalExp;
        }
        return output;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static double[] calculateSGCoefficients(int sideN, int polyorder){
        int windowsize=sideN*2+1;
        Array2DRowRealMatrix A = new Array2DRowRealMatrix(windowsize,polyorder+1);

        double[] column1 = new double[windowsize];
        int[] column2 = IntStream.range(-sideN,sideN+1).toArray();

        Arrays.fill(column1,1);
        A.setColumn(0, column1);
        A.setColumn(1, Arrays.stream(column2).asDoubleStream().toArray());

        if (polyorder>1) {
            for (int i = 2; i <= polyorder; i++) {
                double[] column = calculateExponential(column2,i);
                A.setColumn(i, Arrays.stream(column).toArray());
            }
        }

        SingularValueDecomposition svd=new SingularValueDecomposition(A);
        DecompositionSolver solver=svd.getSolver();
        RealMatrix pinv=solver.getInverse();

        double [] coeff=pinv.getRow(0);

//        System.out.println(Arrays.deepToString(app));
//        System.out.println(Arrays.toString(column2));
//        System.out.println(Arrays.toString(coeff));

        return coeff;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static double[] applySGfilter(double[] signal) {
        if (coefficients == null) {
            coefficients=calculateSGCoefficients(5,2);
        }

        int len=coefficients.length;
        int padding=(len-1)/2;

        double[] head=new double[padding];
        Arrays.fill(head,signal[0]);
        double[] tail=new double[padding];
        Arrays.fill(tail,signal[signal.length-1]);

        double[] merged=merge(head,signal,tail);

        double[] convSignal=MathArrays.convolve(merged,coefficients);

        double[] output=Arrays.copyOfRange(convSignal,2*padding,convSignal.length-2*padding);
        return output;
    }

    /***
     * function to append additional padding arrays to the signal array
     * @param arrays   : double [] arrays to be concatenated
     * @return double [] concatenated array
     */
    public static double[] merge(double[]... arrays)
    {
        int finalLength = 0;
        for (double[] array : arrays) {
            finalLength += array.length;
        }

        double[] dest = null;
        int destPos = 0;

        for (double[] array : arrays)
        {
            if (dest == null) {
                dest = Arrays.copyOf(array, finalLength);
                destPos = array.length;
            } else {
                System.arraycopy(array, 0, dest, destPos, array.length);
                destPos += array.length;
            }
        }
        return dest;
    }
}


