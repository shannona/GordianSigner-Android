package com.bc.gordiansigner.ui.account.add_account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.Error.FINGERPRINT_NOT_MATCH_ERROR
import com.bc.gordiansigner.helper.ext.SIMPLE_DATE_TIME_FORMAT
import com.bc.gordiansigner.helper.ext.toString
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.AccountService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Single
import java.util.*

class AddAccountViewModel(
    lifecycle: Lifecycle,
    private val accountService: AccountService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val importAccountLiveData = CompositeLiveData<Pair<KeyInfo, String>>()

    fun importWallet(phrase: String, alias: String, saveXpv: Boolean, keyInfo: KeyInfo?) {
        importAccountLiveData.add(
            rxLiveDataTransformer.single(
                accountService.importHDKeyWallet(phrase).flatMap { key ->
                    val importedKeyInfo = KeyInfo(key.fingerprintHex, alias, Date().toString(
                        SIMPLE_DATE_TIME_FORMAT
                    ), saveXpv)

                    if (keyInfo != null && keyInfo != importedKeyInfo) {
                        Single.error(FINGERPRINT_NOT_MATCH_ERROR)
                    } else {
                        accountService.saveKey(importedKeyInfo, key).map { Pair(importedKeyInfo, it.xprv) }
                    }
                }
            )
        )
    }
}