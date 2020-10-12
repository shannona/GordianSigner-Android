package com.bc.gordiansigner.ui.main

import android.util.Log
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.BaseViewModel
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import com.bc.gordiansigner.ui.account.AccountsActivity
import com.bc.gordiansigner.ui.share_account.ShareAccountMapActivity
import com.bc.gordiansigner.ui.sign.PsbtSignActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: MainViewModel

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        buttonImportAccount.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(AccountsActivity::class.java)
        }

        buttonConfirmAccount.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(ShareAccountMapActivity::class.java)
        }

        buttonSignPsbt.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(PsbtSignActivity::class.java)
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
                            R.drawable.ic_check_circle_24,
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