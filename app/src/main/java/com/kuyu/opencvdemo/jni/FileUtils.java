package com.kuyu.opencvdemo.jni;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    public static String copyFileToData(Context context, String assetName) {
        String path = context.getFilesDir().getAbsolutePath() + File.separator + assetName;
        File file = new File(path);
        if (file.exists()) {
            return path;
        }
        AssetManager assetManager = context.getAssets();
        try {
            InputStream ais = assetManager.open(assetName);
            FileOutputStream fos = context.openFileOutput(assetName, Context.MODE_PRIVATE);
            final int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            int readlength = 0;
            do {
                readlength = ais.read(buffer, 0, bufferSize);
                if (readlength < 0) {
                    break;
                }
                fos.write(buffer, 0, readlength);
            } while (readlength > 0);
            fos.close();
            ais.close();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
