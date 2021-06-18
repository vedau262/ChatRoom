package com.festfive.app.viewmodel.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.festfive.app.base.viewmodel.BaseViewModel
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
    private var mUserList: MutableLiveData<MutableList<OnlineUser>> = MutableLiveData()
    fun getUsers(): MutableLiveData<MutableList<OnlineUser>> = mUserList

    private val _myId = MutableLiveData<String>()
    val myId : LiveData<String>
        get() {
            return _myId
        }

    private val _onAnswerAccept = MutableLiveData<Boolean>(false)
    val onAnswerAccept : LiveData<Boolean>
        get() {
            return _onAnswerAccept
        }

    private val _streamSocket = MutableLiveData<StreamSocket>()
    val streamSocket : LiveData<StreamSocket>
        get() {
            return _streamSocket
        }

    init {
        this.onBindSocketReceivedListener()
        socketManager.onChannel(Constants.KEY_ACCEPT_VIDEO_CALL, Emitter.Listener {
            Timber.e(TAG + "KEY_ACCEPT_VIDEO_CALL "+ it)
            _onAnswerAccept.postValue(true)
        })
    }

    override fun onStreamChanged(data: StreamSocket) {
        super.onStreamChanged(data)
        Timber.e(TAG + "onStreamChanged "+data)
        if(data!=null){
            _streamSocket.postValue(data)
        }

    }

    override fun onMyIdChanged(data: OnlineUser) {
        super.onMyIdChanged(data)
        Timber.e(TAG + "onMyIdChanged "+data)
        if(data!=null){
            _myId.postValue(data.id)
        }
    }

    override fun onUserJoinChanged(data: MutableList<OnlineUser>) {
        super.onUserJoinChanged(data)
        Timber.e(TAG + "onUserJoinChanged ------> $data")
        mUserList.value = (data)
    }
}