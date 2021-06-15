package com.festfive.app.view.chat

import com.festfive.app.R
import com.festfive.app.base.view.BaseActivity
import com.festfive.app.base.viewmodel.EmptyViewModel
import com.festfive.app.databinding.ActivityChatBinding
import com.festfive.app.view.ChatAdapter

class SocketTestActivity : BaseActivity<ActivityChatBinding, EmptyViewModel>(){
    override fun getLayoutRes() : Int = R.layout.activity_chat
}