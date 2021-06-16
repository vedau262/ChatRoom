package com.festfive.app.view.chat

import android.app.Application
import android.content.Context
import android.util.Log
import com.festfive.app.application.MyApp
import com.festfive.app.extension.getDefault
import com.festfive.app.model.StreamSocket
import com.festfive.app.push.SocketManager
import com.github.nkzawa.socketio.client.Socket
import io.reactivex.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject

class WebRtcClient(
       private val app: Application,
       private val eglContext: EglBase.Context,
       private val webrtcListener: RtcListener
) {

    private val endPoints = BooleanArray(MAX_PEER)
    private val factory: PeerConnectionFactory
    private val peers = HashMap<String, Peer>()
    private val iceServers = LinkedList<PeerConnection.IceServer>()
    private val pcConstraints = MediaConstraints()
    private var localMS: MediaStream? = null
    private var videoSource: VideoSource? = null

    private val mVideoCapturer by lazy { getVideoCapturer() }

    init {
        //Initialize PeerConnectionFactory globals.
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(app).createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)
        val options = PeerConnectionFactory.Options()

        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(eglContext,   true, true)
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglContext)
        factory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()

        //Used when initializing the ICE server to create a PC
        iceServers.add(PeerConnection.IceServer.builder("stun:23.21.150.121").createIceServer())
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        iceServers.add(PeerConnection.IceServer.builder("turn:numb.viagenie.ca").setUsername("webrtc@live.com").setPassword("muazkh").createIceServer())


        //Initialize the local MediaConstraints used when creating the PC, which is the configuration information of the streaming media
        pcConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        pcConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        pcConstraints.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))

        MyApp.mSocket.emitData("get_id", "get_id")
    }
    fun onCallReady(id: String) {
        webrtcListener.onCallReady(id)
    }
    private fun getVideoCapturer() =
        Camera2Enumerator(app).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    fun startLocalCamera(name: String, context: Context) {
        //init local media stream
        val localVideoSource = factory.createVideoSource(false)
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(
                Thread.currentThread().name, eglContext
            )
        (mVideoCapturer as VideoCapturer).initialize(
            surfaceTextureHelper,
            context,
            localVideoSource.capturerObserver
        )
        mVideoCapturer.startCapture(320, 240, 60)
        localMS = factory.createLocalMediaStream("LOCALMEDIASTREAM")
        localMS?.addTrack(factory.createVideoTrack("LOCALMEDIASTREAM", localVideoSource))
        webrtcListener.onLocalStream(localMS!!)
    }


    /**
     * Call this method in Activity.onPause()
     */
    fun onPause() {
        videoSource?.capturerObserver?.onCapturerStopped()
    }

    /**
     * Call this method in Activity.onResume()
     */
    fun onResume() {
        videoSource?.capturerObserver?.onCapturerStarted(true)
    }

    /**
     * Call this method in Activity.onDestroy()
     */
    fun onDestroy() {
        for (peer in peers.values) {
            peer.pc?.dispose()
        }
        videoSource?.dispose()
        factory.dispose()
       /* socket?.disconnect()
        socket?.close()*/
    }

    /**
     * Implement this interface to be notified of events.
     */
    interface RtcListener {
        fun onCallReady(callId: String)

        fun onStatusChanged(newStatus: String)

        fun onLocalStream(localStream: MediaStream)

        fun onAddRemoteStream(remoteStream: MediaStream, endPoint: Int)

        fun onRemoveRemoteStream(endPoint: Int)

        fun onOnlineIdsChanged(jsonArray: JSONArray) {}
    }


    fun callByClientId(clientId: String) {
        sendMessage(clientId, "init", JSONObject())
    }

    /**
     * Send a message through the signaling server
     *
     * @param to      id of recipient
     * @param type    type of message
     * @param payload payload of message
     * @throws JSONException
     */
    @Throws(JSONException::class)
    private fun sendMessage(to: String, type: String, payload: JSONObject) {
        Timber.e("sendMessage to " + to)
        val message = JSONObject()
        message.put("to", to)
        message.put("type", type)
        message.put("payload", payload)
        MyApp.mSocket.emitData("message", message)
    }


    fun processStreamSocket(data: StreamSocket){
        Timber.e("processStreamSocket: "+ data)
        try {
            val from = data.from
            val type = data.type
            var payload: JSONObject? = null
            if (type != "init") {
                payload = data.payload
            }
            //Used to check whether the PC already exists and whether the maximum 2 PC limit has been reached
            if (!peers.containsKey(from)) {
                val endPoint = findEndPoint()
                if (endPoint == MAX_PEER) return
                else if (localMS != null) {
                    addPeer(from, endPoint)
                } else {

                }
            }
            //Method of responding to corresponding steps according to different instruction types and data
            when (type) {
                "init" -> createOffer(from)
                "offer" -> createAnswer(from, payload)
                "answer" -> setRemoteSdp(from, payload)
                "candidate" -> addIceCandidate(from, payload)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }
    private inner class Peer(val id: String, val endPoint: Int) : SdpObserver, PeerConnection.Observer {

        val pc: PeerConnection?


        init {
            this.pc = factory.createPeerConnection(iceServers, pcConstraints, this)
            pc?.addStream(localMS!!) //, new MediaConstraints()
            Timber.e("new Peer: $id $endPoint ${pc != null}")
            webrtcListener.onStatusChanged("CONNECTING")
        }

        override fun onCreateSuccess(sdp: SessionDescription) {
            Timber.e("onCreateSuccess")
            // TODO: modify sdp to use pcParams prefered codecs
            try {
                val payload = JSONObject()
                payload.put("type", sdp.type.canonicalForm())
                payload.put("sdp", sdp.description)
                sendMessage(id, sdp.type.canonicalForm(), payload)
                pc!!.setLocalDescription(this@Peer, sdp)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        override fun onSetSuccess() {}

        override fun onCreateFailure(s: String) {}

        override fun onSetFailure(s: String) {}

        override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {}

        override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
            webrtcListener.onStatusChanged(iceConnectionState.name)
            Timber.e( "onIceConnectionChange ${iceConnectionState.name}")
            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
                removePeer(id)
            }
        }

        override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {}

        override fun onIceCandidate(candidate: IceCandidate) {
            try {
                val payload = JSONObject()
                payload.put("label", candidate.sdpMLineIndex)
                payload.put("id", candidate.sdpMid)
                payload.put("candidate", candidate.sdp)
                sendMessage(id, "candidate", payload)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        }

        override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        }

        override fun onAddStream(mediaStream: MediaStream) {
            Timber.e( "onAddStream " + mediaStream.id)
            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
            webrtcListener.onAddRemoteStream(mediaStream, endPoint + 1)
        }

        override fun onRemoveStream(mediaStream: MediaStream) {
            Timber.e(  "onRemoveStream " + mediaStream.id)
            removePeer(id)
        }

        override fun onDataChannel(dataChannel: DataChannel) {}

        override fun onRenegotiationNeeded() {

        }
    }

    private fun addPeer(id: String, endPoint: Int): Peer {
        val peer = Peer(id, endPoint)
        peers[id] = peer

        endPoints[endPoint] = true
        return peer
    }

    private fun removePeer(id: String) {
        val peer = peers[id]
        webrtcListener.onRemoveRemoteStream(peer!!.endPoint)
        peer.pc!!.close()
        peers.remove(peer.id)
        endPoints[peer.endPoint] = false
    }

    private fun findEndPoint(): Int {
        for (i in 0 until MAX_PEER)
            if (!endPoints[i]) return i
        return MAX_PEER
    }

    private fun createOffer(peerId: String) {
        Log.d(TAG, "CreateOfferCommand peerId "+ peerId)
        val peer = peers[peerId]
        peer?.pc?.createOffer(peer, pcConstraints)
    }

    private fun createAnswer(peerId: String, payload: JSONObject?) {
        Log.d(TAG, "CreateAnswerCommand payload "+ payload)
        val peer = peers[peerId]
        val sdp = SessionDescription(
            SessionDescription.Type.fromCanonicalForm(payload?.getString("type")),
            payload?.getString("sdp")
        )
        peer?.pc?.setRemoteDescription(peer, sdp)
        peer?.pc?.createAnswer(peer, pcConstraints)
    }

    private fun setRemoteSdp(peerId: String, payload: JSONObject?) {
        Log.d(TAG, "SetRemoteSDPCommand")
        val peer = peers[peerId]
        val sdp = SessionDescription(
            SessionDescription.Type.fromCanonicalForm(payload?.getString("type")),
            payload?.getString("sdp")
        )
        peer?.pc?.setRemoteDescription(peer, sdp)
    }

    private fun addIceCandidate(peerId: String, payload: JSONObject?) {
        Log.d(TAG, "AddIceCandidateCommand "+ payload)
        val pc = peers[peerId]!!.pc
        if (pc!!.remoteDescription != null) {
            val candidate = IceCandidate(
                payload!!.getString("id"),
                payload.getInt("label"),
                payload.getString("candidate")
            )
            pc.addIceCandidate(candidate)
        }
    }

    companion object {
        const val TAG = "WebRtcClient"
        const val MAX_PEER = 2
    }
}