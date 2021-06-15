package com.festfive.app.base.viewmodel

import androidx.lifecycle.LiveData
import com.tbruyelle.rxpermissions2.RxPermissions
import com.festfive.app.model.error.ErrorMessage
import com.festfive.app.utils.event.SystemEvent
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Created by Nhat.vo on 16/11/2020.
 */
interface IBaseViewModel {
    val isLoadingObs: LiveData<Boolean>
    val errorObs: LiveData<ErrorMessage>

    fun setLoading(boolean: Boolean)
    fun setErrorMessage(t: Throwable? = null, message: String? = null)
    fun resetErrorMessage()

    fun getTempObservable(): Single<Any> {
        return Single.create {
            it.onSuccess(true)
        }
    }

    fun addDisposable(disposable: Disposable, isSaveDisposable: Boolean = false)
    fun requestPermission(
        rxPermissions: RxPermissions,
        permissions: Array<String>,
        onSuccess: ((Boolean) -> Unit)? = null
    )

    fun checkPermission(rxPermissions: RxPermissions, permissions: Array<String>): Boolean


    fun onCreate()
    fun onStart()
    fun onPause()
    fun onResume()
    fun onStop()
    fun onDestroy()
}