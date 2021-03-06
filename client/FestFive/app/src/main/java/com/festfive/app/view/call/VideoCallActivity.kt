package com.festfive.app.view.call

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.base.view.BaseActivity
import com.festfive.app.customize.listener.SignallingClientListener
import com.festfive.app.databinding.ActivityVideoCallBinding
import com.festfive.app.extension.disable
import com.festfive.app.extension.getDefault
import com.festfive.app.model.StreamSocket
import com.festfive.app.model.VideoCall
import com.festfive.app.utils.Constants
import com.festfive.app.viewmodel.call.VideoCallViewModel
import com.festfive.app.viewmodel.call.WebRtcClient
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.EglBase
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import timber.log.Timber
import java.util.concurrent.TimeUnit


class VideoCallActivity :BaseActivity<ActivityVideoCallBinding, VideoCallViewModel>(), SignallingClientListener {
    private val TAG = "VideoCallActivity: "

    private var task: Disposable? = null
    private var taskEndCall: Disposable? = null
    private var webRtcClient: WebRtcClient? = null
    private val eglBase by lazy { EglBase.create() }
    private var mAudio: AudioManager? = null
    private var cameraOn: Boolean = true
    private var speakerOn: Boolean = true
    private var isMute: Boolean = false

    private var myId = ""
    private var friendId = ""
    var endCallSubject = PublishSubject.create<Boolean>()

    override fun getLayoutRes(): Int = R.layout.activity_video_call

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAudio = getApplicationContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val videoCall = Gson().fromJson<VideoCall>(intent.getStringExtra(Constants.KEY_PUT_OBJECT).toString(), VideoCall::class.java)
        if(videoCall.isReceive.getDefault()){
            friendId = videoCall.from.getDefault()
            myId = videoCall.to.getDefault()
        } else {
            friendId = videoCall.to.getDefault()
            myId = videoCall.from.getDefault()
            dataBinding.acceptCall.visibility = View.GONE
        }
        mViewModel.setCallback(this)

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
            iView = this@VideoCallActivity

            localRenderer.apply {
                setEnableHardwareScaler(false)
                init(eglBase.eglBaseContext, null)
                setZOrderOnTop(true)
            }

            remoteRenderer.apply {
                setEnableHardwareScaler(false)
                init(eglBase.eglBaseContext, null)
                setZOrderMediaOverlay(false)
            }

            mViewModel.requestPermission(
                RxPermissions(this@VideoCallActivity),
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
    }

    fun setSpeaker() {
        speakerOn = !speakerOn
        Timber.e("$TAG setHead to $speakerOn")
        mAudio?.apply {
            isSpeakerphoneOn = speakerOn
            mode = AudioManager.MODE_IN_COMMUNICATION
        }

        if(speakerOn){
            dataBinding.changeSpeaker.setImageResource(R.drawable.ic_speaker_on)
        } else {
            dataBinding.changeSpeaker.setImageResource(R.drawable.ic_speaker_off)
        }
    }

    fun setCamera() {
        cameraOn = !cameraOn
        Timber.e("$TAG setCamera to $cameraOn")
        webRtcClient?.SetCamera(cameraOn)
        if(cameraOn){
            dataBinding.setCameraOnOff.setImageResource(R.drawable.ic_camera_on)
        } else {
            dataBinding.setCameraOnOff.setImageResource(R.drawable.ic_camera_off)
        }
    }

    fun setMicrophoneMute() {
        isMute = !isMute
        Timber.e("$TAG setMicrophoneMute to $isMute")
        mAudio?.apply {
            isMicrophoneMute = isMute
            mode = AudioManager.MODE_IN_COMMUNICATION
        }

        if(isMute){
            dataBinding.changeMic.setImageResource(R.drawable.ic_mic_off)
        } else {
            dataBinding.changeMic.setImageResource(R.drawable.ic_mic_on)
        }
    }

