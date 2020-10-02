package com.bc.gordiansigner.helper

import com.bc.gordiansigner.model.KeyPair
import com.blockstream.libwally.Wally.*

/**
 * The [Bip32] provides multiple low-level functions as utility to work with BIP32
 */
object Bip32 {

    /**
     * Securely randomize an entropy byte array with given length
     */
    fun randomEntropy(length: Int) {
        throw UnsupportedOperationException()
    }

    /**
     * Generate keypair from given seed for corresponding network
     */
    fun generateKeyPair(network: Network, seed: ByteArray) = bip32_key_from_seed(
        seed, (if (network == Network.TEST) {
            BIP32_VER_TEST_PRIVATE
        } else {
            BIP32_VER_MAIN_PRIVATE
        }).toLong(), 0
    ).let { root ->
        val base58Xprv = bip32_key_to_base58(root, BIP32_FLAG_KEY_PRIVATE.toLong())
        val base58Xpub = bip32_key_to_base58(root, BIP32_FLAG_KEY_PUBLIC.toLong())
        KeyPair(base58_to_bytes(base58Xprv), base58_to_bytes(base58Xpub))
    }

}