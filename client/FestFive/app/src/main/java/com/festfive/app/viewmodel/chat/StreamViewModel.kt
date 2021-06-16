package com.festfive.app.viewmodel.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.festfive.app.base.viewmodel.BaseViewModel
import com.festfive.app.model.StreamSocket
import com.festfive.app.push.SocketManager
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class StreamViewModel @Inject constructor (
): BaseViewModel() {

    private val _streamSocket = MutableLiveData<StreamSocket>()
    val streamSocket : LiveData<StreamSocket>
        get() {
            return _streamSocket
        }

    init {
        this.onBindSocketReceivedListener()
    }

    override fun onStreamChanged(data: StreamSocket) {
        super.onStreamChanged(data)
        Timber.e("onStreamChanged "+data)
        if(data!=null){
            _streamSocket.postValue(data)
        }

    }
}