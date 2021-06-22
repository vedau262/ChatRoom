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
import com.festfive.app.utils.event.RxEvent
import com.festfive.app.utils.event.SystemEvent
import com.github.nkzawa.emitter.Emitter
import com.google.gson.Gson
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class GroupCallViewModel @Inject constructor (
    private val socketManager: SocketManager
): BaseViewModel() {
    private val TAG = "GroupCallViewModel: "
    var myRoom  = ""

    var callbacks: SignallingClientListener? = null

    private val _mUserList: MutableLiveData<MutableList<OnlineUser>> = MutableLiveData()
    val userList: LiveData<MutableList<OnlineUser>> = _mUserList

    init {
        this.onBindSocketReceivedListener()
        socketManager.onChannel(Constants.KEY_STREAM_GROUP_CALL, Emitter.Listener {
            Timber.e(TAG +  Constants.KEY_STREAM_GROUP_CALL)
            val data = it[0] as JSONObject
            val mes = Gson().fromJson<StreamSocket>(data.toString(), StreamSocket::class.java).apply {
                payload = data.getJSONObject("payload")
            }

            Timber.e(TAG + "onStreamChanged "+data)
            if(mes!=null){
                callbacks?.onProcessStreamSocket(mes)
            }
        })
    }

    fun endCall(roomId: String) {
        socketManager.emitData(Constants.KEY_END_GROUP_CAL, roomId)
    }

    fun onStartCall(myRoomId : String) {
        socketManager.emitData(Constants.KEY_START_GROUP_CALL, myRoomId)
    }

    fun onStartAnswer(friendId : String) {
        socketManager.emitData(Constants.KEY_START_ANSWER, friendId)
    }

    fun setCallback(callback: SignallingClientListener) {
        this.callbacks = callback
    }

    /*override fun onStreamChanged(data: StreamSocket) {
        super.onStreamChanged(data)
        Timber.e(TAG + "onStreamChanged "+data)
        if(data!=null){
            callbacks?.onProcessStreamSocket(data)
        }
    }*/

    override fun onUserJoinChanged(data: MutableList<OnlineUser>) {
        super.onUserJoinChanged(data)
        Timber.e("onUserJoinChanged ------> $data")
        val list = data
        for(i in list.size-1 downTo 0){
            if(list[i].room!=myRoom){
                list.removeAt(i)
            }
        }
        _mUserList.postValue(list)
    }
}