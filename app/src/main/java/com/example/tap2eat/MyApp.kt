package com.example.tap2eat

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import android.webkit.WebView

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

    }
}
