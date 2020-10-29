package com.bc.gordiansigner.ui.share_account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.Descriptor
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.AccountMapService
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class ShareAccountMapViewModel(
    lifecycle: Lifecycle,
    private val accountMapService: AccountMapService,
    private val contactService: ContactService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val accountMapLiveData = CompositeLiveData<String>()
    internal val accountMapStatusLiveData = CompositeLiveData<Pair<List<KeyInfo>, Descriptor>>()

    fun checkValidAccountMap(string: String) {
        accountMapStatusLiveData.add(
            rxLiveDataTransformer.single(
                Single.zip(
                    accountMapService.getAccountMapInfo(string),
                    contactService.getContactKeysInfo(),
                    BiFunction { (_, descriptor), contacts ->
                        val joinedSigner = descriptor.validFingerprints().map { fingerprint ->
                            val index = contacts.indexOfFirst { it.fingerprint == fingerprint }
                            if (index != -1) {
                                contacts[index]
                            } else {
                                KeyInfo(fingerprint, "unknown", false)
                            }
                        }
                        Pair(joinedSigner, descriptor)
                    }
                )
            )
        )
    }

    fun updateAccountMap(accountMapString: String, xprv: String) {
        accountMapLiveData.add(rxLiveDataTransformer.single(
            accountMapService.getAccountMapInfo(accountMapString)
                .flatMap { (accountMap, descriptor) ->
                    val hdKey = HDKey(xprv)

                    val keyInfoSet = descriptor.validFingerprints()
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