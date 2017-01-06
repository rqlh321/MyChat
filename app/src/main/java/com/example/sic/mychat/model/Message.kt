package com.example.sic.mychat.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Message {

    @SerializedName("name")
    @Expose
    var name: String? = null
    @SerializedName("message")
    @Expose
    var message: String? = null

    constructor(name: String?, message: String?) {
        this.name = name
        this.message = message
    }
}