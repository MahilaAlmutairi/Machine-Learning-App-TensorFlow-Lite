package com.mahila.datefruitsclassifierapp.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.mahila.datefruitsclassifierapp.R
import com.mahila.datefruitsclassifierapp.app.DFCApp.Companion.instant
import com.mahila.datefruitsclassifierapp.ml.DFModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DFCViewModel : ViewModel() {
    val imageSize = 32
    private var bitmapImage: Bitmap? = null
    private var imageDec = " "

    fun setUpImageView(): Bitmap? {
        return bitmapImage
    }

    fun selectedImage(_bitmapImage: Bitmap) {
        bitmapImage = _bitmapImage
    }

    fun setUpTextView(): String {
        return imageDec
    }

    fun imageDec(_imageDec: String) {
        imageDec = _imageDec
    }
    //Image classification process

    fun classify(image: Bitmap?) {
        val model = DFModel.newInstance( instant.applicationContext)

        // Creates inputs for reference.
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 32, 32, 3), DataType.FLOAT32)
        val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imageSize * imageSize)
        image?.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
        var pixel = 0
        //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val `val` = intValues[pixel++] // RGB
                byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 1))
                byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 1))
                byteBuffer.putFloat((`val` and 0xFF) * (1f / 1))
            }
        }
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val confidences = outputFeature0.floatArray
        // find the index of the class with the biggest confidence.
        var maxPos = 0
        var maxConfidence = 0f
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxPos = i
            }
        }
        val classes = arrayOf(
            instant.applicationContext.getString(R.string.Ajwa),
            instant.applicationContext.getString(R.string.Rothana),
            instant.applicationContext.getString(R.string.SukkaryMofetiland)
        )

       imageDec(classes[maxPos])

        // Releases model resources if no longer used.
        model.close()
    }

    fun scaling(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)

    }


}