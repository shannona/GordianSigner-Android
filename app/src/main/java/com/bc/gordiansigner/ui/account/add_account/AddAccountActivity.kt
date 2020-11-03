package com.bc.gordiansigner.ui.account.add_account

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Bip39
import com.bc.gordiansigner.helper.Error.FINGERPRINT_NOT_MATCH_ERROR
import com.bc.gordiansigner.helper.KeyStoreHelper
import com.bc.gordiansigner.helper.ext.enrollDeviceSecurity
import com.bc.gordiansigner.helper.ext.hideKeyBoard
import com.bc.gordiansigner.helper.ext.replaceSpaces
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import kotlinx.android.synthetic.main.activity_add_account.*
import javax.inject.Inject

class AddAccountActivity : BaseAppCompatActivity() {

    companion object {
        private const val TAG = "AddAccountActivity"
        private const val KEY_INFO = "key_info"
        private const val NEED_RESULT = "need_result"
        private const val KEY_XPRV = "key_xprv"

        fun getBundle(keyInfo: KeyInfo?, needResult: Boolean = true) = Bundle().apply {
            putParcelable(KEY_INFO, keyInfo)
            putBoolean(NEED_RESULT, needResult)
        }

        fun extractResultData(intent: Intent) = intent.getStringExtra(KEY_XPRV)
    }

    @Inject
    internal lateinit var viewModel: AddAccountViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private var shouldReturnResult = false
    private var keyInfo: KeyInfo? = null

    private val bip39Words = Bip39.words
    private var autoCompleteCharCount = -1
    private var isUserChanged = true

    private val addedWords = mutableListOf<String>()

    override fun layoutRes() = R.layout.activity_add_account

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        intent.getParcelableExtra<KeyInfo>(KEY_INFO)?.let {
            keyInfo = it
            aliasEditText.setText(it.alias)
            tvHeader.text = getString(R.string.signer_format, it.fingerprint, it.alias)
        }
        shouldReturnResult = intent.getBooleanExtra(NEED_RESULT, false)

        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        buttonAddSigner.setSafetyOnclickListener {
            if (addedWords.isEmpty()) {
                dialogController.alert(R.string.warning, R.string.please_input_your_recovery_words)
            } else {
                importWallet()
            }
        }

        buttonAdd.setSafetyOnclickListener {
            val wordsString = editText.text?.replaceSpaces() ?: return@setSafetyOnclickListener
            val words = wordsString.split(" ")
            this.hideKeyBoard()

            if (words.all { bip39Words.contains(it) }) {
                words.forEach { word ->
                    addedWords.add(word)
                    wordsEditText.append("${if (addedWords.size > 1) "\n" else ""}${addedWords.size}. $word")
                }

                autoCompleteCharCount = -1
                editText.setText("")
                editText.hint = getString(R.string.add_word_format, addedWords.size + 1)
            } else {
                dialogController.alert(R.string.warning, R.string.incorrect_words)
            }
        }

        buttonRemove.setSafetyOnclickListener {
            if (addedWords.isEmpty()) return@setSafetyOnclickListener

            addedWords.removeAt(addedWords.size - 1)
            if (addedWords.size > 0) {
                val result = wordsEditText.text.toString().substringBeforeLast("\n")
                wordsEditText.text = result
            } else {
                wordsEditText.text = ""
            }
        }

        editText.doAfterTextChanged { text ->
            val userText =
                if (text != null && isUserChanged) text.substring(0, editText.selectionEnd) else {
                    if (autoCompleteCharCount >= 0) {
                        text?.setSpan(
                            ForegroundColorSpan(Color.WHITE),
                            0,
                            text.length - autoCompleteCharCount,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        text?.setSpan(
                            ForegroundColorSpan(Color.GREEN),
                            text.length - autoCompleteCharCount,
                            text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        text?.setSpan(
                            ForegroundColorSpan(Color.RED),
                            0,
                            text.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    isUserChanged = true
                    return@doAfterTextChanged
                }

            if (userText.isEmpty()) {
                autoCompleteCharCount = -1
                setAutoComplete("")
                return@doAfterTextChanged
            }

            val firstMatch = bip39Words.firstOrNull { it.startsWith(userText) }
            firstMatch?.let {
                autoCompleteCharCount = it.length - userText.length
                setAutoComplete(it)
            } ?: run {
                autoCompleteCharCount = -1
                setAutoComplete(userText)
            }
            editText.setSelection(userText.length)
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                buttonAdd.callOnClick()
            }

            addedWords.size < 12
        }

        editText.hint = getString(R.string.add_word_format, 1)
    }

    override fun observe() {
        super.observe()

        viewModel.importAccountLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    dialogController.alert(
                        R.string.success,
                        if (scSavePrivate.isChecked) R.string.seed_words_encrypted_and_saved else R.string.your_account_has_been_saved_to_the_account_book
                    ) {
                        if (!shouldReturnResult) {
                            navigator.anim(RIGHT_LEFT).finishActivity()
                        } else {
                            val intent = Intent().apply { putExtra(KEY_XPRV, res.data()!!) }
                            navigator.anim(RIGHT_LEFT).finishActivityForResult(intent)
                        }
                    }
                }

                res.isError() -> {
                    if (!KeyStoreHelper.handleKeyStoreError(
                            applicationContext,
                            res.throwable()!!,
                            dialogController,
                            navigator,
                            authRequiredCallback = {
                                KeyStoreHelper.biometricAuth(
                                    this,
                                    R.string.auth_required,
                                    R.string.auth_for_storing_account,
                                    successCallback = {
                                        importWallet()
                                    },
                                    failedCallback = { code ->
                                        if (code == BIOMETRIC_ERROR_NONE_ENROLLED) {
                                            navigator.anim(RIGHT_LEFT).enrollDeviceSecurity()
                                        } else {
                                            Log.e(TAG, "Biometric auth failed with code: $code")
                                        }
                                    })
                            })
                    ) {
                        val message = when (res.throwable()!!) {
                            FINGERPRINT_NOT_MATCH_ERROR -> R.string.your_input_signer_is_not_matched
                            else -> R.string.some_thing_went_wrong_your_seed_words_were_not_saved
                        }
                        dialogController.alert(
                            R.string.error,
                            message
                        )
                    }
                }
            }
        })
    }

    private fun importWallet() {
        val phrase = addedWords.joinToString(separator = " ")
        val alias = aliasEditText.text.toString()
        val isSavePrivateKey = scSavePrivate.isChecked
        viewModel.importWallet(phrase, alias, isSavePrivateKey, keyInfo)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED && requestCode == KeyStoreHelper.ENROLLMENT_REQUEST_CODE) {
            // resultCode is 3 after biometric is enrolled
            importWallet()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setAutoComplete(value: String) {
        isUserChanged = false
        editText.setText(value)
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}