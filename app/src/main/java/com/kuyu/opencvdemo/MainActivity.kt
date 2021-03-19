package com.kuyu.opencvdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_hello.setOnClickListener { startActivity(Intent(this@MainActivity, DetectActivity::class.java)) }
        tv_activity2.setOnClickListener {
            startActivity(Intent(this@MainActivity, MainActivity2::class.java))
        }
        tv_activity3.setOnClickListener{
            startActivity(Intent(this@MainActivity, OpenCVActivity3::class.java));
        }
//        requestPermission()
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            startActivity(Intent(this, DetectActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this, DetectActivity::class.java))
            } else {
                Toast.makeText(this, "权限拒绝", Toast.LENGTH_SHORT).show()
                tv_hello!!.isEnabled = false
            }
        }
    }
}
