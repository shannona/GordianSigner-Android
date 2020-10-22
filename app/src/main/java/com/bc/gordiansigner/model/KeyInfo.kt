package com.bc.gordiansigner.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class KeyInfo(

    @Expose
    @SerializedName("fingerprint")
    val fingerprint: String,

    @Expose
    @SerializedName("alias")
    var alias: String,

    @Expose
    @SerializedName("is_saved")
    var isSaved: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyInfo

        if (fingerprint != other.fingerprint) return false
        return true
    }

    override fun hashCode(): Int {
        return fingerprint.hashCode()
    }
}