package com.example.tap2eat.API

import com.example.tap2eat.Utils.SECRET_KEY
import com.example.tap2eat.models.CustomerModel
import EphemeralKeyModel
import com.example.tap2eat.models.PaymentIntentModel
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {
    @Headers("Authorization: Bearer $SECRET_KEY")
    @POST("v1/customers")
    suspend fun getCustomer() : Response<CustomerModel>

    @Headers(
        "Authorization: Bearer $SECRET_KEY",
        "Stripe-Version: 2025-08-27.basil"
    )
    @FormUrlEncoded
    @POST("v1/ephemeral_keys")
    suspend fun getEphemeralKey(
        @Field("customer") customerId: String
    ): Response<EphemeralKeyModel>


    @Headers("Authorization: Bearer $SECRET_KEY")
    @FormUrlEncoded
    @POST("v1/payment_intents")
    suspend fun getPaymentIntent(
        @Field("customer") customerId: String,
        @Field("amount") amount: String,
        @Field("currency") currency: String = "inr",
        @Field("automatic_payment_methods[enabled]") automatePay: Boolean = true
    ): Response<PaymentIntentModel>
}