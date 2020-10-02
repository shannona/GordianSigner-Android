package com.bc.gordiansigner.helper

import com.blockstream.libwally.Wally
import com.blockstream.libwally.Wally.*

/**
 * The [Bip39] provides multiple low-level functions as utility to work with BIP39
 */
object Bip39 {

    /**
     * Get mnemonic words from given byte array in default language
     */
    fun mnemonicFromBytes(bytes: ByteArray) =
        bip39_mnemonic_from_bytes(bip39_get_wordlist(null), bytes)

    /**
     * Get seed from given mnemonic words in byte array
     */
    fun seedFromMnemonic(mnemonic: String) = ByteArray(Wally.BIP39_SEED_LEN_512).apply {
        bip39_mnemonic_to_seed(mnemonic, null, this)
    }
}