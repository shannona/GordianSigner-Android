package com.bc.gordiansigner.ui.sign

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.Error.PSBT_UNABLE_TO_SIGN_ERROR
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.model.Psbt
import com.bc.gordiansigner.service.AccountService
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.service.TransactionService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class PsbtSignViewModel(
    lifecycle: Lifecycle,
    private val accountService: AccountService,
    private val contactService: ContactService,
    private val transactionService: TransactionService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val psbtSigningLiveData = CompositeLiveData<String>()
    internal val getKeyToSignLiveData = CompositeLiveData<KeyInfo>()
    internal val psbtCheckingLiveData = CompositeLiveData<Pair<List<KeyInfo>, Psbt>>()

    fun checkPsbt(base64: String) {
        psbtCheckingLiveData.add(rxLiveDataTransformer.single(
            Single.fromCallable {
                Psbt(base64)
            }.subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .flatMap { psbt ->
                    Single.zip(
                        contactService.getContactKeysInfo(),
                        accountService.getKeysInfo(),
                        BiFunction<List<KeyInfo>, List<KeyInfo>, List<KeyInfo>> { contacts, keys ->
                            keys.toMutableSet().also { it.addAll(contacts) }.toList()
                        }
                    ).map { keysInfo ->
                        val joinedSigners = psbt.inputBip32Derivs.map { bip32Deriv ->
                            val index =
                                keysInfo.indexOfFirst { it.fingerprint == bip32Deriv.fingerprintHex }
                            if (index != -1) {
                                keysInfo[index]
                            } else {
                                KeyInfo.unknown(bip32Deriv.fingerprintHex)
                            }
                        }
                        Pair(joinedSigners, psbt)
                    }
                }
        ))
    }

    fun getKeyToSign(base64: String) {
        getKeyToSignLiveData.add(rxLiveDataTransformer.single(Single.fromCallable {
            val psbt = Psbt(base64)
            if (!psbt.signable) {
                throw PSBT_UNABLE_TO_SIGN_ERROR
            }
            psbt
        }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io()).flatMap { psbt ->
            accountService.getKeysInfo().flatMap { keysInfo ->
                if (keysInfo.isEmpty()) {
                    Single.just(KeyInfo.empty())
                } else {
                    val inputBip32DerivFingerprints =
                        psbt.inputBip32Derivs.map { it.fingerprintHex }
                    val keyInfo =
                        keysInfo.firstOrNull { inputBip32DerivFingerprints.contains(it.fingerprint) }
                    if (keyInfo == null) {
                        Single.just(KeyInfo.empty())
                    } else {
                        val contactsKeyInfo = inputBip32DerivFingerprints
                            .filter { it != keyInfo.fingerprint }
                            .map { KeyInfo.default(it, "", false) }
                            .toSet()

                        contactService.appendContactKeysInfo(contactsKeyInfo)
                            .andThen(Single.just(keyInfo))
                    }
                }
            }
        }))
    }

    fun signPsbt(base64: String, keyInfo: KeyInfo, xprv: String?) {
        psbtSigningLiveData.add(rxLiveDataTransformer.single(Single.fromCallable {
            val psbt = Psbt(base64)
            if (!psbt.signable) {
                throw PSBT_UNABLE_TO_SIGN_ERROR
            }
            psbt
        }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io()).flatMap { psbt ->
            if (xprv != null) {
                Single.just(HDKey(xprv))
            } else {
                accountService.getHDKey(keyInfo.fingerprint)
            }.flatMap { hdKey ->
                transactionService.signPsbt(
                    psbt,
                    hdKey
                )
            }
        }))
    }
}