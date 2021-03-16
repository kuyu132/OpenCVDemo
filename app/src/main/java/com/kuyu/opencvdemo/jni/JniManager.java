package com.kuyu.opencvdemo.jni;

public class JniManager {

    static {
        System.loadLibrary("native-lib");
    }

    public native void init(String path);
}
