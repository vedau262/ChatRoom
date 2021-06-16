package com.festfive.app.view.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.festfive.app.R
import com.festfive.app.base.view.BaseActivity
import com.festfive.app.base.viewmodel.EmptyViewModel
import com.festfive.app.databinding.ActivityChatBinding
import com.festfive.app.view.ChatAdapter

const val REQUEST_CODE_PERMISSION = 100
class SocketTestActivity : BaseActivity<ActivityChatBinding, EmptyViewModel>(){
    override fun getLayoutRes() : Int = R.layout.activity_chat

    override fun initView() {
        super.initView()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE_PERMISSION
            )
        }
    }
}