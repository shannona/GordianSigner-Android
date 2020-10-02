package com.bc.gordiansigner.ui.main

import com.bc.gordiansigner.R
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.BaseViewModel
import javax.inject.Inject

class MainActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: MainViewModel

    override fun layoutRes(): Int = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = viewModel

}