package com.example.sic.mychat.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GeneralMessage {

    @SerializedName("type")
    @Expose
    var type: String? = null
    @SerializedName("data")
    @Expose
    var data: String? = null

}