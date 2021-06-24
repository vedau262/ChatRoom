package com.festfive.app.view

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.festfive.app.base.viewmodel.BaseViewModel
import com.festfive.app.data.log.IFrogLog
import com.festfive.app.extension.observeOnUiThread
import com.festfive.app.model.Frog
import com.festfive.app.model.test.Listenner
import com.festfive.app.model.test.User
import com.festfive.app.repository.LoginRepository
import com.festfive.app.repository.UserRepository
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject
import kotlin.random.Random

class TestViewModel @Inject constructor(
    private val realm: Context,
    private val loginRepository: LoginRepository,
    private val userRepository: UserRepository,
    private val dao: IFrogLog
) : BaseViewModel() {
    var mes : MutableLiveData<String> = MutableLiveData()
    var user : MutableLiveData<User> = MutableLiveData()
    val mUser : LiveData<User>
        get() = user
    val _isSuccess = MutableLiveData<Boolean>()

    init {
        addFrogs()
        getAllFrogs()
    }

    fun addFrogs(){
        addDisposable(dao.saveFrog(Frog(
            name = "frog",
            age = 10,
            species = "water",
            owner = "ower " + Random.nextInt(1 , 100)

        )).observeOnUiThread()
            .subscribe({
            Timber.e("addFrogs ok ok")
        }, {
            Timber.e("addFrogs error error")
            onApiError("error")
        })
        )
    }

    fun getAllFrogs(){
        addDisposable(dao.getAllFrog()
            .observeOnUiThread()
            .subscribe({
                Timber.e("all frog: ${Gson().toJson(it)}")
            }, {
                Timber.e("error: $it}")
            })
        )
    }

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




