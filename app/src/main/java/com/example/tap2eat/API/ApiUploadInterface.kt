package com.example.tap2eat.API

import com.example.tap2eat.models.MediaModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiUploadInterface {
    @Multipart
    @POST("/api/v1/uploadMedia")
    suspend fun uploadMedia(
        @Part media: MultipartBody.Part
    ): Response<MediaModel>
}