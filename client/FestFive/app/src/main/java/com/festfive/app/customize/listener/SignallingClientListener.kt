package com.festfive.app.customize.listener

import com.festfive.app.model.StreamSocket

interface SignallingClientListener {
    fun onAnswerAccept()
    fun onEndCall()
    fun onProcessStreamSocket(data: StreamSocket)
}