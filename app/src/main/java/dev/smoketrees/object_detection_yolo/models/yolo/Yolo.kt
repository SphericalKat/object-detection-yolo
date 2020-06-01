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

class Yolo(private val context: Context) {
    private val interpreter: Interpreter
    private val labels: List<String>

    fun detectObjects(bitmap: Bitmap): MutableList<String> {
        // create image processor to resize image to input dimensions
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        // create tensorflow representation of an image
        val tensorImage = TensorImage(DataType.UINT8)
            tensorImage.load(bitmap)

        val labelList = loadLabelList(context.assets)

        // resize image using processor
        val processedImage = imageProcessor.process(tensorImage)

        val inputbuffer = TensorBuffer.createFixedSize(intArrayOf(1, 300, 300, 3), DataType.UINT8)
        inputbuffer.loadBuffer(processedImage.buffer, intArrayOf(1, 300, 300, 3))

        val boundBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 10, 4), DataType.FLOAT32)
        val classBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32)
        val classProbBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32)
        val numBoxBuffer = TensorBuffer.createFixedSize(intArrayOf(1), DataType.FLOAT32)

        interpreter.runForMultipleInputsOutputs(arrayOf(inputbuffer.buffer), mapOf(
            0 to boundBuffer.buffer,
            1 to classBuffer.buffer,
            2 to classProbBuffer.buffer,
            3 to numBoxBuffer.buffer
        ))

//        Log.d("TAG", boundBuffer.floatArray.contentToString())
//        Log.d("TAG", classBuffer.floatArray.contentToString())
//        Log.d("TAG", classProbBuffer.floatArray.contentToString())
//        Log.d("TAG", boundBuffer.floatArray.contentToString())

        val classNames = mutableListOf<String>()

        classBuffer.floatArray.forEach {
            classNames.add(labelList[it.toInt()+1])
        }

        return classNames
    }

    private fun loadLabelList(
        assetManager: AssetManager
    ): List<String> {
        val labelList: MutableList<String> = ArrayList()
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
        labels = FileUtil.loadLabels(context, LABEL_FILE)
    }
}