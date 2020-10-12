package com.bc.gordiansigner.ui.account.add_account

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.WalletService
import dagger.Module
import dagger.Provides

@Module
class AddAccountModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: AddAccountActivity,
        walletService: WalletService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = AddAccountViewModel(activity.lifecycle, walletService, rxLiveDataTransformer)
}