package com.festfive.app.view.chat

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.base.view.BaseFragment
import com.festfive.app.customize.listener.SignallingClientListener
import com.festfive.app.databinding.FragmentVideoCallBinding
import com.festfive.app.extension.disable
import com.festfive.app.extension.getDefault
import com.festfive.app.extension.initLinear
import com.festfive.app.model.OnlineUser
import com.festfive.app.model.StreamSocket
import com.festfive.app.model.VideoCall
import com.festfive.app.utils.Constants
import com.festfive.app.viewmodel.chat.VideoCallViewModel
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_setup.*
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.EglBase
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import timber.log.Timber


class VideoCallFragment : BaseFragment<FragmentVideoCallBinding, VideoCallViewModel>(), SignallingClientListener{
    private val TAG = "VideoCallFragment: "

    private var webRtcClient: WebRtcClient? = null
    private val eglBase by lazy { EglBase.create() }
    override fun getLayoutRes(): Int = R.layout.fragment_video_call

    private var myId = ""
    private var friendId = ""

    override fun initView() {
        super.initView()
//        showVideoView(false)

        dataBinding.apply {
            iView = this@VideoCallFragment

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

            mViewModel.requestPermission(RxPermissions(requireActivity()),
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
        val videoCall = Gson().fromJson<VideoCall>(arguments?.getString(Constants.KEY_PUT_OBJECT).toString(), VideoCall::class.java)
        if(videoCall.isReceive.getDefault()){
            friendId = videoCall.from.getDefault()
            myId = videoCall.to.getDefault()
        } else {
            friendId = videoCall.to.getDefault()
            myId = videoCall.from.getDefault()
            dataBinding.acceptCall.visibility = View.GONE
        }

        mViewModel.setCallback(this)
    }

    @SuppressLint("LongLogTag")
    private fun startWebRTC() {
        Log.d("startWebRTC ", "startWebRTC")
        webRtcClient = WebRtcClient(
            requireActivity().application,
            eglBase.eglBaseContext,
            object : WebRtcClient.RtcListener {
                @SuppressLint("LongLogTag")
                override fun onOnlineIdsChanged(jsonArray: JSONArray) {
                    activity?.runOnUiThread {
                        Log.d("startWebRTC onOnlineIdsChanged list", jsonArray.toString())
                    }
                }

                override fun onCallReady(callId: String) {
                    activity?.runOnUiThread {
                        Log.d("startWebRTC onCallReady", callId)
                        webRtcClient?.startLocalCamera(android.os.Build.MODEL, requireActivity())
                    }
                }

                @SuppressLint("LongLogTag")
                override fun onStatusChanged(newStatus: String) {
                    Log.d("startWebRTC onStatusChanged ", newStatus)
                    activity?.runOnUiThread {
                        if(newStatus == PeerConnection.IceConnectionState.DISCONNECTED.name){

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

                    activity?.runOnUiThread {
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
        val videoCall = Gson().fromJson<VideoCall>(arguments?.getString(Constants.KEY_PUT_OBJECT).toString(), VideoCall::class.java)
        if(!videoCall.isReceive.getDefault()){
            onStartCall()
        }
    }

    fun onStartCall() {
        Timber.e(TAG + "onStartCall to $friendId")
        val message = JSONObject()
        message.put("to", friendId)
        message.put("from", myId)
        MyApp.mSocket.emitData(Constants.KEY_START_VIDEO_CALL, message)
        webRtcClient?.onCallReady(myId)
    }

    fun onStartAnswer() {
        Timber.e(TAG + "onStartAnswer to $friendId")
        dataBinding.acceptCall.visibility = View.GONE
        MyApp.mSocket.emitData(Constants.KEY_START_ANSWER, friendId)
        webRtcClient?.onCallReady(myId)
        webRtcClient?.callByClientId(friendId)
        showVideoView(true)
    }

    fun endCall() {
        Timber.e(TAG + "endCall to $friendId")
        mViewModel.endCall(friendId)
        webRtcClient?.onDestroy()
        dataBinding.localRenderer.clearAnimation()
        dataBinding.remoteRenderer.clearAnimation()
        dataBinding.localRenderer.disable()
        dataBinding.remoteRenderer.disable()
        killActivity()
    }

    override fun onAnswerAccept() {
        Timber.e(TAG + "onAnswerAccept from $friendId")
        showVideoView(true)
    }

    override fun onEndCall() {
        endCall()
        Timber.e(TAG + "onEndCall from $friendId")
        webRtcClient?.onDestroy()
        dataBinding.localRenderer.clearAnimation()
        dataBinding.remoteRenderer.clearAnimation()
        dataBinding.localRenderer.disable()
        dataBinding.remoteRenderer.disable()
        killActivity()
    }

    override fun onProcessStreamSocket(data: StreamSocket) {
        data?.let {
            webRtcClient?.processStreamSocket(it)
        }
    }

    fun killActivity(){
//        navController.popBackStack()
        onHandleBackPressed()
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
        mViewModel.endCall(friendId)
        webRtcClient?.onDestroy()
    }
}