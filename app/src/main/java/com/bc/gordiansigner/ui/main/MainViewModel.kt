package com.bc.gordiansigner.ui.main

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.BaseViewModel

class MainViewModel(
    lifecycle: Lifecycle,
    private val walletService: WalletService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val walletStateLiveData = CompositeLiveData<Boolean>()

    fun getWalletState() {
        walletStateLiveData.add(rxLiveDataTransformer.single(
            walletService.getHDKeyXprvs().map { it.isNotEmpty() }
        ))
    }

}