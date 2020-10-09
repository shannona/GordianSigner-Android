package com.bc.gordiansigner.ui.account

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.WalletService
import dagger.Module
import dagger.Provides

@Module
class AccountsModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: AccountsActivity,
        walletService: WalletService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = AccountsViewModel(activity.lifecycle, walletService, rxLiveDataTransformer)
}