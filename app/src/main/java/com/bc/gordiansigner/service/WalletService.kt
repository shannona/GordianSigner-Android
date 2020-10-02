package com.bc.gordiansigner.service

import com.bc.gordiansigner.helper.Bip39
import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.service.storage.FileStorageApi
import com.bc.gordiansigner.service.storage.SharedPrefApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.security.SecureRandom
import javax.inject.Inject

class WalletService @Inject constructor(
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : BaseService(sharedPrefApi, fileStorageApi) {

    fun importHDKeyWallet(mnemonic: String, network: Network): Single<HDKey> {
        return Single.fromCallable {
            val seed = Bip39.seedFromMnemonic(mnemonic)
            HDKey(seed, network)
        }.subscribeOn(Schedulers.io())
    }

    fun generateHDKeyWallet(network: Network): Single<HDKey> {
        val entropy = ByteArray(16)
        SecureRandom().nextBytes(entropy)
        val mnemonic = Bip39.mnemonicFromBytes(entropy)

        return importHDKeyWallet(mnemonic, network)
    }
}