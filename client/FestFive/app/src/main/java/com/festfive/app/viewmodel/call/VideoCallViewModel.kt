package com.festfive.app.viewmodel.call

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.festfive.app.application.MyApp
import com.festfive.app.base.viewmodel.BaseViewModel
import com.festfive.app.customize.listener.SignallingClientListener
import com.festfive.app.extension.getDefault
import com.festfive.app.model.OnlineUser
import com.festfive.app.model.StreamSocket
import com.festfive.app.push.SocketManager
import com.festfive.app.utils.Constants
import com.github.nkzawa.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class VideoCallViewModel @Inject constructor (
    private val socketManager: SocketManager
): BaseViewModel() {
    private val TAG = "VideoCallViewModel: "

    var callbacks: SignallingClientListener? = null

    init {
        this.onBindSocketReceivedListener()
        socketManager.onChannel(Constants.KEY_ACCEPT_VIDEO_CALL, Emitter.Listener {
            Timber.e(TAG + Constants.KEY_ACCEPT_VIDEO_CALL)
            callbacks?.onAnswerAccept()
        })

        socketManager.onChannel(Constants.KEY_END_CALL, Emitter.Listener {
            Timber.e(TAG +  Constants.KEY_END_CALL)
            callbacks?.onEndCall()
        })
    }

    fun endCall(friendId: String) {
        MyApp.mSocket.emitData(Constants.KEY_END_CALL, friendId)
    }

    fun setCallback(callback: SignallingClientListener) {
        this.callbacks = callback
    }

    override fun onStreamChanged(data: StreamSocket) {
        super.onStreamChanged(data)
        Timber.e(TAG + "onStreamChanged "+data)
        if(data!=null){
            callbacks?.onProcessStreamSocket(data)
        }
    }
}