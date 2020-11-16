package edu.washington.cs.ubicomplab.rdt_reader.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;
import android.util.Log;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ImageUtilTest {
//    double[] curve= null;
    int controlPeakLoc=37;
    int signalPeakLoc=108;
    static double [] curve=null;
    @BeforeAll
    static void beforeAll() {
        try {
            curve=loadResourceAsdoubleArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        System.out.println("Test properly set up");
    }


    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.out.println("Test is complete!");
    }

    public static double[] loadResourceAsdoubleArray() throws IOException {

        //Delimiter used in CSV file
        final String DELIMITER = ",";
        String[] tokens=null;
        BufferedReader fileReader = null;
        String fileToParse = "src\\test\\java\\edu\\washington\\cs\\ubicomplab\\rdt_reader\\utils\\resource\\testl.csv";

        try
        {
            String line = "";
            //Create the file reader
            fileReader = new BufferedReader(new FileReader(fileToParse));

            //Read the file line by line
            while ((line = fileReader.readLine()) != null)
            {
                //Get all tokens available in line
                tokens = line.split(DELIMITER);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        double[] parsed = new  double[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            parsed[i] = Double.parseDouble(tokens[i]);
        }
        return parsed;
    }

    @org.junit.jupiter.api.Test
    void peakLinearBaselineCorrectedControl() {
        double peakHeight= ImageUtil.peakLinearBaselineCorrected(curve,30,controlPeakLoc);
        assertEquals(peakHeight,95.52702700999998);
    }
    @org.junit.jupiter.api.Test
    void peakLinearBaselineCorrectedSignal() {
        double peakHeight= ImageUtil.peakLinearBaselineCorrected(curve,30,signalPeakLoc);
        assertEquals(peakHeight,138.41891891999998);
    }
    @org.junit.jupiter.api.Test
    void peakLinearBaselineCorrected0Width() {
        double peakHeight= ImageUtil.peakLinearBaselineCorrected(curve,0,signalPeakLoc);
        assertEquals(peakHeight,0);
    }

//    @Test
//    void sgfilterTest() {
//        ImageUtil imageUtil=new ImageUtil();
//        double [] smoothed=imageUtil.sgfilter.applySGfilter(curve);
//        assertEquals(smoothed[0],140.47700784064418);
//    }


    @Test
    void detectPeaks() {
        ArrayList <double []> dpks=ImageUtil.detectPeaks(curve,3,false);
        System.out.println("done");
    }
}

