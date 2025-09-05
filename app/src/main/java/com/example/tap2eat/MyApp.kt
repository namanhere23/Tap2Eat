package com.example.tap2eat

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import android.webkit.WebView

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        WebView.setWebContentsDebuggingEnabled(false)

        try {
            WebView(this)
        } catch (e: Exception) {
            Log.d("WebViewException", "Error initializing WebView")
        }
        
        // Handle VCN attribution tag errors
        try {
            // Initialize any VCN-related components here if needed
        } catch (e: Exception) {
            Log.d("VCNException", "VCN initialization error: ${e.message}")
        }
    }
}
