package com.bc.gordiansigner.ui.sign

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.Psbt
import com.bc.gordiansigner.service.TransactionService
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class PsbtSignViewModel(
    lifecycle: Lifecycle,
    private val walletService: WalletService,
    private val transactionService: TransactionService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val psbtSigningLiveData = CompositeLiveData<String>()
    internal val psbtCheckingLiveData = CompositeLiveData<Any>()

    fun checkPsbt(base64: String, network: Network = Network.TEST) {
        psbtCheckingLiveData.add(rxLiveDataTransformer.completable(
            Completable.fromCallable {
                Psbt(base64, network)
            }
        ))
    }

    fun signPsbt(base64: String, network: Network) {
        psbtSigningLiveData.add(rxLiveDataTransformer.single(Single.fromCallable {
            val psbt = Psbt(base64, network)
            if (!psbt.signable) {
                throw IllegalStateException("PSBT is unable to sign")
            }
            psbt
        }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io()).flatMap { psbt ->
            walletService.getLocalHDKeyXprvs().flatMap { hdKeys ->
                if (hdKeys.isEmpty()) {
                    Single.error(IllegalStateException("HD keys is empty"))
                } else {
                    val inputBip32DerivFingerprints =
                        psbt.inputBip32Derivs.map { it.fingerprintHex }
                    val hdKey =
                        hdKeys.firstOrNull() { inputBip32DerivFingerprints.contains(it.fingerprintHex) }
                    if (hdKey == null) {
                        Single.error(IllegalStateException("No HD key is able to sign this PSBT"))
                    } else {
                        val path =
                            psbt.inputBip32Derivs.first { it.fingerprintHex == hdKey.fingerprintHex }.path
                        transactionService.signPsbt(
                            psbt,
                            hdKey.derive(path)
                        )
                    }

                }
            }
        }
        ))
    }

}