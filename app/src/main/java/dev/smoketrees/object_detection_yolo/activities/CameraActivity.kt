@file:Suppress("DEPRECATION")

package dev.smoketrees.object_detection_yolo.activities

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import dev.smoketrees.object_detection_yolo.R
import kotlinx.android.synthetic.main.activity_camera.*


class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_camera)
        setupCamera()

        cameraFab.setOnClickListener {

            camera.takePicture()
        }
    }

    private fun setupCamera() {
        camera.setLifecycleOwner(this)
        camera.mapGesture(Gesture.PINCH, GestureAction.ZOOM)
        camera.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS)
        camera.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                result.toBitmap {bitmap ->
                    if (bitmap != null) {
                        MainActivity.clickedImage = bitmap
                        finish()
                    } else {
                        Toast.makeText(this@CameraActivity, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}