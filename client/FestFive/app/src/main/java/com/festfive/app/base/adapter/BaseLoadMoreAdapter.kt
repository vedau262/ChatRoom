package com.festfive.app.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.festfive.app.R
import com.festfive.app.data.network.NetworkState
import com.festfive.app.data.preference.ConfigurationPrefs
import com.festfive.app.data.preference.IConfigurationPrefs
import com.festfive.app.databinding.ViewLoadMoreBinding

/**
 * Created by Nhat Vo on 24/08/2020.
 */
abstract class BaseLoadMoreAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var prefs: IConfigurationPrefs

    val list: MutableList<T> = mutableListOf()

    private var networkState: NetworkState? = null

    override fun getItemViewType(position: Int) =
        if (hasExtraRow() && position == itemCount - 1) {
            ViewType.ITEM_LOADING.type
        } else {
            ViewType.ITEM_NORMAL_LIST.type
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        prefs = ConfigurationPrefs(parent.context)
        return when (viewType) {
            ViewType.ITEM_LOADING.type -> BaseViewHolder(
                DataBindingUtil.inflate<ViewLoadMoreBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.view_load_more,
                    parent,
                    false
                )
            )
            else -> getViewHolder(parent, viewType)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LoadingViewHolder -> holder.bindData()
            else -> onBindHolder(holder, position)
        }
    }

    abstract fun getViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    abstract fun onBindHolder(holder: RecyclerView.ViewHolder, position: Int)

    override fun getItemCount(): Int = list.size + if (hasExtraRow()) 1 else 0

    protected fun hasExtraRow() = list.size > 5 && networkState != null && networkState == NetworkState.LOADING

    fun updateData(list: MutableList<T>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(itemCount)
            } else {
                notifyItemInserted(itemCount)
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    enum class ViewType(val type: Int) {
        ITEM_NORMAL_LIST(0), ITEM_LOADING(-1), ITEM_SPECIAL_LIST(-2)
    }
}

