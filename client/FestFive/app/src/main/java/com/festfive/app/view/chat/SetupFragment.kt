package com.festfive.app.view.chat

import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.base.view.BaseFragment
import com.festfive.app.databinding.FragmentSetupBinding
import com.festfive.app.extension.createRoomId
import com.festfive.app.extension.getDefault
import com.festfive.app.extension.initLinear
import com.festfive.app.model.VideoCall
import com.festfive.app.utils.Constants
import com.festfive.app.viewmodel.chat.SetupViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup.*
import org.json.JSONObject
import timber.log.Timber


class SetupFragment : BaseFragment<FragmentSetupBinding, SetupViewModel>() {

    private val userListAdapter : UserListAdapter by lazy {
        UserListAdapter{onClickUser ->
            Timber.e("onClickUser ${onClickUser.toString()}")
            if(onClickUser.isCall){
                gotoGroupVideoCall(VideoCall(to=onClickUser.user.id.getDefault()))
            } else {
                val roomID = onClickUser.user.id.getDefault().createRoomId( MyApp.onlineUser.id.getDefault())
                gotoChat(roomID)
            }
        }
    }
    override fun getLayoutRes(): Int  = R.layout.fragment_setup
    private var friendID = ""

    override fun initView() {
        super.initView()
        Timber.e("initView")

        dataBinding.apply {
            this.iView = this@SetupFragment
            rc_user.apply {
                adapter = userListAdapter
                initLinear(RecyclerView.VERTICAL)
            }


            txtUserId.addTextChangedListener { text ->
                dataBinding.userName = text.toString()
            }
        }

    }

    override fun initViewModel() {
        super.initViewModel()
        mViewModel.apply {
            getUsers().observe(viewLifecycleOwner, Observer {
                userListAdapter.updateData(it)
            })

            videoCall.observe(viewLifecycleOwner, Observer {
                //get call from friend
                it?.let {
                    gotoGroupVideoCall(it)
                }

            })
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.e("MyApp.onlineUser.id:  ${MyApp.onlineUser.id}")
        dataBinding.setupDone = !MyApp.onlineUser.id.isNullOrEmpty()
    }

    fun setupChat() {
        val room = dataBinding.txtRoomId.text.toString()
        val user = dataBinding.txtUserId.text.toString()

        if(room.isNotEmpty() && user.isNotEmpty()){
            dataBinding.setupDone = true
            mViewModel.setupChat(dataBinding.txtRoomId.text.toString(), dataBinding.txtUserId.text.toString())
        }
    }

    fun gotoChat() {
        gotoChat(dataBinding.txtRoomId.text.toString())
    }

    private fun gotoChat(roomID : String) {
        MyApp.mSocket.emitData(Constants.KEY_JOIN_CHAT_ROOM, roomID)
        val message = JSONObject()
        message.put("room", roomID)
        message.put("name", MyApp.onlineUser.name)
        MyApp.mSocket.emitData(Constants.KEY_UPDATE_USER_INFO, message)
        MyApp.updateUser( MyApp.onlineUser.apply {
            room = roomID
        })
        navController.navigate(R.id.action_setupFragment_to_chatFragment, Bundle().apply {
            putString(Constants.KEY_PUT_OBJECT, roomID)
        })
    }

    fun gotoGroupVideoCall(videoCall: VideoCall) {
        navController.navigate(R.id.action_setupFragment_to_videoCallFragment,
            Bundle().apply {
                putString(
                    Constants.KEY_PUT_OBJECT,
                    Gson().toJson(videoCall)
            )
        })
}
    fun gotoGroupVideoCall() {
        navController.navigate(R.id.action_setupFragment_to_groupVideoCallFragment,
        Bundle().apply {
            putString(
                Constants.KEY_PUT_OBJECT,
                friendID)
        })
    }
}