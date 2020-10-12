package com.bc.gordiansigner.service

import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.model.Bip39Mnemonic
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefKey.ROOT_XPRV_KEYS
import com.bc.gordiansigner.service.storage.sharedpref.rxCompletable
import com.bc.gordiansigner.service.storage.sharedpref.rxSingle
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
            val seed = Bip39Mnemonic(mnemonic).seed
            HDKey(seed, network)
        }.subscribeOn(Schedulers.computation())
    }

    fun generateHDKeyWallet(network: Network): Single<HDKey> {
        return Single.fromCallable {
            val entropy = ByteArray(16)
            SecureRandom().nextBytes(entropy)
            val seed = Bip39Mnemonic(entropy).seed
            HDKey(seed, network)
        }.subscribeOn(Schedulers.computation())
    }

    /** BEGIN -- simple mechanism getting & saving account by storing XPRVS **/

    fun getLocalHDKeyXprvs() =
        sharedPrefApi.SECURE.rxSingle { sharedPref ->
            sharedPref.get(ROOT_XPRV_KEYS, Set::class)
        }.map { set -> set.map { HDKey(it as String) } }

    fun saveHDKeyXprv(hdKey: HDKey) =
        getLocalHDKeyXprvs().map {
            it.toMutableSet().apply {
                add(hdKey)
            }
        }.flatMapCompletable { keys ->
            saveHDKeyXprvs(keys)
        }.andThen(Single.just(hdKey))

    fun deleteKey(fingerprintHex: String) =
        getLocalHDKeyXprvs().map {
            it.filter { key -> key.fingerprintHex != fingerprintHex }.toSet()
        }.flatMapCompletable { keys ->
            saveHDKeyXprvs(keys)
        }

    private fun saveHDKeyXprvs(keys: Set<HDKey>) =
        sharedPrefApi.SECURE.rxCompletable { sharedPref ->
            sharedPref.put(ROOT_XPRV_KEYS, keys.map { it.xprv }.toSet())
        }

    /** END - simple mechanism getting & saving account by storing XPRVS **/

}