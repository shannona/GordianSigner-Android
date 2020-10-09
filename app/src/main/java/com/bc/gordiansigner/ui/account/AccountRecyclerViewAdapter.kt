package com.bc.gordiansigner.ui.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.gone
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.helper.ext.visible
import com.bc.gordiansigner.model.HDKey
import kotlinx.android.synthetic.main.item_signer.view.*

class AccountRecyclerViewAdapter(
    private var onDelete: ((String) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<HDKey>()

    var isEditing = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun set(items: List<HDKey>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun add(item: HDKey) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun remove(fingerprintHex: String) {
        val index = items.indexOfFirst { i -> i.fingerprintHex == fingerprintHex }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return KeyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_signer,
                parent,
                false
            ),
            onDelete
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? KeyViewHolder)?.bind(items[position], isEditing)
    }

    class KeyViewHolder(
        view: View,
        onDelete: ((String) -> Unit)?
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var key: HDKey

        init {
            itemView.buttonDelete.setSafetyOnclickListener {
                onDelete?.invoke(key.fingerprintHex)
            }
        }

        fun bind(key: HDKey, isEditing: Boolean) {
            this.key = key
            with(itemView) {
                tvFingerprint.text = key.fingerprintHex
                if (isEditing) {
                    buttonDelete.visible(true)
                } else {
                    buttonDelete.gone()
                }
            }
        }
    }
}