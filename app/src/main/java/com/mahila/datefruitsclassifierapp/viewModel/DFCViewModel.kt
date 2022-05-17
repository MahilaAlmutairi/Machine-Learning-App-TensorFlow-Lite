package com.mahila.datefruitsclassifierapp.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

class DFCViewModel : ViewModel() {
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


}