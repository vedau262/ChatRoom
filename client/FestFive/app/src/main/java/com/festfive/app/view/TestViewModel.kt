package com.festfive.app.view

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.festfive.app.base.viewmodel.BaseViewModel
import com.festfive.app.extension.observeOnUiThread
import com.festfive.app.model.test.Listenner
import com.festfive.app.model.test.User
import com.festfive.app.repository.LoginRepository
import com.festfive.app.repository.UserRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.lang.Exception
import javax.inject.Inject

class TestViewModel @Inject constructor(
    private val context: Context,
    private val loginRepository: LoginRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {
    var mes : MutableLiveData<String> = MutableLiveData()
    var user : MutableLiveData<User> = MutableLiveData()
    val mUser : LiveData<User>
        get() = user
    val _isSuccess = MutableLiveData<Boolean>()

    fun parseData(){
        addDisposable(userRepository.getListUsers()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { setLoading(true) }
            .doFinally { setLoading(false) }
            .subscribe(
                {
                    user.value = it
                    Log.d("test", it.toString())
                },{
                    OnFailure(it)}
            ))






        addDisposable(loginRepository.login("aa", "bb")
            .observeOnUiThread()
            .doOnSubscribe { setLoading(true) }
            .doFinally { setLoading(false) }
            .subscribe(
                {
                    Log.d("test", it.toString())
                },{
                    OnFailure(it)
                })
        )
    }


    private fun OnFailure(it: Throwable?) {
        if (it != null) {
            onApiError(it)
        }
    }
}




