package com.kuyu.opencvdemo.jni;

import android.view.Surface;

import org.jetbrains.annotations.Nullable;

public class JniManager {

    static {
        System.loadLibrary("native-lib");
    }

    public native void init(String path);

    public native void postData(byte[] data, int mSurfaceViewWidth, int mSurfaceViewHeight, int cameraIndexFront);

    public native void setSurface(Surface surface);
}
