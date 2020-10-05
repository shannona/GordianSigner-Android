package com.bc.gordiansigner.ui.add_account

import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.ext.replaceSpaces
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import kotlinx.android.synthetic.main.activity_add_account.*
import javax.inject.Inject

class AddAccountActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: AddAccountViewModel

    override fun layoutRes() = R.layout.activity_add_account

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        buttonNext.setOnClickListener {
            editText.text?.replaceSpaces()?.let { phrase ->
                viewModel.importWallet(phrase, Network.TEST)
            }
        }
    }

    override fun observe() {
        super.observe()

        viewModel.importAccountLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    finish()
                }

                res.isError() -> {
                    tvWarning.setText(R.string.import_failed_please_review_your_12)
                }
            }
        })
    }
}