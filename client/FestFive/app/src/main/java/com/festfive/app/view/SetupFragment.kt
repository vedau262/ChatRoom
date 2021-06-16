package com.festfive.app.view

import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.festfive.app.R
import com.festfive.app.application.MyApp
import com.festfive.app.base.view.BaseFragment
import com.festfive.app.databinding.FragmentSetupBinding
import com.festfive.app.extension.getDefault
import com.festfive.app.extension.initLinear
import com.festfive.app.utils.Constants
import com.festfive.app.view.chat.UserListAdapter
import com.festfive.app.viewmodel.chat.SetupViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup.*
import timber.log.Timber


class SetupFragment : BaseFragment<FragmentSetupBinding, SetupViewModel>() {

    private val userListAdapter= UserListAdapter()
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

            txtRoomId.addTextChangedListener { text ->
                dataBinding.roomID = text.toString()
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
                if(it.size>1){
                    it.find { onlineUser ->  onlineUser.id!=MyApp.onlineUser.id}?.apply {
                        friendID = this.id.getDefault()
                    }
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
        navController.navigate(R.id.action_setupFragment_to_chatFragment)
    }

    fun gotoVideoCall() {
        navController.navigate(R.id.action_setupFragment_to_videoCallFragment,
        Bundle().apply {
            putString(
                Constants.KEY_PUT_OBJECT,
                friendID)
        })
    }
}