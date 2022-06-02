package com.mahila.datefruitsclassifierapp.app

import android.app.Application

class DFCApp: Application() {
    override fun onCreate() {
        super.onCreate()
        instant=this
    }
    companion object{
        lateinit var instant:Application
        private set
    }
}