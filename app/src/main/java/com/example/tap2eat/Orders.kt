package com.example.tap2eat

import com.stripe.model.billingportal.Configuration.Features.SubscriptionUpdate.Product

data class Orders(
    val items: List<String> = emptyList(),
    val amount: Int = 0,
    val timestamp: Long = 0L,
    val status: String = ""
)
