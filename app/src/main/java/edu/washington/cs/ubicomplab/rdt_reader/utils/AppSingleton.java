package edu.washington.cs.ubicomplab.rdt_reader.utils;

import android.app.Application;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

public final class AppSingleton extends Application {
    private static AppSingleton sInstance;
    private byte[] mCapturedImageData;
    private byte[] mResultWindowData;
    private Mat refDescriptor;
    private MatOfKeyPoint refKeypoints;

    // Getters & Setters

    public Mat getRefDescriptor() {
        return refDescriptor;
    }

    public void setRefDescriptor(Mat refDescriptor) {
        this.refDescriptor = refDescriptor;
    }

    public MatOfKeyPoint getRefKeypoints() {
        return refKeypoints;
    }

    public void setRefKeypoints(MatOfKeyPoint refKeypoints) {
        this.refKeypoints = refKeypoints;
    }


    public byte[] getCapturedImageData() {
        return mCapturedImageData;
    }

    public byte[] getResultWindowData() {
        return mResultWindowData;
    }

    public void setData(byte[] capturedImageData, byte[] resultWindowData) {
        mCapturedImageData = capturedImageData;
        mResultWindowData = resultWindowData;
    }

    // Singleton code
    public static AppSingleton getInstance() {
        if(sInstance == null)
            sInstance = new AppSingleton();
        return sInstance;
    }

    private AppSingleton () {}

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
