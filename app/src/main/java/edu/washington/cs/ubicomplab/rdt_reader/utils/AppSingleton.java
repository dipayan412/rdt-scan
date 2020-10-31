package edu.washington.cs.ubicomplab.rdt_reader.utils;

import android.app.Application;

public final class AppSingleton extends Application {
    private static AppSingleton sInstance;
    private byte[] mCapturedImageData;
    private byte[] mResultWindowData;

    // Getters & Setters
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
