package com.bc.gordiansigner.ui.account.add_account

import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Bip39Words
import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import kotlinx.android.synthetic.main.activity_add_account.*
import javax.inject.Inject

class AddAccountActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: AddAccountViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private val bip39Words = Bip39Words.valid
    private var autoCompleteCharCount = -1
    private var isUserChanged = true

    private val addedWords = mutableListOf<String>()

    override fun layoutRes() = R.layout.activity_add_account

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        title = getString(R.string.import_account)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        buttonAddSigner.setSafetyOnclickListener {
            val phrase = addedWords.joinToString(separator = " ")
            viewModel.importWallet(phrase, Network.TEST)
        }

        buttonAdd.setSafetyOnclickListener {
            if (autoCompleteCharCount >= 0) {
                val word = editText.text.toString()
                addedWords.add(word)

                wordsEditText.append("${if (addedWords.size > 1) "\n" else ""}${addedWords.size}. $word")

                autoCompleteCharCount = -1
                editText.setText("")
                editText.hint = getString(R.string.add_word_format, addedWords.size + 1)
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
                        R.string.seed_words_encrypted_and_saved
                    ) {
                        finish()
                    }
                }

                res.isError() -> {
                    dialogController.alert(
                        R.string.error,
                        R.string.some_thing_went_wrong_your_seed_words_were_not_saved
                    )
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

    private fun setAutoComplete(value: String) {
        isUserChanged = false
        editText.setText(value)
    }
}