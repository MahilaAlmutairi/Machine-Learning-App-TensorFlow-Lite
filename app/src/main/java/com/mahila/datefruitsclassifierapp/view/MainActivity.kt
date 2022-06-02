package com.mahila.datefruitsclassifierapp.view

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mahila.datefruitsclassifierapp.databinding.ActivityMainBinding
import com.mahila.datefruitsclassifierapp.viewModel.DFCViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var image: Bitmap
    private val vm: DFCViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set up the views
        binding.imageDes.text = vm.setUpTextView()
        if (vm.setUpImageView() != null) {
            binding.imageView.setImageBitmap(vm.setUpImageView())
        }
        //Import image from gallery
        binding.selectImage.setOnClickListener {
            getContent.launch("image/*")
        }
        //Image capture
        binding.takeImage.setOnClickListener {
            takeImage.launch(null)
        }

        // Get intent, action and MIME type
        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if (type.startsWith("image/")) {
                handleSentImage(intent) // Handle single image being sent from  other apps
            }
        }
    }

    private fun handleSentImage(intent: Intent?) {
        //get the sent image from other apps store it in bitmap variable
        val uriImg = intent!!.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?

        val bitmap = MediaStore.Images.Media.getBitmap(
            this.contentResolver, uriImg
        )
        bitmap?.let {
            preProcessing(bitmap)


        }

    }

    //image selection process
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver, uri
            )
            preProcessing(bitmap)
        }

    //Capturing image process
    private val takeImage =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                preProcessing(bitmap)
            }

        }

    private fun preProcessing(bitmap: Bitmap){
        vm.selectedImage(bitmap)
        binding.imageView.setImageBitmap(vm.setUpImageView())
        image = vm.scaling(bitmap)

        //Get class of image from the model
        vm.classify(image)
        binding.imageDes.text = vm.setUpTextView()

    }



}