package com.bc.gordiansigner.service

import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.ext.fromJson
import com.bc.gordiansigner.helper.ext.newGsonInstance
import com.bc.gordiansigner.model.Bip39Mnemonic
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.file.rxCompletable
import com.bc.gordiansigner.service.storage.file.rxSingle
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.security.SecureRandom
import javax.inject.Inject

class WalletService @Inject constructor(
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : BaseService(sharedPrefApi, fileStorageApi) {

    companion object {
        private const val XPRIV_KEY_FILE = "xpriv.secret"
        private const val KEY_INFO_FILE = "keyinfo.secret"
    }

    fun importHDKeyWallet(mnemonic: String, network: Network = Network.TEST): Single<HDKey> {
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

    fun getHDKey(fingerprint: String) =
        getHDKeys().map { keys -> keys.first { it.fingerprintHex == fingerprint } }

    fun getHDKeys() =
        fileStorageApi.SUPER_SECURE.rxSingle { gateway ->
            val privs = gateway.readOnFilesDir(XPRIV_KEY_FILE)
            if (privs.isEmpty()) {
                emptyList()
            } else {
                newGsonInstance().fromJson<List<String>>(String(privs))
            }
        }.map { set -> set.map { HDKey(it) } }

    fun getKeysInfo() = fileStorageApi.SECURE.rxSingle { gateway ->
        val json = gateway.readOnFilesDir(KEY_INFO_FILE)
        if (json.isEmpty()) {
            emptyList()
        } else {
            newGsonInstance().fromJson<List<KeyInfo>>(String(json))
        }
    }

    fun saveKey(keyInfo: KeyInfo, hdKey: HDKey) = saveKeyInfo(keyInfo).flatMap {
        if (keyInfo.isSaved) {
            saveHDKey(hdKey)
        } else {
            Single.just(hdKey)
        }
    }

    fun deleteHDKey(fingerprintHex: String): Completable = getHDKeys().flatMapCompletable { keys ->
        if (keys.any { it.fingerprintHex == fingerprintHex }) {
            val newKeys = keys.filterNot { it.fingerprintHex == fingerprintHex }.toSet()
            saveHDKeys(newKeys).andThen(updateKeyInfo(fingerprintHex, false))
        } else {
            Completable.complete()
        }
    }

    fun deleteKeyInfo(fingerprintHex: String) = getKeysInfo().flatMapCompletable { keysInfo ->
        if (keysInfo.any { it.fingerprint == fingerprintHex }) {
            val newKeys = keysInfo.filterNot { it.fingerprint == fingerprintHex }.toSet()
            saveKeysInfo(newKeys).andThen(deleteHDKey(fingerprintHex))
        } else {
            Completable.complete()
        }
    }

    private fun updateKeyInfo(fingerprint: String, isSaved: Boolean): Completable =
        getKeysInfo().flatMapCompletable { keysInfo ->
            val keyInfoSet = keysInfo.toMutableSet()
            keyInfoSet.firstOrNull { it.fingerprint == fingerprint }?.let {
                it.isSaved = isSaved
                saveKeysInfo(keyInfoSet)
            } ?: Completable.complete()
        }

    private fun saveHDKey(hdKey: HDKey) =
        getHDKeys().flatMap { keys ->
            val keySet = keys.toMutableSet()
            if (keySet.add(hdKey)) {
                saveHDKeys(keySet).andThen(Single.just(hdKey))
            } else {
                Single.just(hdKey)
            }
        }

    private fun saveKeyInfo(keyInfo: KeyInfo) = getKeysInfo().flatMap { keysInfo ->
        val keyInfoSet = keysInfo.toMutableSet()
        keyInfoSet.firstOrNull { it == keyInfo }?.let {
            it.alias = keyInfo.alias
            it.isSaved = keyInfo.isSaved
        } ?: let {
            keyInfoSet.add(keyInfo)
        }

        saveKeysInfo(keyInfoSet).andThen(Single.just(keyInfo))
    }

    private fun saveHDKeys(keys: Set<HDKey>) =
        fileStorageApi.SUPER_SECURE.rxCompletable { gateway ->
            gateway.writeOnFilesDir(
                XPRIV_KEY_FILE,
                newGsonInstance().toJson(keys.map { it.xprv }).toByteArray()
            )
        }

    private fun saveKeysInfo(keysInfo: Set<KeyInfo>) =
        fileStorageApi.SECURE.rxCompletable { gateway ->
            gateway.writeOnFilesDir(
                KEY_INFO_FILE,
                newGsonInstance().toJson(keysInfo).toByteArray()
            )
        }

}