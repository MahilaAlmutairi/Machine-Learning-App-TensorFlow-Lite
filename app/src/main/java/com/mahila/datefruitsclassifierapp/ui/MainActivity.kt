package com.mahila.datefruitsclassifierapp.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mahila.datefruitsclassifierapp.R
import com.mahila.datefruitsclassifierapp.databinding.ActivityMainBinding
import com.mahila.datefruitsclassifierapp.ml.DFModel
import com.mahila.datefruitsclassifierapp.viewModel.DFCViewModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var image: Bitmap
    private val imageSize = 32
    private val vm: DFCViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set up the views
        binding.imageDes.text = vm.setUpTextView()
        vm.setUpImageView().let { binding.imageView.setImageBitmap(it) }
        //Import image from gallery
        binding.selectImage.setOnClickListener {
            getContent.launch("image/*")
        }
        //Image capture
        binding.takeImage.setOnClickListener {
            takeImage.launch(null)
        }
    }

    //image selection process
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver, uri
            )
            vm.selectedImage(bitmap)
            binding.imageView.setImageBitmap(vm.setUpImageView())
            image = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
            //Get class of image from the model
            classify(image)
        }

    //Capturing image process
    private val takeImage =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                vm.selectedImage(bitmap)
                binding.imageView.setImageBitmap(vm.setUpImageView())
                image = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)

                //Get class of image from the model
                classify(image)
            }

        }

    //Image classification process
    private fun classify(image: Bitmap?) {

        val model = DFModel.newInstance(this)

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
            getString(R.string.Ajwa),
            getString(R.string.Rothana),
            getString(R.string.SukkaryMofetiland)
        )

        vm.imageDec(classes[maxPos])
        binding.imageDes.text = vm.setUpTextView()

        // Releases model resources if no longer used.
        model.close()
    }


}