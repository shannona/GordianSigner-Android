package com.bc.gordiansigner.ui.sign

import android.util.Log
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.ext.*
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import kotlinx.android.synthetic.main.activity_psbt_sign.*
import javax.inject.Inject

class PsbtSignActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: PsbtSignViewModel

    override fun layoutRes() = R.layout.activity_psbt_sign

    override fun viewModel() = viewModel


    override fun initComponents() {
        super.initComponents()

        title = "PSBT Signer"

        buttonNext.setSafetyOnclickListener {
            val accountMapJson = editText.text.toString()
            viewModel.signPsbt(accountMapJson, Network.TEST)
        }

        buttonCopy.setSafetyOnclickListener {
            this.copyToClipboard(tvResult.text.toString())
            buttonCopy.animateWithString("Copied")
        }
    }

    override fun observe() {
        super.observe()

        viewModel.psbtLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    buttonCopy.visible()
                    tvResult.text = res.data()
                }

                res.isError() -> {
                    Log.d("PsbtSignActivity", res.throwable()?.message ?: "")
                    buttonCopy.gone()
                    tvResult.text = res.throwable()?.message
                }
            }
        })
    }
}