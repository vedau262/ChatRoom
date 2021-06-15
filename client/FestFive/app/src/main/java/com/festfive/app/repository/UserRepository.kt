package com.festfive.app.repository

import com.festfive.app.data.RetrofitAPIs
import com.festfive.app.model.test.User
import io.reactivex.Observable
import io.reactivex.rxjava3.core.Single

import javax.inject.Inject

class UserRepository @Inject constructor(private val service: RetrofitAPIs){

    fun getListUsers() : Single<User> {
        return service.getListUsers()
    }
}