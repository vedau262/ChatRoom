package com.festfive.app.view.chat

import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.festfive.app.R
import com.festfive.app.base.view.BaseFragment
import com.festfive.app.databinding.FragmentChatBinding
import com.festfive.app.extension.initLinear
import com.festfive.app.utils.Constants
import com.festfive.app.viewmodel.chat.ChatViewmodel
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : BaseFragment<FragmentChatBinding, ChatViewmodel>(){
    val chatAdapter = ChatAdapter()

    override fun getLayoutRes(): Int =  R.layout.fragment_chat

    override fun initViewModel() {
        super.initViewModel()

        mViewModel.apply {
            getMessage().observe(viewLifecycleOwner, Observer {
                if(!it.isNullOrEmpty()){
                    chatAdapter.addListData(it)
                    rc_chat.scrollToPosition(chatAdapter.itemCount-1)
                }
            })

            val roomId = arguments?.getString(Constants.KEY_PUT_OBJECT).toString()
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

            input.addTextChangedListener {text ->
                dataBinding.enableSend = !text.toString().trim().isNullOrEmpty()
            }
        }
    }

    fun onSendMessage() {
        val mes = dataBinding.input.text.toString().trim()
        mViewModel.sendMessage(mes)
        dataBinding.input.text.clear()
    }
}