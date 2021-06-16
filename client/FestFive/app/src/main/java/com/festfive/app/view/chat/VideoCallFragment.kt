package com.festfive.app.view.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.base.view.BaseFragment
import com.festfive.app.base.viewmodel.EmptyViewModel
import com.festfive.app.databinding.FragmentVideoCallBinding
import com.festfive.app.extension.getDefault
import com.festfive.app.utils.Constants
import com.festfive.app.viewmodel.chat.StreamViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tbruyelle.rxpermissions2.RxPermissions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.EglBase
import org.webrtc.MediaStream
import timber.log.Timber
import java.lang.reflect.Type


class VideoCallFragment : BaseFragment<FragmentVideoCallBinding, StreamViewModel> (){
    private var webRtcClient: WebRtcClient? = null
    private val eglBase by lazy { EglBase.create() }
    override fun getLayoutRes(): Int = R.layout.fragment_video_call
    var friendId = ""

    override fun initView() {
        super.initView()
        friendId = arguments?.getString(Constants.KEY_PUT_OBJECT).toString()

//        webRtcClient?.startLocalCamera(android.os.Build.MODEL, requireActivity())

        dataBinding.apply {
            txtRoomId.setOnClickListener{
                webRtcClient?.friendId = friendId
//                webRtcClient?.startLocalCamera(android.os.Build.MODEL, requireActivity())

                webRtcClient?.callByClientId(friendId)
            }

            localRenderer.apply {
                setEnableHardwareScaler(false)
                init(eglBase.eglBaseContext, null)
            }

            remoteRenderer.apply {
                setEnableHardwareScaler(false)
                init(eglBase.eglBaseContext, null)
            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        mViewModel.requestPermission(RxPermissions(this),
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        ) { granted ->
            if (granted){
                webRtcClient?.onDestroy()
                startWebRTC()
            }
        }

        mViewModel.apply {
            streamSocket.observe(viewLifecycleOwner, Observer {
                it?.let {
                    webRtcClient?.processStreamSocket(it)
                }
            })
        }
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
                    remoteStream.videoTracks[0].addSink(dataBinding.remoteRenderer)
                }

                @SuppressLint("LongLogTag")
                override fun onRemoveRemoteStream(endPoint: Int) {
                    Log.d("startWebRTC onRemoveRemoteStream ", endPoint.toString())
                }
            })
//        webRtcClient?.startLocalCamera(android.os.Build.MODEL, this@MainActivity)
    }
}