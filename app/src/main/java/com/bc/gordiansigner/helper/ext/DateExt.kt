package com.bc.gordiansigner.helper.ext

import java.text.SimpleDateFormat
import java.util.*

const val SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"

fun Date.toString(
    format: String
): String {
    return try {
        val formatter = SimpleDateFormat(format, Locale.ENGLISH)
        formatter.format(this)
    } catch (e: Throwable) {
        ""
    }
}