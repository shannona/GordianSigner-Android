package com.bc.gordiansigner.helper.ext

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.bc.gordiansigner.ui.Navigator

fun Navigator.openAppSetting(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        startActivity(intent)
    } catch (ignore: Throwable) {
    }
}