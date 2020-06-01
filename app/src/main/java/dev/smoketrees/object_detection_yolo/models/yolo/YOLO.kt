package dev.smoketrees.object_detection_yolo.models.yolo

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.InputStreamReader

class YOLO(private val context: Context) {
    private val interpreter: Interpreter

    // lazily load object labels
    private val labelList by lazy { loadLabelList(context.assets) }

    // create image processor to resize image to input dimensions
    private val imageProcessor by lazy {
        ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
            .build()
    }

    // create tensorflow representation of an image
    private val tensorImage by lazy { TensorImage(DataType.UINT8) }

    /**
     * Detect objects in a bitmap
     *
     * @param bitmap an image containing the objects to be detected
     * @return a list of object names
     */
    fun detectObjects(bitmap: Bitmap): List<String> {
        tensorImage.load(bitmap)

        // resize image using processor
        val processedImage = imageProcessor.process(tensorImage)

        // load image data into input buffer
        val inputbuffer = TensorBuffer.createFixedSize(intArrayOf(1, 300, 300, 3), DataType.UINT8)
        inputbuffer.loadBuffer(processedImage.buffer, intArrayOf(1, 300, 300, 3))

        // create output buffers
        val boundBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 10, 4), DataType.FLOAT32)
        val classBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32)
        val classProbBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32)
        val numBoxBuffer = TensorBuffer.createFixedSize(intArrayOf(1), DataType.FLOAT32)

        // run interpreter
        interpreter.runForMultipleInputsOutputs(
            arrayOf(inputbuffer.buffer), mapOf(
                0 to boundBuffer.buffer,
                1 to classBuffer.buffer,
                2 to classProbBuffer.buffer,
                3 to numBoxBuffer.buffer
            )
        )

        // map and return classnames to detected categories
        return classBuffer.floatArray.map { labelList[it.toInt() + 1] }
    }

    /**
     * Loads a list of object labels from the app's assets
     *
     * @param assetManager An instance of [AssetManager] fetched from a [Context]
     * @return A list of class labels
     */
    private fun loadLabelList(
        assetManager: AssetManager
    ): List<String> {
        val labelList = mutableListOf<String>()
        val reader =
            BufferedReader(InputStreamReader(assetManager.open(LABEL_FILE)))
        var line = reader.readLine()
        while (line != null) {
            labelList.add(line)
            line = reader.readLine()
        }
        reader.close()
        return labelList
    }

    companion object {
        private const val MODEL_FILE = "detect.tflite"
        private const val LABEL_FILE = "labelmap.txt"
    }

    init {
        val options = Interpreter.Options()
        options.setNumThreads(8)
        interpreter = Interpreter(FileUtil.loadMappedFile(context, MODEL_FILE), options)
    }
}