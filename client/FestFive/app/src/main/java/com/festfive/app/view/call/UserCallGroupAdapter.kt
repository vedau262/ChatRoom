package com.festfive.app.view.call

import com.festfive.app.R
import com.festfive.app.base.adapter.BaseBindingAdapter
import com.festfive.app.databinding.ItemCallBinding
import com.festfive.app.databinding.ItemUserBinding
import com.festfive.app.model.DataStream
import com.festfive.app.model.OnClickUser
import com.festfive.app.model.OnlineUser
import org.webrtc.EglBase
import org.webrtc.MediaStream

class UserCallGroupAdapter (private val onlineUser : (OnlineUser) -> Unit) :
    BaseBindingAdapter<ItemCallBinding, DataStream>(){
    private val eglBase by lazy { EglBase.create() }

    override fun getLayoutId(viewType: Int) = R.layout.item_call

    override fun bindViewHolder(binding: ItemCallBinding, position: Int) {
        val data = list[position]
        binding.apply {
            user = data.onlineUser
            mediaStrem = data.mediaStream
            onlineUser.invoke(data.onlineUser)

            localRenderer.apply {
                setEnableHardwareScaler(false)
                init(eglBase.eglBaseContext, null)
            }

//            if(mediaStrem!=null){
            mediaStrem?.videoTracks?.get(0)?.addSink(localRenderer)
//            }
        }
    }

    fun addStream(stream: MediaStream){
        list.forEach { data ->
            if (stream.id == data.onlineUser.id){
                data.apply {
                    mediaStream = stream
                }

            }
        }
        notifyDataSetChanged()
    }
}