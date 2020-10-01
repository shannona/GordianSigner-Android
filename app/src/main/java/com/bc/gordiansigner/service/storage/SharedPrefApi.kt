package com.bc.gordiansigner.service.storage

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SharedPrefApi @Inject constructor(
    context: Context
) {

    companion object {
        // shared pref key comes here
    }

    private val sharePrefGateway = SharedPrefGateway(context)

    fun <T> rxSingle(action: (SharedPrefGateway) -> T): Single<T> {
        return Single.create(SingleOnSubscribe<T> { e ->
            try {
                e.onSuccess(action.invoke(sharePrefGateway))
            } catch (ex: Exception) {
                e.onError(ex)
            }
        }).subscribeOn(Schedulers.io())
    }

    fun rxCompletable(action: (SharedPrefGateway) -> Unit): Completable {
        return Completable.create { e ->
            try {
                action.invoke(sharePrefGateway)
                e.onComplete()
            } catch (ex: Exception) {
                e.onError(ex)
            }

        }.subscribeOn(Schedulers.io())
    }
}