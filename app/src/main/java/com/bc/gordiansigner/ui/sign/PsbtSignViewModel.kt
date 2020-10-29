package com.bc.gordiansigner.ui.sign

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.Error.NO_APPROPRIATE_HD_KEY_ERROR
import com.bc.gordiansigner.helper.Error.NO_HD_KEY_FOUND_ERROR
import com.bc.gordiansigner.helper.Error.PSBT_UNABLE_TO_SIGN_ERROR
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.model.Psbt
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.service.TransactionService
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class PsbtSignViewModel(
    lifecycle: Lifecycle,
    private val walletService: WalletService,
    private val contactService: ContactService,
    private val transactionService: TransactionService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val psbtSigningLiveData = CompositeLiveData<Pair<String, KeyInfo?>>()
    internal val psbtCheckingLiveData = CompositeLiveData<Psbt>()

    fun checkPsbt(base64: String) {
        psbtCheckingLiveData.add(rxLiveDataTransformer.single(
            Single.fromCallable {
                Psbt(base64)
            }
        ))
    }

    fun signPsbt(base64: String, xprv: String?) {
        psbtSigningLiveData.add(rxLiveDataTransformer.single(Single.fromCallable {
            val psbt = Psbt(base64)
            if (!psbt.signable) {
                throw PSBT_UNABLE_TO_SIGN_ERROR
            }
            psbt
        }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io()).flatMap { psbt ->
            if (xprv != null) {
                val hdKey = HDKey(xprv)
                transactionService.signPsbt(
                    psbt,
                    hdKey
                ).map { Pair(it, null) }
            } else {
                walletService.getKeysInfo().flatMap { keysInfo ->
                    if (keysInfo.isEmpty()) {
                        Single.error(NO_HD_KEY_FOUND_ERROR)
                    } else {
                        val inputBip32DerivFingerprints =
                            psbt.inputBip32Derivs.map { it.fingerprintHex }
                        val keyInfo =
                            keysInfo.firstOrNull { inputBip32DerivFingerprints.contains(it.fingerprint) }
                        if (keyInfo == null) {
                            Single.error(NO_APPROPRIATE_HD_KEY_ERROR)
                        } else {
                            val contactsKeyInfo = inputBip32DerivFingerprints
                                .filter { it != keyInfo.fingerprint }
                                .map { KeyInfo(it, "", false) }
                                .toSet()

                            contactService.appendContactKeysInfo(contactsKeyInfo)
                                .andThen(if (keyInfo.isSaved) {
                                    walletService.getHDKey(keyInfo.fingerprint).flatMap { hdKey ->
                                        transactionService.signPsbt(
                                            psbt,
                                            hdKey
                                        ).map { Pair(it, null) }
                                    }
                                } else {
                                    Single.just(Pair(base64, keyInfo))
                                })
                        }

                    }
                }
            }
        }))
    }
}