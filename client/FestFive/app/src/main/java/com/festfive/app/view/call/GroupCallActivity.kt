package com.festfive.app.view.call

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.base.view.BaseActivity
import com.festfive.app.customize.listener.SignallingClientListener
import com.festfive.app.databinding.ActivityGroupCallBinding
import com.festfive.app.extension.disable
import com.festfive.app.extension.getDefault
import com.festfive.app.extension.initGrid
import com.festfive.app.model.OnlineUser
import com.festfive.app.model.StreamSocket
import com.festfive.app.utils.Constants
import com.festfive.app.viewmodel.call.GroupCallViewModel
import com.festfive.app.viewmodel.call.GroupClientListener
import com.festfive.app.viewmodel.call.WebRtcGroup
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_group_call.*
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber


class GroupCallActivity :BaseActivity<ActivityGroupCallBinding, GroupCallViewModel>(),
    GroupClientListener {
    private val TAG = "GroupCallActivity: "

    private val mAdapter : UserCallGroupAdapter by lazy {
        UserCallGroupAdapter {
            Timber.e("$TAG UserCallGroupAdapter ${it.toString()}")
            if(!it.isMe){
                callFriend(it)
            }
        }
    }

    fun callFriend(user: OnlineUser) {
        if(!listFriendCalled.contains(user) && mAdapter.findMe(user.id.getDefault()) < mAdapter.findMe(myId)){
            Timber.e("callFriend ${user.toString()}")
            webRtcClient?.callByClientId(user.id.getDefault())
            listFriendCalled.add(user)
        }
    }

    private var task: Disposable? = null
    private var taskEndCall: Disposable? = null
    private var webRtcClient: WebRtcGroup? = null
    private val eglBase by lazy { EglBase.create() }
    private var mAudio: AudioManager? = null
    private var cameraOn: Boolean = true
    private var speakerOn: Boolean = true
    private var isMute: Boolean = false

    private var myId = ""
    private var myRoomId = ""
    var endCallSubject = PublishSubject.create<Boolean>()
    var listSurfaceView : MutableList<SurfaceViewRenderer> = mutableListOf()
    var listFriendCalled : MutableList<OnlineUser> = mutableListOf()

    override fun getLayoutRes(): Int = R.layout.activity_group_call

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myRoomId = intent.getStringExtra(Constants.KEY_PUT_OBJECT).toString()
        mViewModel.myRoom = myRoomId
        myId = MyApp.onlineUser.id.getDefault()
        mAudio = getApplicationContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        taskEndCall = (endCallSubject
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    true -> {
                        Toast.makeText(this, "End call!", Toast.LENGTH_SHORT).show()
//                        this.finish()
                    }

                }
            }, {}))
    }

    override fun initView() {
        super.initView()
//        showVideoView(false)

        dataBinding.apply {
            iView = this@GroupCallActivity

            rc_user.apply {
                adapter = mAdapter
                initGrid(3)
            }

            mViewModel.requestPermission(
                RxPermissions(this@GroupCallActivity),
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            ) { granted ->
                if (granted){
                    webRtcClient?.onDestroy()
                    startWebRTC()
                    checkIsCalling()
                }
            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        mViewModel.setCallback(this)
        mViewModel.apply {
            userList.observe(this@GroupCallActivity, androidx.lifecycle.Observer {
                mAdapter.updateData(it)
            })
        }

    }

    fun setSpeaker() {
        speakerOn = !speakerOn
        Timber.e("$TAG setHead to $speakerOn")
        mAudio?.apply {
            isSpeakerphoneOn = speakerOn
            mode = AudioManager.MODE_IN_COMMUNICATION
        }

        /*if(speakerOn){
            dataBinding.changeSpeaker.setImageResource(R.drawable.ic_speaker_on)
        } else {
            dataBinding.changeSpeaker.setImageResource(R.drawable.ic_speaker_off)
        }*/
    }

    fun setCamera() {
        cameraOn = !cameraOn
        Timber.e("$TAG setCamera to $cameraOn")
        webRtcClient?.SetCamera(cameraOn)
       /* if(cameraOn){
            dataBinding.setCameraOnOff.setImageResource(R.drawable.ic_camera_on)
        } else {
            dataBinding.setCameraOnOff.setImageResource(R.drawable.ic_camera_off)
        }*/
    }

    fun setMicrophoneMute() {
        isMute = !isMute
        Timber.e("$TAG setMicrophoneMute to $isMute")
        mAudio?.apply {
            isMicrophoneMute = isMute
            mode = AudioManager.MODE_IN_COMMUNICATION
        }

        /*if(isMute){
            dataBinding.changeMic.setImageResource(R.drawable.ic_mic_off)
        } else {
            dataBinding.changeMic.setImageResource(R.drawable.ic_mic_on)
        }*/
    }

    fun setFrontCamera(){
        webRtcClient?.setFrontCamera()
    }

    @SuppressLint("LongLogTag")
    private fun startWebRTC() {
        Log.d("startWebRTC ", "startWebRTC")
        webRtcClient = WebRtcGroup(
            myRoomId,
            this.application,
            eglBase.eglBaseContext,
            object : WebRtcGroup.RtcListener {
                @SuppressLint("LongLogTag")
                override fun onOnlineIdsChanged(jsonArray: JSONArray) {
                    runOnUiThread {
                        Log.d("startWebRTC onOnlineIdsChanged list", jsonArray.toString())
                    }
                }

                override fun onCallReady(callId: String) {
                    runOnUiThread {
                        Log.d("startWebRTC onCallReady", callId)
                        webRtcClient?.startLocalCamera(Build.MODEL, this@GroupCallActivity)
                    }
                }

                @SuppressLint("LongLogTag")
                override fun onStatusChanged(newStatus: String) {
                    Log.d("startWebRTC onStatusChanged ", newStatus)
                    runOnUiThread {
                        if (newStatus == PeerConnection.IceConnectionState.DISCONNECTED.name) {

                        }
//                        Toast.makeText(this@MainActivity, newStatus, Toast.LENGTH_SHORT).show()
                    }
                }

                @SuppressLint("LongLogTag")
                override fun onLocalStream(localStream: MediaStream) {
                    Log.d("startWebRTC onLocalStream ", Gson().toJson(localStream))
                    addSurfaceView(localStream)
                }

                @SuppressLint("LongLogTag")
                override fun onAddRemoteStream(remoteStream: MediaStream, endPoint: Int) {
                    Timber.e("startWebRTC onAddRemoteStream ${Gson().toJson(remoteStream)}  endPoint : $endPoint" )

                    runOnUiThread {
                        addSurfaceView(remoteStream)
                    }
                }

                @SuppressLint("LongLogTag")
                override fun onRemoveRemoteStream(endPoint: Int) {
                    Log.d("startWebRTC onRemoveRemoteStream ", endPoint.toString())
                }
            })
    }

    private fun checkIsCalling() {
        onStartCall()
    }

    fun onStartCall() {
        Timber.e(TAG + "onStartCall to $myRoomId")
        val message = JSONObject()
        message.put("to", myRoomId)
        message.put("from", myId)
        mViewModel.onStartCall(myRoomId)
        webRtcClient?.onCallReady(myId)
    }

    fun endCall() {
        Timber.e(TAG + "endCall to $myRoomId")
        task?.dispose()
        mViewModel.endCall(myRoomId)
        releaseSurfaceView()
        killActivity()
    }

    override fun onEndCall(id: String) {
        Timber.e(TAG + "friend onEndCall: $id")
//        releaseSurfaceView(mAdapter.findMe(id))
        runOnUiThread {
            mAdapter.removeData(mAdapter.findMe(id))
        }
        listFriendCalled.find { user ->
            user.id ==id
        }.apply {
            listFriendCalled.remove(this)
        }
    }

    override fun onProcessStreamSocket(data: StreamSocket) {
        data?.let {
            webRtcClient?.processStreamSocket(it)
        }
    }
    private fun killActivity() {
        runOnUiThread {
            Timber.e(TAG + "killActivity ")
            finish()
        }
    }

    fun addSurfaceView(localStream: MediaStream) {
        var sc_width = getDisplayMetrics().widthPixels
        var sc_height = getDisplayMetrics().heightPixels
        val view = SurfaceViewRenderer(this).apply {
            /*layoutParams = LinearLayout.LayoutParams(
                GridLayout.LayoutParams.WRAP_CONTENT,
                GridLayout.LayoutParams.WRAP_CONTENT
            )*/

            layoutParams = LinearLayout.LayoutParams(
                sc_width/3,
                sc_height/3
            )
//            holder.setFixedSize(sc_width, sc_height)
            setEnableHardwareScaler(false)
            init(eglBase.eglBaseContext, null)
            setZOrderMediaOverlay(false)
        }
        dataBinding.info.addView(view)
        listSurfaceView.add(view)
        localStream.videoTracks[0].addSink(listSurfaceView.get(listSurfaceView.size-1))
        dataBinding.info.columnCount = 3
        /*
        dataBinding.info.columnCount = if(listSurfaceView.size+1>=4) (listSurfaceView.size+1)/2  else 1
        dataBinding.info.rowCount = if(dataBinding.info.columnCount>=2) (listSurfaceView.size+1)/dataBinding.info.columnCount  else listSurfaceView.size/2+1

        listSurfaceView.forEach {view ->
//            listSurfaceView[i].holder.setFixedSize(sc_width/dataBinding.info.columnCount, sc_height/dataBinding.info.rowCount)
//            listSurfaceView[i].setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            view.scaleX = dataBinding.info.columnCount.toFloat()
            view.scaleY = dataBinding.info.rowCount.toFloat()
            Timber.e("listSurfaceView[i].scaleX  " + view.scaleX + "scaleY  " + view.scaleY)
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        }*/
    }

    private fun releaseSurfaceView(){
        for(i in 0 until listSurfaceView.size){
            listSurfaceView[i].clearAnimation()
            listSurfaceView[i].disable()
        }
        listSurfaceView.clear()
    }

    fun releaseSurfaceView(pos : Int){
        listSurfaceView[pos].clearAnimation()
        listSurfaceView[pos].disable()
    }

     fun getDisplayMetrics() : DisplayMetrics {
         val displayMetrics = DisplayMetrics()
         windowManager.defaultDisplay.getMetrics(displayMetrics)
         return displayMetrics
     }

    override fun onPause() {
        super.onPause()
        webRtcClient?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webRtcClient?.onResume()
    }

    override fun onStop() {
        Timber.e("$TAG onStop")
        task?.dispose()
        taskEndCall?.dispose()
        mViewModel.endCall(myRoomId)
        webRtcClient?.onDestroy()
        releaseSurfaceView()
        super.onStop()
    }
}