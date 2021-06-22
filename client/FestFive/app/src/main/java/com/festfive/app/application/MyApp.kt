package com.festfive.app.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.festfive.app.BuildConfig
import com.festfive.app.model.*
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




    companion object {
        lateinit var instance: MyApp
        private var activityVisible: Boolean = false
        fun isAppVisibility(): Boolean = activityVisible
        lateinit var mSocket: SocketManager
        var onlineUser =  OnlineUser(isMe = true)

        fun updateUser(user: OnlineUser){
            onlineUser = user

        }

        fun onDestroy(){
            onlineUser = OnlineUser()
            mSocket.disconnect()
        }

        fun initSocketListener() {
            mSocket.setConnectedCallback {
                mSocket.onChannel("test", Emitter.Listener { args ->
                    Timber.e(
                        "Listener Emit Test ----> ${
                            args.get(0).toString()
                        }"
                    )
                })

                mSocket.onChannel(Constants.KEY_RECEIVE_MESSAGE, Emitter.Listener {
                    val messageList = it[0] as JSONArray
                    Timber.e("onChannel ${Constants.KEY_RECEIVE_MESSAGE}: $messageList")
                    val listType: Type = object : TypeToken<List<ChatMessage?>?>() {}.type
                    val list : ArrayList<ChatMessage> = Gson().fromJson(messageList.toString(), listType)
                    val mes = MessageSocket(list = list)
                    RxEvent.send(SystemEvent.SocketData(data = mes))
                })


                mSocket.onChannel("ids", Emitter.Listener {
                    val ids = it[0] as JSONArray
                    Timber.e("onChannel ids:" + ids)
                    val listType: Type = object : TypeToken<List<OnlineUser?>?>() {}.type
                    val list : ArrayList<OnlineUser> = Gson().fromJson(ids.toString(), listType)
                    list.find { user -> user.id ==  onlineUser.id}?.apply {
                        isMe = true
                    }
                    val mes = UserSocket(list = list)
                    RxEvent.send(SystemEvent.SocketData(data = mes))

                })

                mSocket.onChannel("id", Emitter.Listener {
                    val id = it[0] as String
                    onlineUser.id = id
                    Timber.e("onChannel id:" + id)
                    val mes = OnlineUser(id = id)
                    RxEvent.send(SystemEvent.SocketData(data = mes))
                })

                mSocket.onChannel(Constants.KEY_STREAM_VIDEO_CALL, Emitter.Listener {
                    val data = it[0] as JSONObject
                    Timber.e("onChannel video_call:" + data)
                    val mes = Gson().fromJson<StreamSocket>(data.toString(), StreamSocket::class.java).apply {
                        payload = data.getJSONObject("payload")
                    }

                    Timber.e("onChannel message:" + mes)
                    RxEvent.send(SystemEvent.SocketData(data = mes))
                })

                mSocket.onChannel(Constants.KEY_INCOMING_VIDEO_CALL, Emitter.Listener {
                    val id = it[0] as JSONObject
                    Timber.e("onChannel $:" + id)
                    val mes = VideoCall(from = id.getString("from"), to = id.getString("to"), isReceive=true)
                    RxEvent.send(SystemEvent.SocketData(data = mes))
                })
            }
        }
    }
}