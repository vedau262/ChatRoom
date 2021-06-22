package com.festfive.app.viewmodel.call

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.festfive.app.application.MyApp
import com.festfive.app.base.viewmodel.BaseViewModel
import com.festfive.app.customize.listener.SignallingClientListener
import com.festfive.app.model.OnlineUser
import com.festfive.app.model.StreamSocket
import com.festfive.app.push.SocketManager
import com.festfive.app.utils.Constants
import timber.log.Timber
import javax.inject.Inject

class GroupCallViewModel @Inject constructor (
    private val socketManager: SocketManager
): BaseViewModel() {
    private val TAG = "GroupCallViewModel: "

    var callbacks: SignallingClientListener? = null
    private val _mUserList: MutableLiveData<MutableList<OnlineUser>> = MutableLiveData()
    val userList: LiveData<MutableList<OnlineUser>> = _mUserList

    init {
        this.onBindSocketReceivedListener()
    }

    fun endCall(roomId: String) {
        socketManager.emitData(Constants.KEY_END_CALL, roomId)
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

    override fun onUserJoinChanged(data: MutableList<OnlineUser>) {
        super.onUserJoinChanged(data)
        Timber.e("onUserJoinChanged ------> $data")
        _mUserList.value = (data)
    }
}