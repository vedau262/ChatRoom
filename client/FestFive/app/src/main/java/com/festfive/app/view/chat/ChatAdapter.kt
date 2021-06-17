package com.festfive.app.view.chat

import com.festfive.app.R
import com.festfive.app.base.adapter.BaseBindingAdapter
import com.festfive.app.databinding.ItemChatBinding
import com.festfive.app.model.ChatMessage

class ChatAdapter: BaseBindingAdapter<ItemChatBinding, ChatMessage>(){
    override fun getLayoutId(viewType: Int) = R.layout.item_chat

    override fun bindViewHolder(binding: ItemChatBinding, position: Int) {
        val message = list[position]
//        binding.txtMes.text = message.userName + ": " + message.message
//        Log.d("BaseBindingAdapter" , "" + list[position])

        binding.apply {
            data = message
        }
    }
}