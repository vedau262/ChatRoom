package com.festfive.app.view

import com.festfive.app.base.view.IBaseActivity
import com.festfive.app.viewmodel.chat.ChatViewmodel

interface ISocketTestActivity : IBaseActivity<ChatViewmodel>{
    fun setupChat()
}