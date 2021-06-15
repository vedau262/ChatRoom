package com.festfive.app.model

import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.extension.getDefault

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
    val room : String? = null
){
    fun  getUserName() : String = if(isMe) "me: " + name else name.getDefault()
}

