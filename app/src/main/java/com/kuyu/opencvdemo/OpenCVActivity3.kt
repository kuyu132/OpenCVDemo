package com.kuyu.opencvdemo

import android.graphics.Point
import android.hardware.Camera
import android.os.Bundle
import android.util.Size
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.kuyu.cameralib.CameraApi
import com.kuyu.cameralib.ICameraApiCallback
import com.kuyu.opencvdemo.jni.JniManager
import kotlinx.android.synthetic.main.activity_opencv3.*

class OpenCVActivity3 : AppCompatActivity() {

    private val jniManager: JniManager = JniManager()
    private val previewWidth = 1920
    private val previewHeight = 1080
    private var ratio = 0f
    private var mSurfaceViewWidth = 0
    private var mSurfaceViewHeight = 0

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
        btn_switch_camera.setOnClickListener {

        }
        surface_view.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                CameraApi.getInstance().setCameraId(CameraApi.CAMERA_INDEX_FRONT)
                CameraApi.getInstance().initCamera(this@OpenCVActivity3, callback)
                CameraApi.getInstance().setPreviewSize(Size(mSurfaceViewWidth, mSurfaceViewHeight))
                CameraApi.getInstance().setFps(30)
            }

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                ratio = previewWidth / previewHeight.toFloat()
                holder?.let {
                    CameraApi.getInstance().startPreview(it)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                CameraApi.getInstance().stopCamera()
            }
        })
    }

    val callback = object : ICameraApiCallback {
        override fun onPreviewFrameCallback(data: ByteArray?, camera: Camera?) {
            camera?.addCallbackBuffer(data)
        }

        override fun onNotSupportErrorTip(message: String?) {
        }

        override fun onCameraInit(camera: Camera?) {
        }
    }
}