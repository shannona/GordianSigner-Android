package com.bc.gordiansigner.ui.main

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    @ActivityScope
    fun provideVM(
        activity: MainActivity,
        walletService: WalletService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = MainViewModel(activity.lifecycle, walletService, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNavigator(activity: MainActivity) = Navigator(activity)
}