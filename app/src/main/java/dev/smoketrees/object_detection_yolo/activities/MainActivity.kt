package dev.smoketrees.object_detection_yolo.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import dev.smoketrees.object_detection_yolo.R
import dev.smoketrees.object_detection_yolo.models.yolo.YOLO
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        var clickedImage: Bitmap? = null
    }

    private val yolo by lazy { YOLO(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check for camera perms and open CameraActivity
        capturebutton.setOnClickListener {
            runWithPermissions(Manifest.permission.CAMERA) {
                startActivity(Intent(this, CameraActivity::class.java))
            }
        }

        // detect classes from image
        detectButton.setOnClickListener {
            if (clickedImage == null) {
                Toast.makeText(this, "You need to take a picture first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val classes = yolo.detectObjects(clickedImage!!)
            resultTextView.text = classes.toString()
        }
    }
}