package com.festfive.app.view

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.base.view.BaseFragment
import com.festfive.app.databinding.ChatFragmentBinding
import com.festfive.app.extension.getDefault
import com.festfive.app.extension.initLinear
import com.festfive.app.model.ChatMessage
import com.festfive.app.utils.Constants
import com.festfive.app.viewmodel.chat.ChatViewmodel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.chat_fragment.*
import org.json.JSONException
import org.json.JSONObject

class ChatFragment : BaseFragment<ChatFragmentBinding, ChatViewmodel>(){
    val chatAdapter = ChatAdapter()

    override fun getLayoutRes(): Int =  R.layout.chat_fragment

    override fun initViewModel() {
        super.initViewModel()

        mViewModel.apply {
            getMessage().observe(viewLifecycleOwner, Observer {
                chatAdapter.addListData(it)
                rc_chat.scrollToPosition(chatAdapter.itemCount-1)
//                chatAdapter.updateData(it)
            })

            getAllMessage()
        }

    }

    override fun initView() {
        super.initView()
        dataBinding.apply {
            iView = this@ChatFragment
            rcChat.apply {
                adapter = chatAdapter
                initLinear(RecyclerView.VERTICAL)

            }
        }
    }

    fun onSendMessage() {
        val mes = dataBinding.input.text.toString()
        mViewModel.sendMessage(mes)
        dataBinding.input.text.clear()
    }
}