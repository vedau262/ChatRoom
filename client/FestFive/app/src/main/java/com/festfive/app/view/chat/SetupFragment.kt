package com.festfive.app.view.chat

import android.content.Intent
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
import com.festfive.app.view.call.GroupCallActivity
import com.festfive.app.view.call.VideoCallActivity
import com.festfive.app.viewmodel.chat.SetupViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup.*
import org.json.JSONObject
import timber.log.Timber


class SetupFragment : BaseFragment<FragmentSetupBinding, SetupViewModel>() {
    private var myRoomId = ""
    private val userListAdapter : UserListAdapter by lazy {
        UserListAdapter{onClickUser ->
            Timber.e("onClickUser ${onClickUser.toString()}")
            if(onClickUser.isCall){
                gotoVideoCall(VideoCall(from = MyApp.onlineUser.id, to=onClickUser.user.id.getDefault()))
            } else {
                val roomID = onClickUser.user.id.getDefault().createRoomId( MyApp.onlineUser.id.getDefault())
                gotoChat(roomID)
            }
        }
    }

    override fun getLayoutRes(): Int  = R.layout.fragment_setup

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
                if(!it.isNullOrEmpty()){
                    dataBinding.enableGroup = true
                    dataBinding.btnSetUp.text = getText(R.string.btn_update)
                }
            })

            videoCall.observe(viewLifecycleOwner, Observer {
                //get call from friend
                it?.let {
                    gotoVideoCall(it)
                }
            })

            if(!mViewModel.onlineUser.name.getDefault().isNullOrEmpty()){
                dataBinding.txtUserId.setText(mViewModel.onlineUser.name.getDefault())
                dataBinding.userName = mViewModel.onlineUser.name.getDefault()
                setupChat()
            }
        }
    }

    fun setupChat() {
        val room = dataBinding.txtRoomId.text.toString()
        myRoomId = room
        val user = dataBinding.txtUserId.text.toString()

        if(room.isNotEmpty() && user.isNotEmpty()){
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

    fun gotoVideoCall(videoCall: VideoCall) {
        val intent = Intent(requireContext(), VideoCallActivity::class.java)
        intent.putExtra(Constants.KEY_PUT_OBJECT,
            Gson().toJson(videoCall)
        )
        requireContext().startActivity(intent)
    }

    fun gotoGroupVideoCall() {
        val intent = Intent(requireContext(), GroupCallActivity::class.java)
        intent.putExtra(Constants.KEY_PUT_OBJECT, myRoomId)
        requireContext().startActivity(intent)
    }
}