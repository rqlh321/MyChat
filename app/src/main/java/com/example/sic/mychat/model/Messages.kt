package com.example.sic.mychat.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Messages {

    @SerializedName("messages")
    @Expose
    var messages: List<Message>? = null

}