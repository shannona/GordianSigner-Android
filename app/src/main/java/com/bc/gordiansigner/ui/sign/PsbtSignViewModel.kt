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

class PsbtSignViewModel(
    lifecycle: Lifecycle,
    private val walletService: WalletService,
    private val transactionService: TransactionService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    companion object {
        //Hardcoded deriv path because current can't get from psbt
        private const val DERIV_PATH = "m"
    }

    internal val psbtLiveData = CompositeLiveData<String>()
    internal val psbtCheckingLiveData = CompositeLiveData<Any>()

    fun checkPsbt(base64: String, network: Network = Network.TEST) {
        psbtCheckingLiveData.add(rxLiveDataTransformer.completable(
            Completable.fromCallable {
                Psbt(base64, network)
            }
        ))
    }

    fun signPsbt(base64: String, network: Network) {
        psbtLiveData.add(rxLiveDataTransformer.single(
            walletService.getLocalHDKeyXprvs().flatMap { hdKeys ->
                if (hdKeys.isNotEmpty()) {
                    transactionService.signPsbt(
                        Psbt(base64, network),
                        hdKeys.first().derive(DERIV_PATH)
                    )
                } else {
                    Single.error(Throwable("Missing account, you need to import an account first!"))
                }
            }
        ))
    }

}