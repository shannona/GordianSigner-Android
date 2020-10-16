package com.bc.gordiansigner.helper.ext

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

fun CharSequence.replaceSpaces() = this.replace("\\s+".toRegex(), " ").trim()

fun String.toQrCode(size: Int, isReverted: Boolean = false) = Single.fromCallable {
    val writer = MultiFormatWriter()
    val hints = mapOf(
        Pair(EncodeHintType.MARGIN, 2)
    )
    val bitMatrix = writer.encode(this, BarcodeFormat.QR_CODE, size, size, hints)
    bitMatrix.toBitmap(size, isReverted)
}.subscribeOn(Schedulers.io())