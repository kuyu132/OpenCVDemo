package com.kuyu.cameralib;

import android.hardware.Camera;

public interface ICameraApiCallback {
    void onPreviewFrameCallback(byte[] data, Camera camera);
    void onNotSupportErrorTip(String message);

    void onCameraInit(Camera camera);
}
