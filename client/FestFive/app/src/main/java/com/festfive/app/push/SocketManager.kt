package com.festfive.app.push

import android.os.Handler
import android.os.Looper
import com.festfive.app.application.MyApp
import com.festfive.app.data.preference.IConfigurationPrefs
import com.festfive.app.extension.copyObject
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val prefs: IConfigurationPrefs
) {

    private val URL_SOCKET = "http://192.168.0.123:5000/"
    private var mSocket: Socket? = null
    private var mChannels = arrayListOf<String>()

    fun init() {
        prefs.token?.let {
            val opts = IO.Options()
            opts.forceNew = true
            mSocket = IO.socket(URL_SOCKET, opts)
            setupConnectStatus()
        }
    }

    private fun setupConnectStatus() {
        mSocket?.let {
            it.on("connect") {
                emitData("test", "dung test chat")
                Timber.e("On Socket Connected")
            }


            it.on("test") {
                Timber.e("socket test: ok")
            }

            it.on("disconnect") {
                Timber.e("On Socket Disconnected")
            }
        }
    }

    fun onChannel(channel: String, listener: Emitter.Listener) {
        mSocket?.let { socket ->
            if (!socket.hasListeners(channel)) {
                socket.on(channel, listener)
                Timber.e("On channel: $channel")
            }
            if (!mChannels.contains(channel)) {
                mChannels.add(channel)
            }
            mChannels.distinctBy { it }
        }
    }

    fun setConnectedCallback(onConnected: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isRegisterSocketListener()) {
                onConnected.invoke()
            } else {
                setConnectedCallback(onConnected)
            }
        }, 100)
    }

    fun emitData(key: String, data: Any) {
        Timber.e("Emit Data with event: $key ,data: ${Gson().toJson(data)}")
        val mes = Gson().toJson(data)
        mSocket?.emit(key, data)
    }

    private fun isRegisterSocketListener(): Boolean {
        return when {
            mSocket == null -> {
                init()
                false
            }
            mSocket?.connected() != true -> {
                connect()
                false
            }
            else -> {
                true
            }
        }
    }

    private fun removeChannels() {
        mSocket?.apply {
            val temps = mChannels.copyObject(Array<String>::class.java)
            temps.forEach {
                off(it)
                mChannels.remove(it)
            }
        }
    }

    fun removeChannel(chanelName: String) {
        mSocket?.apply {
            val temps = mChannels.copyObject(Array<String>::class.java)
            if(temps.contains(chanelName)){
                off(chanelName)
                mChannels.remove(chanelName)
            }
        }
    }

    private fun connect() {
        mSocket?.connect()
    }

    fun disconnect() {
        mSocket?.apply {
            Timber.e("disconnect")
            removeChannels()
            disconnect()
            mSocket = null
        }
    }

    companion object {
        fun parseSocketData(vararg args: Any?, callback: (String) -> Unit) {
            if (args.isNotEmpty()) {
                args[0]?.let { message ->
                    kotlin.runCatching {
                        val data = JSONArray(message)[0].toString()
                        Timber.e("SocketMessage ------> $data")
                        callback.invoke(data)
                    }
                }
            }
        }
    }
}