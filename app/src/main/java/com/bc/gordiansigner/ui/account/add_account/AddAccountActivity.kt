package com.bc.gordiansigner.ui.account.add_account

import android.view.MenuItem
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.ext.replaceSpaces
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import kotlinx.android.synthetic.main.activity_add_account.*
import javax.inject.Inject

class AddAccountActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: AddAccountViewModel

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes() = R.layout.activity_add_account

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        title = getString(R.string.import_account)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        buttonNext.setSafetyOnclickListener {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}