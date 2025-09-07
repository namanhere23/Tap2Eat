package com.example.tap2eat

import android.app.Application
import android.content.Intent
import android.util.Log
import com.google.firebase.FirebaseApp
import android.webkit.WebView
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)


}}
