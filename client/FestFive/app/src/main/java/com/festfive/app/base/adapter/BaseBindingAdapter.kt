package com.festfive.app.base.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.festfive.app.customize.listener.ISingleClickListener
import com.festfive.app.data.preference.ConfigurationPrefs
import com.festfive.app.data.preference.IConfigurationPrefs

/**
 * Created by Nhat.vo on 17/11/2020.
 */
abstract class BaseBindingAdapter<V : ViewDataBinding, T>(val onClicked: ((T?) -> Unit)? = null) : RecyclerView.Adapter<BaseViewHolder<V>>(), ISingleClickListener<T> {

    lateinit var prefs: IConfigurationPrefs
    val list: MutableList<T> = mutableListOf()
    var parentWidth = 0
    lateinit var context: Context

    protected abstract fun getLayoutId(viewType: Int): Int
    abstract fun bindViewHolder(binding: V, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<V> {
        parentWidth = parent.measuredWidth
        context = parent.context
        prefs = ConfigurationPrefs(parent.context)
        val binding = DataBindingUtil.inflate<V>(
            LayoutInflater.from(parent.context),
            getLayoutId(viewType),
            parent,
            false
        )

        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<V>, position: Int) {
        bindViewHolder(holder.binding, position)
    }

    open fun updateData(list: MutableList<T>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    open fun addData(data: T) {
        this.list.add(data)
        notifyItemChanged(list.size-1)
    }

    open fun removeData(data: T) {
        val index = list.indexOf(data)
        if(index!=1){
            this.list.remove(data)
            notifyItemRemoved(index)
        }
    }

    open fun removeData(pos: Int) {
        if(pos>= 0 && pos < list.size){
            this.list.removeAt(pos)
            notifyDataSetChanged()
        }
    }

    open fun addListData(list: MutableList<T>) {
        this.list.addAll(list)
        notifyItemRangeChanged(list.indexOf(list[0]), this.list.size)
    }


    override fun getItemCount(): Int = list.size

    override fun onSingleClicked(data: T) {}
}

open class BaseViewHolder<V : ViewDataBinding>(val binding: V) : RecyclerView.ViewHolder(binding.root) {
    open fun bind(position: Int) {}
}