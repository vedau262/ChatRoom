package com.festfive.app.view.call

import com.festfive.app.R
import com.festfive.app.base.adapter.BaseBindingAdapter
import com.festfive.app.base.adapter.BaseViewHolder
import com.festfive.app.databinding.ItemCallBinding
import com.festfive.app.databinding.ItemUserBinding
import com.festfive.app.model.DataStream
import com.festfive.app.model.OnClickUser
import com.festfive.app.model.OnlineUser
import kotlinx.android.synthetic.main.item_call.view.*
import org.webrtc.EglBase
import org.webrtc.MediaStream
import timber.log.Timber

class UserCallGroupAdapter (private val onlineUser : (OnlineUser) -> Unit) :
    BaseBindingAdapter<ItemCallBinding, DataStream>(){
//    private val eglBase by lazy { EglBase.create() }
private val rootEglBase: EglBase = EglBase.create()
    override fun getLayoutId(viewType: Int) = R.layout.item_call

    override fun bindViewHolder(binding: ItemCallBinding, position: Int) {
        val data = list[position]
        binding.apply {
            user = data.onlineUser
            mediaStrem = data.mediaStream
            onlineUser.invoke(data.onlineUser)

            if(data.mediaStream==null){
                localRenderer.apply {
                    setMirror(true)
                    setEnableHardwareScaler(false)
                    init(rootEglBase.eglBaseContext, null)
//                    init(eglBase.eglBaseContext, null)
                }
                mediaStrem?.videoTracks?.get(0)?.addSink(localRenderer)
            }
        }
    }

    fun addStream(stream: MediaStream, position: Int){
        /*list.forEach { data ->
            if (stream.id == data.onlineUser.id){
                data.apply {
                    mediaStream = stream
                }

            }
        }*/

        list[position].mediaStream = stream
        notifyDataSetChanged()
    }

    fun findMe(userID : String) : Int{
        list.find{ it ->
            it.onlineUser.id == userID
        } .apply {
            Timber.e("findMe $userID is at pos : ${list.indexOf(this)}")
            return list.indexOf(this)
        }
        Timber.e("findMe $userID is at pos : -1")
        return -1
    }


}