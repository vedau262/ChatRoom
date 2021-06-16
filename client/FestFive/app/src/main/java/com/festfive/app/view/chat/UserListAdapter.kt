package com.festfive.app.view.chat

import android.util.Log
import com.festfive.app.R
import com.festfive.app.base.adapter.BaseBindingAdapter
import com.festfive.app.databinding.ItemChatBinding
import com.festfive.app.databinding.ItemUserBinding
import com.festfive.app.model.OnlineUser

class UserListAdapter(private val onlineUser :(OnlineUser) -> Unit) : BaseBindingAdapter<ItemUserBinding, OnlineUser>(){
    override fun getLayoutId(viewType: Int) = R.layout.item_user

    override fun bindViewHolder(binding: ItemUserBinding, position: Int) {
        val data = list[position]
        binding.apply {
            user = data
            btnCall.setOnClickListener {
                onlineUser.invoke(data)
            }
        }
    }
}