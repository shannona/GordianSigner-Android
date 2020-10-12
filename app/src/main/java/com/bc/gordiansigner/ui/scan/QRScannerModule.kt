package com.bc.gordiansigner.ui.scan

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.ui.DialogController
import dagger.Module
import dagger.Provides

@Module
class QRScannerModule {

    @ActivityScope
    @Provides
    fun provideDialogController(activity: QRScannerActivity) = DialogController(activity)
}