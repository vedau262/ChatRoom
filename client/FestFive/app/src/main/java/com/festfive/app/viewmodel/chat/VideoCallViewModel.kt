package com.festfive.app.viewmodel.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.festfive.app.application.MyApp
import com.festfive.app.base.viewmodel.BaseViewModel
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

    private val _onAnswerAccept = MutableLiveData<Boolean>(false)
    val onAnswerAccept : LiveData<Boolean>
        get() {
            return _onAnswerAccept
        }

    private val _onEndCall = MutableLiveData<Boolean>(false)
    val onEndCall : LiveData<Boolean>
        get() {
            return _onEndCall
        }

    private val _streamSocket = MutableLiveData<StreamSocket>()
    val streamSocket : LiveData<StreamSocket>
        get() {
            return _streamSocket
        }

    init {
        this.onBindSocketReceivedListener()
        socketManager.onChannel(Constants.KEY_ACCEPT_VIDEO_CALL, Emitter.Listener {
            Timber.e(TAG + Constants.KEY_ACCEPT_VIDEO_CALL + it)
            _onAnswerAccept.postValue(true)
        })

        socketManager.onChannel(Constants.KEY_END_CALL, Emitter.Listener {
            Timber.e(TAG +  Constants.KEY_END_CALL + _onEndCall.value.getDefault())
            _onEndCall.postValue(true)
        })
    }

    fun endCall(friendId: String) {
        MyApp.mSocket.emitData(Constants.KEY_END_CALL, friendId)
    }

    fun resetValue() {
        _onAnswerAccept.value = false
        _onEndCall.value = false
        Timber.e(TAG + "resetValue ")
    }

    override fun onStreamChanged(data: StreamSocket) {
        super.onStreamChanged(data)
        Timber.e(TAG + "onStreamChanged "+data)
        if(data!=null){
            _streamSocket.postValue(data)
        }

    }
}