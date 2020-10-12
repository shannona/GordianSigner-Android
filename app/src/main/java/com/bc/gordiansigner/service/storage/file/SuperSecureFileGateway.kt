package com.bc.gordiansigner.service.storage.file

import android.content.Context
import androidx.security.crypto.MasterKeys
import com.bc.gordiansigner.helper.KeyStoreHelper

/**
 * This level of security requires user authentication (device biometric) each time using the key.
 * It also use hardware-backed if API level is 28 or above.
 * Throws [IllegalStateException] if device biometric auth has not been setup.
 * Throws [java.security.KeyStoreException] with root cause [android.security.keystore.UserNotAuthenticatedException] if user has not authenticated by biometric.
 */
class SuperSecureFileGateway internal constructor(context: Context) : SecureFileGateway(context) {

    companion object {
        private const val MASTER_KEY_ALIAS = "super_secure_file_master_key_alias"
        private const val MASTER_KEY_SIZE = 256
    }

    override val MASTER_KEY_ALIAS: String
        get() = MasterKeys.getOrCreate(
            KeyStoreHelper.buildSuperSecureMasterKeySpec(
                context,
                SuperSecureFileGateway.MASTER_KEY_ALIAS,
                SuperSecureFileGateway.MASTER_KEY_SIZE
            )
        )
}