package com.festfive.app.base.viewmodel

import androidx.lifecycle.*
import com.festfive.app.extension.observeOnUiThread
import com.festfive.app.model.*
import com.festfive.app.model.error.ErrorMessage
import com.festfive.app.utils.event.RxEvent
import com.festfive.app.utils.event.SystemEvent
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by Nhat.vo on 16/11/2020.
 */

abstract class BaseViewModel :ViewModel(),  IBaseViewModel,
    LifecycleObserver {

    private var compositeDisposable = CompositeDisposable()

    private var currentDisposable: Disposable? = null

    private val _isLoadingObs = MutableLiveData<Boolean>()
    override val isLoadingObs: LiveData<Boolean>
        get() = _isLoadingObs

    private val _errorObs = MutableLiveData<ErrorMessage>()
    override val errorObs: LiveData<ErrorMessage>
        get() = _errorObs

    protected var isUserVisible: Boolean = false

    override fun setLoading(boolean: Boolean) {
        _isLoadingObs.postValue(boolean)
    }

    override fun setErrorMessage(t: Throwable?, message: String?) {
        _errorObs.postValue(ErrorMessage(t, message))
    }

    override fun resetErrorMessage() {
        _errorObs.postValue(null)
    }

    override fun addDisposable(disposable: Disposable, isSaveDisposable: Boolean) {
        if (isSaveDisposable) {
            currentDisposable = disposable
        }
        if (compositeDisposable.isDisposed) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable.add(disposable)
    }

    open fun onRemoveCurrentDisposable() {
        currentDisposable?.apply {
            compositeDisposable.remove(this)
        }
    }

    override fun requestPermission(
        rxPermissions: RxPermissions,
        permissions: Array<String>,
        onSuccess: ((Boolean) -> Unit)?
    ) {
        rxPermissions.requestEachCombined(*permissions)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { granted ->
                onSuccess?.invoke(granted.granted)
            }?.apply {

            }
    }

    override fun checkPermission(
        rxPermissions: RxPermissions,
        permissions: Array<String>
    ): Boolean {
        permissions.forEach {
            if (!rxPermissions.isGranted(it)) {
                return false
            }
        }

        return true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    override fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    override fun onResume() {
        isUserVisible = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun onPause() {
        isUserVisible = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) compositeDisposable.dispose()
    }

    open fun onApiError(t: Throwable) {
        Timber.e(Gson().toJson(t))
        setLoading(false)
        setErrorMessage(t)
    }

    open fun onApiError(message: String?) {
        setLoading(false)
        setErrorMessage(message = message)
    }

    open fun onApiError(errorMessage: ErrorMessage) {
        setLoading(false)
        _errorObs.postValue(errorMessage)
    }

    fun onBindSocketReceivedListener() {
        addDisposable(
            RxEvent.listen(SystemEvent.SocketData::class.java)
            .observeOnUiThread()
            .subscribe {
                when (it.data) {
                    is MessageSocket -> onChatMessageChanged(it.data.list)
                    is UserSocket -> onUserJoinChanged(it.data.list)
                    is StreamSocket -> onStreamChanged(it.data)
                    is OnlineUser -> onMyIdChanged(it.data)
                }
            })
    }

    open fun onChatMessageChanged(data: MutableList<ChatMessage>) {
    }

    open fun onUserJoinChanged(data: MutableList<OnlineUser>) {
    }

    open fun onStreamChanged(data: StreamSocket) {
    }

    open fun onMyIdChanged(data: OnlineUser) {
    }
}