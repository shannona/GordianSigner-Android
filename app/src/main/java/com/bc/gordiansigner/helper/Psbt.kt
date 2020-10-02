package com.bc.gordiansigner.helper

import com.blockstream.libwally.Wally.*

/**
 * The [Psbt] provides multiple low-level functions as utility to work with PSBT
 */
object Psbt {

    /**
     * Sign the PSBT in base64 with given private key and return new PSBT base64 string
     */
    fun sign(base64: String, prv: ByteArray) = psbt_from_base64(base64).let {
        psbt_sign(it, prv, 0x00)
        psbt_to_base64(it, 0)
    }
}