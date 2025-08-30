package com.example.tap2eat

import java.io.Serializable

data class UserDetails (
    var name: String,
    var email: String,
    var password: String,
    var mobile: String,
    var photo: String? = null
) : Serializable