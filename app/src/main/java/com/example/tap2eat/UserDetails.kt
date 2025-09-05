package com.example.tap2eat

import java.io.Serializable

data class UserDetails (
    var name: String?=null,
    var email: String?=null,
    var mobile: String?=null,
    var photo: String? = null
) : Serializable