package com.festfive.app.viewmodel.chat

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.festfive.app.application.MyApp
import com.festfive.app.base.viewmodel.BaseViewModel
import com.festfive.app.extension.getDefault
import com.festfive.app.model.OnlineUser
import com.festfive.app.model.VideoCall
import com.festfive.app.push.SocketManager
import com.festfive.app.utils.Constants
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SetupViewModel @Inject constructor (
    private val socketManager: SocketManager
): BaseViewModel() {
    private var mUserList: MutableLiveData<MutableList<OnlineUser>> = MutableLiveData()
    private var _videoCall: MutableLiveData<VideoCall> = MutableLiveData()
    val videoCall: MutableLiveData<VideoCall>
        get() {
            return _videoCall
        }

    init {
        this.onBindSocketReceivedListener()
    }

    fun setupChat(roomId: String, name: String) {
        if(MyApp.onlineUser.id.getDefault().isNullOrEmpty()){
            try {
                val message = JSONObject()
                message.put("room", roomId)
                message.put("name", name)
                socketManager?.emitData(Constants.KEY_READY_TO_STREAM, message)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            try {
                val message = JSONObject()
                message.put("room", roomId)
                message.put("name", name)
                socketManager?.emitData(Constants.KEY_UPDATE_USER_INFO, message)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        MyApp.updateUser(OnlineUser(room = roomId, name = name, id = MyApp.onlineUser.id, isMe =  MyApp.onlineUser.isMe))

    }

    fun getUsers(): MutableLiveData<MutableList<OnlineUser>> = mUserList

    override fun onUserJoinChanged(data: MutableList<OnlineUser>) {
        super.onUserJoinChanged(data)
        Timber.e("onUserJoinChanged ------> $data")
        mUserList.value = (data)
    }

    override fun onVideoCall(data: VideoCall) {
        super.onVideoCall(data)
        if(MyApp.onlineUser.id.getDefault() == data.to){
            _videoCall.postValue(data)
        }
    }
}