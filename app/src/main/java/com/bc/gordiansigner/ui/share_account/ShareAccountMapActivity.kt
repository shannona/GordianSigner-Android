package com.bc.gordiansigner.ui.share_account

import android.util.Log
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import kotlinx.android.synthetic.main.activity_share_account_map.*
import javax.inject.Inject

//TODO: Test Activity to receive & fill partial account map with wallet
class ShareAccountMapActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: ShareAccountMapViewModel

    override fun layoutRes() = R.layout.activity_share_account_map

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        //TODO: Hardcoded test data
        val mnemonic = "modify tip timber tissue mandate april title unable valley spawn athlete harsh"
        val accountMapJson = "{\n" +
                "\"descriptor\":\"wsh(sortedmulti(2,[83hf9h94/48h/0h/0h/2h]xpub6CMZuJmP86KE8gaLyDNCQJWWzfGvWfDPhepzBG3keCLZPZ6XPXzsU82ms8BZwCUVR2UxrsDRa2YQ6nSmYbASTqadhRDp2qqd37UvFksA3wT,[<fingerprint>/48h/0h/0h/2h]<xpub>,[<fingerprint>/48h/0h/0h/2h]<xpub>))\",\n" +
                "\"blockheight\":1781992,\n" +
                "\"label\":\"warm test\"\n" +
                "}"

        viewModel.updateAccountMap(accountMapJson, mnemonic)
    }

    override fun observe() {
        super.observe()

        viewModel.accountMapLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    tvResult.text = res.data()
                }

                res.isError() -> {
                    Log.d("ShareAccountMapActivity", res.throwable()?.message ?: "")
                }
            }
        })
    }
}