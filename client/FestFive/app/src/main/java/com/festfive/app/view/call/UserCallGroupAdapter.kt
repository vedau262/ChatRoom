package com.festfive.app.view.call

import com.festfive.app.R
import com.festfive.app.base.adapter.BaseBindingAdapter
import com.festfive.app.databinding.ItemCallBinding
import com.festfive.app.databinding.ItemUserBinding
import com.festfive.app.model.OnClickUser
import com.festfive.app.model.OnlineUser

class UserCallGroupAdapter (private val onlineUser : (OnlineUser) -> Unit) :
    BaseBindingAdapter<ItemCallBinding, OnlineUser>(){

    override fun getLayoutId(viewType: Int) = R.layout.item_call

    override fun bindViewHolder(binding: ItemCallBinding, position: Int) {
        val data = list[position]
        binding.apply {
            user = data
            onlineUser.invoke(data)
        }
    }
}