    fun setFrontCamera(){
        webRtcClient?.setFrontCamera()
    }

    @SuppressLint("LongLogTag")
    private fun startWebRTC() {
        Log.d("startWebRTC ", "startWebRTC")
        webRtcClient = WebRtcClient(
            this.application,
            eglBase.eglBaseContext,
            object : WebRtcClient.RtcListener {
                @SuppressLint("LongLogTag")
                override fun onOnlineIdsChanged(jsonArray: JSONArray) {
                    runOnUiThread {
                        Log.d("startWebRTC onOnlineIdsChanged list", jsonArray.toString())
                    }
                }

                override fun onCallReady(callId: String) {
                    runOnUiThread {
                        Log.d("startWebRTC onCallReady", callId)
                        webRtcClient?.startLocalCamera(Build.MODEL, this@VideoCallActivity)
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
                    localStream.videoTracks[0].addSink(dataBinding.localRenderer)
                }

                @SuppressLint("LongLogTag")
                override fun onAddRemoteStream(remoteStream: MediaStream, endPoint: Int) {
                    Log.d("startWebRTC onAddRemoteStream ", Gson().toJson(remoteStream))

                    runOnUiThread {
                        remoteStream.videoTracks[0].addSink(dataBinding.remoteRenderer)
                    }
                }

                @SuppressLint("LongLogTag")
                override fun onRemoveRemoteStream(endPoint: Int) {
                    Log.d("startWebRTC onRemoveRemoteStream ", endPoint.toString())
                }
            })
    }

    private fun checkIsCalling() {
        val videoCall = Gson().fromJson<VideoCall>(intent.getStringExtra(Constants.KEY_PUT_OBJECT).toString(), VideoCall::class.java)
        if(!videoCall.isReceive.getDefault()){
            onStartCall()
            task = Observable.timer(20000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(this, "Not answer!", Toast.LENGTH_SHORT).show()
                    mViewModel.endCall(friendId)
                    webRtcClient?.onDestroy()
                    Handler().postDelayed({
                        finish()
                    }, 2000)
                }
        }
    }

    fun onStartCall() {
        Timber.e(TAG + "onStartCall to $friendId")
        val message = JSONObject()
        message.put("to", friendId)
        message.put("from", myId)
        mViewModel.onStartCall(message)
        webRtcClient?.onCallReady(myId)
    }

    fun onStartAnswer() {
        Timber.e(TAG + "onStartAnswer to $friendId")
        dataBinding.acceptCall.visibility = View.GONE
        mViewModel.onStartAnswer(friendId)
        webRtcClient?.onCallReady(myId)
        webRtcClient?.callByClientId(friendId)
        showVideoView(true)
    }

    fun endCall() {
        Timber.e(TAG + "endCall to $friendId")
        task?.dispose()
        mViewModel.endCall(friendId)
       onEndCall()
    }

    override fun onAnswerAccept() {
        Timber.e(TAG + "onAnswerAccept from $friendId")
        task?.dispose()
        showVideoView(true)
    }

    override fun onEndCall() {
        Timber.e(TAG + "onEndCall")
        webRtcClient?.onDestroy()
        dataBinding.localRenderer.clearAnimation()
        dataBinding.remoteRenderer.clearAnimation()
        dataBinding.localRenderer.disable()
        dataBinding.remoteRenderer.disable()
        killActivity()
        endCallSubject.onNext(true)
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

    private fun showVideoView(isShow: Boolean) {
        if(isShow){
            dataBinding.localRenderer.visibility = View.VISIBLE
            dataBinding.remoteRenderer.visibility = View.VISIBLE
        } else {
            dataBinding.localRenderer.visibility = View.INVISIBLE
            dataBinding.remoteRenderer.visibility = View.INVISIBLE
        }

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
        super.onStop()
        task?.dispose()
        taskEndCall?.dispose()
        mViewModel.endCall(friendId)
        webRtcClient?.onDestroy()
    }
}