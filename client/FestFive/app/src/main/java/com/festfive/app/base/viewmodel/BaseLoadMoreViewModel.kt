package com.festfive.app.base.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.festfive.app.data.network.NetworkState
import com.festfive.app.extension.observeOnUiThread
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Created by Nhat Vo on 24/08/2020.
 */
abstract class BaseLoadMoreViewModel<T> : BaseViewModel(), IBaseLoadMoreViewModel<T> {

    var mCurrentPage = 0
    protected var mPageCount = 0

    protected val _totalCountObs = MutableLiveData<Int>()
    override val totalCountObs: LiveData<Int>
        get() = _totalCountObs

    protected val _isNewList = MutableLiveData<Boolean>()
    override val isNewList: LiveData<Boolean>
        get() = _isNewList

    protected val _stateObs = MutableLiveData<NetworkState>()
    override val stateObs: LiveData<NetworkState>
        get() = _stateObs

    protected val _listObs = MutableLiveData<MutableList<T>>()
    override val listObs: LiveData<MutableList<T>>
        get() = _listObs

    private var currentError: Throwable? = null
    private val subject = PublishSubject.create<Int>()

    init {
        resetData()

        this.addDisposable(subject
            .debounce(2000, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .observeOnUiThread()
            .subscribe {
                _stateObs.postValue(NetworkState.LOADED)
            })
    }

    override fun loadData(isRefreshed: Boolean, startKey: String?, isLoggedIn: Boolean) {
        if (!isLoggedIn) {
            resetData()
            return
        }
        when {
            _stateObs.value == NetworkState.LOADING -> {
                return
            }
            isRefreshed -> mCurrentPage = 0
            mCurrentPage < mPageCount - 1 -> {
                mCurrentPage += if (_stateObs.value != NetworkState.BLOCK) 1 else 0
            }
            else -> {
                _isNewList.postValue(false)
                return
            }
        }
        if (_stateObs.value != NetworkState.BLOCK) {
            loadMore(startKey)
        } else {
            if (isRefreshed) {
                setLoading(false)
            }
            subject.onNext(Random.nextInt())
        }
    }

    open fun onPagingDataSuccess(data: MutableList<T>): MutableList<T> {
        currentError = null
        var list = _listObs.value ?: mutableListOf()
        if (mCurrentPage == 0) {
            list = data
        } else {
            list.addAll(data)

        }
        _listObs.postValue(list)
        return list
    }

    override fun onReloadList() {
        onRemoveCurrentDisposable()
        addDisposable(
            getTempObservable()
                .observeOnUiThread()
                .delaySubscription(500, TimeUnit.MILLISECONDS)
                .subscribe({
                    onReloadListCallBack()
                }, {})
            , true
        )
    }

    open fun onReloadListCallBack() {
        loadData(true)
    }

    private fun resetData() {
        _totalCountObs.postValue(0)
        _stateObs.postValue(NetworkState.LOADED)
        _listObs.postValue(arrayListOf())
        mCurrentPage = 0
        mPageCount = 0
    }

    fun onSetPagingLoading(isLoading: Boolean) {
        setLoading(isLoading && mCurrentPage == 0)
        when {
            isLoading -> {
                _stateObs.postValue(NetworkState.LOADING)
                return
            }
            currentError != null -> {
                _stateObs.value = NetworkState.BLOCK
            }
            else -> {
                _stateObs.postValue(NetworkState.LOADED)
            }
        }
    }

    override fun onApiError(t: Throwable) {
        if (currentError == null || mCurrentPage == 0) {
            currentError = t
            super.onApiError(t)
        }
        if (mCurrentPage > 0) {
            mCurrentPage -= 1
        }
    }

    abstract fun loadMore(startKey: String?)
    override fun isFirstPage(): Boolean = mCurrentPage == 0

    companion object {
        const val DEFAULT_LIMIT = 10
    }
}