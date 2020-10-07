package com.bc.gordiansigner.ui.main

import android.content.Intent
import android.util.Log
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.BaseViewModel
import com.bc.gordiansigner.ui.add_account.AddAccountActivity
import com.bc.gordiansigner.ui.share_account.ShareAccountMapActivity
import com.bc.gordiansigner.ui.sign.PsbtSignActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: MainViewModel

    override fun layoutRes(): Int = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        buttonImportAccount.setSafetyOnclickListener {
            val intent = Intent(this, AddAccountActivity::class.java)
            startActivity(intent)
        }

        buttonConfirmAccount.setSafetyOnclickListener {
            val intent = Intent(this, ShareAccountMapActivity::class.java)
            startActivity(intent)
        }

        buttonSignPsbt.setSafetyOnclickListener {
            val intent = Intent(this, PsbtSignActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getWalletState()
    }

    override fun observe() {
        super.observe()

        viewModel.walletStateLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    if (res.data() == true) {
                        buttonImportAccount.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_baseline_check_circle_24,
                            0
                        )
                    }
                }

                res.isError() -> {
                    Log.e("MainActivity", res.throwable()?.message.toString())
                }
            }
        })
    }
}