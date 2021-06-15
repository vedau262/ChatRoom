package com.festfive.app.repository

import com.festfive.app.data.RetrofitAPIs
import com.festfive.app.model.test.Listenner
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val service: RetrofitAPIs
){
    fun login(email: String, password: String): Single<Listenner> {
        return service.login(JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
        })
    }
}