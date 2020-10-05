package com.bc.gordiansigner.service.storage.sharedpref

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.bc.gordiansigner.BuildConfig

class SecureSharedPref internal constructor(context: Context) : SharedPref() {

    override val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        BuildConfig.APPLICATION_ID + "_secure",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

}