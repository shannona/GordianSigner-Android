package com.bc.gordiansigner.ui.share_account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.FINGERPRINT_REGEX
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.AccountMapService
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Completable

class ShareAccountMapViewModel(
    lifecycle: Lifecycle,
    private val accountMapService: AccountMapService,
    private val contactService: ContactService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val accountMapLiveData = CompositeLiveData<String>()
    internal val accountMapStatusLiveData = CompositeLiveData<Any>()

    fun checkValidAccountMap(string: String) {
        accountMapStatusLiveData.add(
            rxLiveDataTransformer.completable(
                accountMapService.getAccountMapInfo(string).ignoreElement()
            )
        )
    }

    fun updateAccountMap(accountMapString: String, xprv: String) {
        accountMapLiveData.add(rxLiveDataTransformer.single(
            accountMapService.getAccountMapInfo(accountMapString)
                .flatMap { (accountMap, descriptor) ->
                    val hdKey = HDKey(xprv)

                    val keyInfoSet = descriptor.fingerprints
                        .map { it.replace("[\\[\\]]".toRegex(), "") }
                        .filter { Regex(FINGERPRINT_REGEX).matches(it) }
                        .map { KeyInfo(it, "", false) }.toSet()

                    if (keyInfoSet.isNotEmpty()) {
                        contactService.appendContactKeysInfo(keyInfoSet)
                    } else {
                        Completable.complete()
                    }.andThen(
                        accountMapService.fillPartialAccountMap(
                            accountMap,
                            descriptor,
                            hdKey
                        )
                    )
                }
        ))
    }
}