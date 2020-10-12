package com.bc.gordiansigner.ui.sign

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.ext.animateWithString
import com.bc.gordiansigner.helper.ext.copyToClipboard
import com.bc.gordiansigner.helper.ext.pasteFromClipBoard
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import com.bc.gordiansigner.ui.account.AccountsActivity
import com.bc.gordiansigner.ui.scan.QRScannerActivity
import kotlinx.android.synthetic.main.activity_psbt_sign.*
import javax.inject.Inject

class PsbtSignActivity : BaseAppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_QR_PSBT = 0x02
    }

    @Inject
    internal lateinit var viewModel: PsbtSignViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private var export = false

    override fun layoutRes() = R.layout.activity_psbt_sign

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        title = "PSBT Signer"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        buttonNext.setSafetyOnclickListener {
            if (export) {
                this.copyToClipboard(editText.text.toString())
                buttonNext.animateWithString("Copied")
            } else {
                val accountMapJson = editText.text.toString()
                viewModel.signPsbt(accountMapJson, Network.TEST)
            }
        }
    }

    override fun observe() {
        super.observe()

        viewModel.psbtLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    editText.setText(res.data())
                    buttonNext.setText(R.string.export)
                    export = true
                    dialogController.alert(
                        R.string.psbt_signed,
                        R.string.you_may_now_export_it_by_tapping_the_export_button
                    )
                }

                res.isError() -> {
                    dialogController.alert(
                        getString(R.string.error),
                        res.throwable()?.message
                            ?: getString(R.string.unable_to_sign_psbt_unknown_error)
                    )
                }
            }
        })

        viewModel.psbtCheckingLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    dialogController.alert(
                        R.string.valid_psbt,
                        R.string.you_can_tap_sign_button_to_sign_your_psbt
                    )
                }

                res.isError() -> {
                    editText.setText("")
                    dialogController.alert(R.string.error, R.string.invalid_psbt)
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
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
            R.id.action_scan -> {
                navigator.anim(RIGHT_LEFT)
                    .startActivityForResult(QRScannerActivity::class.java, REQUEST_CODE_QR_PSBT)
            }
            R.id.action_paste -> {
                this.pasteFromClipBoard()?.let {
                    checkPsbt(it)
                } ?: dialogController.alert(R.string.error, R.string.clipboard_is_empty)
            }
            R.id.action_signer -> {
                navigator.anim(RIGHT_LEFT).startActivity(AccountsActivity::class.java)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_QR_PSBT -> {
                    data?.let {
                        val base64 = QRScannerActivity.extractResultData(it)
                        checkPsbt(base64)
                    }
                }

                else -> {
                    error("unknown request code: $requestCode")
                }
            }
        }
    }

    private fun checkPsbt(base64: String) {
        editText.setText(base64)
        export = false
        buttonNext.setText(R.string.sign_psbt)
        viewModel.checkPsbt(base64)
    }
}