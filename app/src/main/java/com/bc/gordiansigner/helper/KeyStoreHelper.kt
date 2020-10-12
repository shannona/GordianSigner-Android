package com.bc.gordiansigner.helper

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.bc.gordiansigner.helper.ext.isStrongboxBacked
import java.security.GeneralSecurityException
import javax.crypto.KeyGenerator

object KeyStoreHelper {

    @Throws(GeneralSecurityException::class)
    fun generateKey(keyGenParameterSpec: KeyGenParameterSpec): String {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
        return keyGenParameterSpec.keystoreAlias
    }

    fun buildSuperSecureMasterKeySpec(
        context: Context,
        keyAlias: String,
        keySize: Int
    ): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(60, KeyProperties.AUTH_BIOMETRIC_STRONG)
            .setKeySize(keySize)
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P && context.isStrongboxBacked()) {
            builder.setIsStrongBoxBacked(true)
        }
        return builder.build()
    }
}