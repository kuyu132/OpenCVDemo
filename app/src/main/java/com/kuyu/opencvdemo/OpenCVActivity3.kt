package com.kuyu.opencvdemo

import android.graphics.Point
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.util.Size
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.kuyu.cameralib.CameraApi
import com.kuyu.cameralib.ICameraApiCallback
import com.kuyu.opencvdemo.jni.JniManager
import kotlinx.android.synthetic.main.activity_opencv3.*
import java.io.File

class OpenCVActivity3 : AppCompatActivity() {

    private val jniManager: JniManager = JniManager()
    private val previewWidth = 1920
    private val previewHeight = 1080
    private var ratio = 0f
    private var mSurfaceViewWidth = 0
    private var mSurfaceViewHeight = 0
    private val cameraId = CameraApi.CAMERA_INDEX_FRONT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opencv3)
        initViews()
    }

    private fun initViews() {
        val size = Point()
        val display = windowManager.defaultDisplay
        display.getRealSize(size)
        mSurfaceViewWidth = size.x
        mSurfaceViewHeight = size.y
        btn_switch_camera.setOnClickListener {}
        surface_view.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                initCamera()
            }

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                ratio = previewWidth / previewHeight.toFloat()
                holder?.let {
                    CameraApi.getInstance().startPreview(it)
                    val path = File(Environment.getExternalStorageDirectory(), "lbpcascade_frontalface.xml").absolutePath
                    jniManager.init(path)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                CameraApi.getInstance().stopCamera()
            }
        })
    }

    private fun initCamera(){
        CameraApi.getInstance().setCameraId(cameraId)
        CameraApi.getInstance().setPreviewSize(Size(previewWidth, previewHeight))
        CameraApi.getInstance().initCamera(this@OpenCVActivity3, callback)
        CameraApi.getInstance().setFps(30).configCamera()
    }

    val callback = object : ICameraApiCallback {
        override fun onPreviewFrameCallback(data: ByteArray?, camera: Camera?) {
            camera?.addCallbackBuffer(data)
            jniManager.postData(data, mSurfaceViewWidth, mSurfaceViewHeight, cameraId)
        }

        override fun onNotSupportErrorTip(message: String?) {
        }

        override fun onCameraInit(camera: Camera?) {
        }
    }
}