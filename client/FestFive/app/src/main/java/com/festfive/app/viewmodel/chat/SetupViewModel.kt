package com.festfive.app.viewmodel.chat

import androidx.lifecycle.MutableLiveData
import com.festfive.app.application.MyApp
import com.festfive.app.base.viewmodel.BaseViewModel
import com.festfive.app.model.OnlineUser
import com.festfive.app.push.SocketManager
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SetupViewModel @Inject constructor (
    private val socketManager: SocketManager
): BaseViewModel() {

    private var roomId: String = ""
    private var name: String =  ""
    private var mUserList: MutableLiveData<MutableList<OnlineUser>> = MutableLiveData()

    init {
        this.onBindSocketReceivedListener()
    }

    fun setupChat(roomId: String, name: String) {
        if(this.roomId.isNullOrEmpty() || this.name.isNullOrEmpty()){
            try {
                val message = JSONObject()
                message.put("room", roomId)
                message.put("name", name)
                socketManager?.emitData("readyToStream", message)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            try {
                val message = JSONObject()
                message.put("room", roomId)
                message.put("name", name)
                socketManager?.emitData("update", message)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        this.roomId = roomId
        this.name = name
        MyApp.updateUser(OnlineUser(room = roomId, name = name, id = MyApp.onlineUser.id, isMe =  MyApp.onlineUser.isMe))

    }

    fun getUsers(): MutableLiveData<MutableList<OnlineUser>> = mUserList

    override fun onUserJoinChanged(data: MutableList<OnlineUser>) {
        super.onUserJoinChanged(data)
        Timber.e("onUserJoinChanged ------> $data")
        mUserList.value = (data)
    }
}