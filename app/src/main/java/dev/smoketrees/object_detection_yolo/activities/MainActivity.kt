package dev.smoketrees.object_detection_yolo.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import dev.smoketrees.object_detection_yolo.R
import dev.smoketrees.object_detection_yolo.models.yolo.Yolo
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var yolo: Yolo
    private var bitmapCrop1: Bitmap? = null
    private var bitmapCrop2: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runWithPermissions(Manifest.permission.CAMERA) {
            Toast.makeText(this, "Camera permissions granted", Toast.LENGTH_SHORT).show()
        }

        try {
            yolo = Yolo(this)
        } catch (e: IOException) {
            Log.e("TAG", "Error initing models", e)
        }

        capturebutton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        detectButton.setOnClickListener {
            val classes = yolo.detectObjects(bitmap1!!)
            resultTextView.text = classes.toString()
        }
    }


    companion object {
        var bitmap1: Bitmap? = null
    }
}