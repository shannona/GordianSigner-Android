package com.bc.gordiansigner.ui.account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Single

class AccountsViewModel(
    lifecycle: Lifecycle,
    private val walletService: WalletService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val hdKeyFingerprintsLiveData = CompositeLiveData<List<KeyInfo>>()
    internal val deleteKeysLiveData = CompositeLiveData<String>()

    fun fetchHDKeyFingerprints() {
        hdKeyFingerprintsLiveData.add(
            rxLiveDataTransformer.single(
                walletService.getKeysInfo()
            )
        )
    }

    fun deleteAccount(fingerprintHex: String) {
        deleteKeysLiveData.add(
            rxLiveDataTransformer.single(
                walletService.deleteKeyInfo(fingerprintHex).andThen(Single.just(fingerprintHex))
            )
        )
    }

    fun deletePrivateKey(fingerprintHex: String) {
        deleteKeysLiveData.add(
            rxLiveDataTransformer.single(
                walletService.deleteHDKey(fingerprintHex).andThen(Single.just(fingerprintHex))
            )
        )
    }
}