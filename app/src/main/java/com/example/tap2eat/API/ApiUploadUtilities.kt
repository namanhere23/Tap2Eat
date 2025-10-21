package com.example.tap2eat.API

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUploadUtilities {
    fun getApiInterface():ApiUploadInterface{
        Log.d("Hello3","Hello3")
        return Retrofit.Builder()
            .baseUrl("https://tap2eat.onrender.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiUploadInterface::class.java)
    }
}