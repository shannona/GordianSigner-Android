package com.bc.gordiansigner.ui.share_account

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.*
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.account.AccountsActivity
import com.bc.gordiansigner.ui.scan.QRScannerActivity
import kotlinx.android.synthetic.main.activity_share_account_map.*
import javax.inject.Inject

class ShareAccountMapActivity : BaseAppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_QR_ACCOUNT_MAP = 0x01
    }

    @Inject
    internal lateinit var viewModel: ShareAccountMapViewModel

    @Inject
    internal lateinit var dialogController: DialogController

    override fun layoutRes() = R.layout.activity_share_account_map

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        title = "Account Map"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        viewModel.accountMapStatusLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    dialogController.alert(
                        R.string.valid_account_map,
                        R.string.you_can_tap_fill_now_to_fill_your_account_map
                    )
                }

                res.isError() -> {
                    Log.d("ShareAccountMapActivity", res.throwable()?.message ?: "")
                    editText.setText("")
                    dialogController.alert(R.string.error, R.string.invalid_account_map)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.common_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.action_scan -> {
                val intent = Intent(this, QRScannerActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_QR_ACCOUNT_MAP)
            }
            R.id.action_paste -> {
                this.pasteFromClipBoard()?.let {
                    checkAccountMap(it)
                } ?: dialogController.alert(R.string.error, R.string.clipboard_is_empty)
            }
            R.id.action_signer -> {
                val intent = Intent(this, AccountsActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_QR_ACCOUNT_MAP -> {
                    data?.let {
                        val accountMap = QRScannerActivity.extractResultData(it)
                        checkAccountMap(accountMap)
                    }
                }

                else -> {
                    error("unknown request code: $requestCode")
                }
            }
        }
    }

    private fun checkAccountMap(string: String) {
        editText.setText(string)
        viewModel.checkValidAccountMap(string)
    }
}