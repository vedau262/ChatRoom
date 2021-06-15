package com.festfive.app.base.viewmodel

import androidx.lifecycle.LiveData
import com.festfive.app.data.network.NetworkState

/**
 * Created by Nhat Vo on 18/11/2020.
 */
interface IBaseLoadMoreViewModel<T> : IBaseViewModel {
    val totalCountObs: LiveData<Int>
    val stateObs: LiveData<NetworkState>
    val isNewList: LiveData<Boolean>
    val listObs: LiveData<MutableList<T>>
    fun loadData(isRefreshed: Boolean = false, startKey: String? = null, isLoggedIn: Boolean = true)
    fun onReloadList()
    fun lastItem(): String
    fun isFirstPage(): Boolean
}