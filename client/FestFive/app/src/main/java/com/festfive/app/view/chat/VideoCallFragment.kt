package com.festfive.app.view.chat

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.base.view.BaseFragment
import com.festfive.app.databinding.FragmentVideoCallBinding
import com.festfive.app.extension.getDefault
import com.festfive.app.extension.initLinear
import com.festfive.app.utils.Constants
import com.festfive.app.viewmodel.chat.StreamViewModel
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_setup.*
import org.json.JSONArray
import org.webrtc.EglBase
import org.webrtc.MediaStream
import org.webrtc.PeerConnection


class VideoCallFragment : BaseFragment<FragmentVideoCallBinding, StreamViewModel> (){
    private val userListAdapter : UserListAdapter by lazy {
        UserListAdapter {
            if(it.isCall){
                webRtcClient?.callByClientId(it.user.id.getDefault())
            }
        }
    }

    private var webRtcClient: WebRtcClient? = null
    private val eglBase by lazy { EglBase.create() }
    override fun getLayoutRes(): Int = R.layout.fragment_video_call
    var friendId = ""

    override fun initView() {
        super.initView()
        friendId = arguments?.getString(Constants.KEY_PUT_OBJECT).toString()

        dataBinding.apply {
            rc_user.apply {
                adapter = userListAdapter
                initLinear(RecyclerView.VERTICAL)
            }

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
                    MyApp.mSocket.emitData(Constants.KEY_REFRESH_IDS, "")
                }
            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()

        mViewModel.apply {
            streamSocket.observe(viewLifecycleOwner, Observer {
                it?.let {
                    webRtcClient?.processStreamSocket(it)
                }
            })

            myId.observe(viewLifecycleOwner, Observer {
                if(!it.isNullOrEmpty()){
                    webRtcClient?.onCallReady(it)
                }
            })

            getUsers().observe(viewLifecycleOwner, Observer {
                userListAdapter.updateData(it)
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
                        updateUI()
                        remoteStream.videoTracks[0].addSink(dataBinding.remoteRenderer)
                    }
                }

                @SuppressLint("LongLogTag")
                override fun onRemoveRemoteStream(endPoint: Int) {
                    Log.d("startWebRTC onRemoveRemoteStream ", endPoint.toString())
                }
            })
    }


    private fun updateUI() {
//        dataBinding.localRenderer.setZOrderOnTop(true)
//        dataBinding.remoteRenderer.setZOrderMediaOverlay(false)
        /*dataBinding.localRenderer.setVisibility(View.GONE)
        dataBinding.remoteRenderer.setVisibility(View.GONE)
        dataBinding.container.removeView(dataBinding.localRenderer)
        dataBinding.container.removeView(dataBinding.remoteRenderer)
        dataBinding.localRenderer.setZOrderOnTop(true)
        dataBinding.remoteRenderer.setZOrderOnTop(false)
        dataBinding.container.addView(
            dataBinding.localRenderer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        dataBinding.container.addView(
            dataBinding.remoteRenderer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        dataBinding.localRenderer.setVisibility(View.VISIBLE)
        dataBinding.remoteRenderer.setVisibility(View.VISIBLE)*/
    }

    override fun onPause() {
        super.onPause()
        webRtcClient?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webRtcClient?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        webRtcClient?.onDestroy()
    }
}