package com.festfive.app.data

import com.festfive.app.model.test.Listenner
import com.festfive.app.model.test.User
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Nhat Vo on 07/12/2020.
 */
interface RetrofitAPIs {

    @POST("user/login")
    fun login(@Body body: JsonObject): Single<Listenner>

    @Streaming
    @GET("")
    fun downloadFile(@Url fileUrl: String): Call<ResponseBody>

    @POST("")
    fun uploadFile( @Url url: String, @Body images: MultipartBody): Completable

    @GET("HelloWorld/master/test")
    fun getListUsers(): Single<User>
}