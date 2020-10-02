package com.bc.gordiansigner.ui.main

import com.bc.gordiansigner.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    @ActivityScope
    fun provideVM(activity: MainActivity) = MainViewModel(activity.lifecycle)
}