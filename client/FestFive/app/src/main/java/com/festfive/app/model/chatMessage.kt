package com.festfive.app.model

import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.extension.getDefault
import com.google.gson.JsonObject
import org.json.JSONObject
import org.webrtc.MediaStream

data class MessageSocket(
    val list : MutableList<ChatMessage>
) {
}

data class ChatMessage(
    val roomId : String,
    val userId : String,
    val userName : String,
    val message : String
) {
    fun  getMessages() : String = if(userId==MyApp.onlineUser.id) "Me: " + message else userName + ": " +message.getDefault()
    fun  getColor() : Int = if(userId==MyApp.onlineUser.id) R.color.color_red else R.color.black
}

data class UserSocket(
    val list : MutableList<OnlineUser>
) {
}

data class OnlineUser(
    var id: String? = null,
    val name: String? = null,
    var isMe: Boolean = false,
    var room : String? = null
){
    fun  getUserName() : String = if(isMe) "me: " + name else name.getDefault()
}

data class StreamSocket(
    val type : String,
    val from : String,
    val to : String,
    var payload : JSONObject
) {
}

data class OnClickUser(
    val user : OnlineUser,
    val isCall : Boolean
) {
}

data class VideoCall(
    val to : String?=null,
    val from : String?=null,
    val isReceive : Boolean?=false
) {
}

data class DataStream(
    var mediaStream: MediaStream? = null,
    val onlineUser: OnlineUser
) {
}

