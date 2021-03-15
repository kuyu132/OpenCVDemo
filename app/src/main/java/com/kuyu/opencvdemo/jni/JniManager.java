package com.kuyu.opencvdemo.jni;

public class JniManager {

    static {
        System.loadLibrary("native-lib");
    }
}
