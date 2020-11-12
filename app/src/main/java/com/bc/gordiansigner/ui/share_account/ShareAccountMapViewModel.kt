package com.bc.gordiansigner.ui.share_account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.ext.SIMPLE_DATE_TIME_FORMAT
import com.bc.gordiansigner.helper.ext.toString
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.Descriptor
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.AccountMapService
import com.bc.gordiansigner.service.AccountService
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.*

class ShareAccountMapViewModel(
    lifecycle: Lifecycle,
    private val accountMapService: AccountMapService,
    private val contactService: ContactService,
    private val accountService: AccountService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val accountMapLiveData = CompositeLiveData<String>()
    internal val accountMapStatusLiveData = CompositeLiveData<Pair<List<KeyInfo>, Descriptor>>()

    fun checkValidAccountMap(string: String) {
        accountMapStatusLiveData.add(
            rxLiveDataTransformer.single(
                accountMapService.getAccountMapInfo(string).flatMap { (_, descriptor) ->
                    val fingerprints = descriptor.validFingerprints()
                    if (fingerprints.isNotEmpty()) {
                        Single.zip(
                            contactService.getContactKeysInfo(),
                            accountService.getKeysInfo(),
                            BiFunction<List<KeyInfo>, List<KeyInfo>, Pair<List<KeyInfo>, Descriptor>> { contacts, keys ->
                                val keysInfo =
                                    keys.toMutableSet().also { it.addAll(contacts) }.toList()
                                val joinedSigner = fingerprints.map { fingerprint ->
                                    val index =
                                        keysInfo.indexOfFirst { it.fingerprint == fingerprint }
                                    if (index != -1) {
                                        keysInfo[index]
                                    } else {
                                        KeyInfo(
                                            fingerprint,
                                            "unknown",
                                            Date(),
                                            false
                                        )
                                    }
                                }
                                Pair(joinedSigner, descriptor)
                            }
                        )
                    } else {
                        Single.just(Pair(emptyList(), descriptor))
                    }
                }
            )
        )
    }

    fun updateAccountMap(accountMapString: String, xprv: String) {
        accountMapLiveData.add(rxLiveDataTransformer.single(
            accountMapService.getAccountMapInfo(accountMapString)
                .flatMap { (accountMap, descriptor) ->
                    val hdKey = HDKey(xprv)

                    val keyInfoSet = descriptor.validFingerprints()
                        .map {
                            KeyInfo.default(it, "", false)
                        }.toSet()

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