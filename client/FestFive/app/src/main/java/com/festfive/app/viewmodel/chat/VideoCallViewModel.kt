package com.festfive.app.viewmodel.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.festfive.app.base.viewmodel.BaseViewModel
import com.festfive.app.model.OnlineUser
import com.festfive.app.model.StreamSocket
import com.festfive.app.push.SocketManager
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class VideoCallViewModel @Inject constructor (
): BaseViewModel() {
    private var mUserList: MutableLiveData<MutableList<OnlineUser>> = MutableLiveData()
    fun getUsers(): MutableLiveData<MutableList<OnlineUser>> = mUserList

    private val _myId = MutableLiveData<String>()
    val myId : LiveData<String>
        get() {
            return _myId
        }

    private val _streamSocket = MutableLiveData<StreamSocket>()
    val streamSocket : LiveData<StreamSocket>
        get() {
            return _streamSocket
        }

    init {
        this.onBindSocketReceivedListener()
    }

    override fun onStreamChanged(data: StreamSocket) {
        super.onStreamChanged(data)
        Timber.e("onStreamChanged "+data)
        if(data!=null){
            _streamSocket.postValue(data)
        }

    }

    override fun onMyIdChanged(data: OnlineUser) {
        super.onMyIdChanged(data)
        Timber.e("onMyIdChanged "+data)
        if(data!=null){
            _myId.postValue(data.id)
        }
    }

    override fun onUserJoinChanged(data: MutableList<OnlineUser>) {
        super.onUserJoinChanged(data)
        Timber.e("onUserJoinChanged ------> $data")
        mUserList.value = (data)
    }
}