package com.bc.gordiansigner.di

import com.bc.gordiansigner.ui.main.MainActivity
import com.bc.gordiansigner.ui.main.MainModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = [MainModule::class])
    @ActivityScope
    internal abstract fun bindMainActivity(): MainActivity
}