package com.bc.gordiansigner.ui.account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Single

class AccountsViewModel(
    lifecycle: Lifecycle,
    private val walletService: WalletService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val hdKeysLiveData = CompositeLiveData<List<HDKey>>()
    internal val deleteKeysLiveData = CompositeLiveData<String>()

    fun fetchWallets() {
        hdKeysLiveData.add(
            rxLiveDataTransformer.single(
                walletService.getLocalHDKeyXprvs()
            )
        )
    }

    fun deleteAccount(fingerprintHex: String) {
        deleteKeysLiveData.add(
            rxLiveDataTransformer.single(
                walletService.deleteKey(fingerprintHex).andThen(Single.just(fingerprintHex))
            )
        )
    }
}