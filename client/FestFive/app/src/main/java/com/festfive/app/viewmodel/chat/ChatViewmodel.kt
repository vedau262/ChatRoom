package com.festfive.app.viewmodel.chat

import androidx.lifecycle.MutableLiveData
import com.festfive.app.application.MyApp
import com.festfive.app.base.viewmodel.BaseViewModel
import com.festfive.app.extension.copyObject
import com.festfive.app.extension.getDefault
import com.festfive.app.model.ChatMessage
import com.festfive.app.push.SocketManager
import com.festfive.app.utils.Constants
import com.festfive.app.utils.event.RxEvent
import com.festfive.app.utils.event.SystemEvent
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.net.URISyntaxException
import javax.inject.Inject


class ChatViewmodel @Inject constructor (): BaseViewModel() {

    private var mMessage: MutableLiveData<MutableList<ChatMessage>> = MutableLiveData()

    init {
        this.onBindSocketReceivedListener()
    }



    fun getMessage() : MutableLiveData<MutableList<ChatMessage>> = mMessage


    override fun onChatMessageChanged(data: MutableList<ChatMessage>) {
        super.onChatMessageChanged(data)
        Timber.e("onChatMessageChanged ------> $data")

        val listMessage = mutableListOf<ChatMessage>()
        data.forEach { mes ->
            if(mes.roomId == MyApp.onlineUser.room){
                listMessage.add(mes)
             }
        }
        mMessage.postValue(listMessage)

    }

    fun sendMessage(mes: String){
        if(mes.isNotEmpty()){
            val chatMessage =  ChatMessage(
                roomId = MyApp.onlineUser.room.getDefault(),
                userId = MyApp.onlineUser.id.getDefault(),
                userName = MyApp.onlineUser.name.getDefault(),
                message = mes
            )

            try {
                val message = JSONObject()
                message.put("roomId", chatMessage.roomId)
                message.put("userId", chatMessage.userId)
                message.put("userName", chatMessage.userName)
                message.put("message", chatMessage.message)
                MyApp.mSocket.emitData(Constants.KEY_SEND_MESSAGE, message)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    }

    fun getAllMessage(){
        try {
            MyApp.mSocket.emitData(Constants.KEY_GET_ALL_MESSAGES, MyApp.onlineUser.room.getDefault())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

}


