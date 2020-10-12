package com.bc.gordiansigner.ui.account

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import com.bc.gordiansigner.ui.account.add_account.AddAccountActivity
import kotlinx.android.synthetic.main.activity_accounts.*
import javax.inject.Inject

class AccountsActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: AccountsViewModel

    @Inject
    internal lateinit var navigator: Navigator

    private lateinit var adapter: AccountRecyclerViewAdapter

    override fun layoutRes() = R.layout.activity_accounts

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        title = "Accounts"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = AccountRecyclerViewAdapter { fingerprint ->
            viewModel.deleteAccount(fingerprint)
        }

        with(recyclerView) {
            this.adapter = this@AccountsActivity.adapter
            this.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchWallets()
    }

    override fun observe() {
        super.observe()

        viewModel.hdKeysLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { keys ->
                        adapter.set(keys)
                    }
                }

                res.isError() -> {
                    Log.e("AccountsActivity", res.throwable()?.message ?: "")
                }
            }
        })

        viewModel.deleteKeysLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { fingerprint ->
                        adapter.remove(fingerprint)
                    }
                }

                res.isError() -> {
                    Log.e("AccountsActivity", res.throwable()?.message ?: "")
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.accounts_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_edit)?.let { item ->
            item.setTitle(if (adapter.isEditing) R.string.done else R.string.edit)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
            R.id.action_add -> {
                navigator.anim(RIGHT_LEFT).startActivity(AddAccountActivity::class.java)
            }
            R.id.action_edit -> {
                adapter.isEditing = !adapter.isEditing
                invalidateOptionsMenu()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}