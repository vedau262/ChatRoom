package com.festfive.app.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.festfive.app.R
import com.festfive.app.base.view.BaseActivity
import com.festfive.app.databinding.ActivityMainBinding
import com.festfive.app.model.test.Listenner
import com.festfive.app.model.test.User

class MainActivity : BaseActivity<ActivityMainBinding, TestViewModel>(), View.OnClickListener {
    override fun getLayoutRes(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding.apply {
            this.user = User("aaa", "xxx", "111")
            this.listenner = Listenner("le", "a", true)

        }
//        mViewModel.parseData()

//        mViewModel.mUser.observe(this, Observer { dataBinding.user = it })

        mViewModel._isSuccess.observeForever {
            Log.d("test", "$it")
        }
    }

    override fun onClick(p0: View?) {
        Log.d("test","onClick")
    }


}