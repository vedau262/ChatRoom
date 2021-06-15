package com.festfive.app.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.festfive.app.BuildConfig
import com.festfive.app.model.ChatMessage
import com.festfive.app.model.MessageSocket
import com.festfive.app.model.OnlineUser
import com.festfive.app.model.UserSocket
import com.festfive.app.push.SocketManager
import com.festfive.app.utils.Constants
import com.festfive.app.utils.SharePreferencesUtils
import com.festfive.app.utils.event.RxEvent
import com.festfive.app.utils.event.SystemEvent
import com.github.nkzawa.emitter.Emitter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.realm.Realm
import io.realm.RealmConfiguration
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.lang.reflect.Type
import java.net.URISyntaxException
import javax.inject.Inject


/**
 * Created by Nhat.vo on 16/11/2020.
 */

class MyApp : DaggerApplication(), Application.ActivityLifecycleCallbacks {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false


    @Inject
    lateinit var mSocketManager: SocketManager

    init {
        instance = this
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val appComponent = DaggerAppComponent.builder().application(this).build()
        appComponent.inject(this)
        return appComponent
    }

    override fun onCreate() {
        super.onCreate()
        // Set context for SharePreferencesUtils
        SharePreferencesUtils.context = this
        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder().build())

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        try {
//creating socket instance
            mSocket = mSocketManager
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

        initSocketListener()
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations && !activityVisible) {
            /*App in foreground*/
            activityVisible = true
        }
    }


    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            /*App in background*/
            activityVisible = false
        }
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (activityReferences == 0) {
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    private fun initSocketListener() {
        mSocket.setConnectedCallback {
            mSocket.onChannel("test", Emitter.Listener { args ->
                Timber.e(
                    "Listener Emit Test ----> ${
                        args.get(0).toString()
                    }"
                )
            })

            mSocket.onChannel(Constants.KEY_RECEIVE_MESSAGE, Emitter.Listener {
                /*SocketManager.parseSocketData(it) { data ->
                    val listType = object : TypeToken<List<String?>?>() {}.type

                    val yourList: List<String> = Gson().fromJson(data, listType)
                    Timber.e("receiver_message" + yourList)
                    val mes =
                        Gson().fromJson(yourList.get(yourList.size - 1), ChatMessage::class.java)

                    *//*if(mes.roomId == this.roomId && mes.userId!=this.userId){
                        RxEvent.send(SystemEvent.SocketData(data = this))
                    }*//*

                    RxEvent.send(SystemEvent.SocketData(data = mes))
                }*/


                val messageList = it[0] as JSONArray
                val listType: Type = object : TypeToken<List<ChatMessage?>?>() {}.type
                val list : ArrayList<ChatMessage> = Gson().fromJson(messageList.toString(), listType)
                val mes = MessageSocket(list = list)
                RxEvent.send(SystemEvent.SocketData(data = mes))

                /*SocketManager.parseSocketData(it) { data ->
                    val messageList = it[0] as JSONObject
                    val listType = object : TypeToken<List<ChatMessage?>?>() {}.type
                    val yourList: ArrayList<ChatMessage> = Gson().fromJson(messageList.toString(), listType)
                    Timber.e("receiver_message" + yourList)
//                    val mes = Gson().fromJson(yourList.get(yourList.size - 1), ChatMessage::class.java)

                    if(mes.roomId == this.roomId && mes.userId!=this.userId){
                        RxEvent.send(SystemEvent.SocketData(data = this))
                    }
                    val mes = MessageSocket(list = yourList)
                    RxEvent.send(SystemEvent.SocketData(data = mes))
                }*/
            })


            mSocket.onChannel("ids", Emitter.Listener {
                val ids = it[0] as JSONArray
                val listType: Type = object : TypeToken<List<OnlineUser?>?>() {}.type
                val list : ArrayList<OnlineUser> = Gson().fromJson(ids.toString(), listType)
                val mes = UserSocket(list = list)
                RxEvent.send(SystemEvent.SocketData(data = mes))

            })

            mSocket.onChannel("id", Emitter.Listener {
                val id = it[0] as String
                onlineUser.id = id
                Timber.e("onlineUser id:" + id)
            })
        }
    }


    companion object {
        lateinit var instance: MyApp
        private var activityVisible: Boolean = false
        fun isAppVisibility(): Boolean = activityVisible
        lateinit var mSocket: SocketManager
        var onlineUser =  OnlineUser(isMe = true)

        fun updateUser(user: OnlineUser){
            onlineUser = user

        }
    }
}