package com.bc.gordiansigner.ui.share_account

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.*
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import kotlinx.android.synthetic.main.activity_share_account_map.*
import javax.inject.Inject

class ShareAccountMapActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: ShareAccountMapViewModel

    override fun layoutRes() = R.layout.activity_share_account_map

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        title = "Fill Account Map"

        buttonFill.setSafetyOnclickListener {
            val accountMapJson = editText.text.toString()
            viewModel.updateAccountMap(accountMapJson)
        }

        buttonCopy.setSafetyOnclickListener {
            this.copyToClipboard(tvResult.text.toString())
            buttonCopy.animateWithString("Copied")
        }
    }

    override fun observe() {
        super.observe()

        viewModel.accountMapLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    buttonCopy.visible()
                    tvResult.text = res.data()
                }

                res.isError() -> {
                    Log.d("ShareAccountMapActivity", res.throwable()?.message ?: "")
                    buttonCopy.gone()
                    tvResult.text = res.throwable()?.message
                }
            }
        })
    }
}