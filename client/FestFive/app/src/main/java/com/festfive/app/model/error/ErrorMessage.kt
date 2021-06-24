package com.festfive.app.model.error

import com.festfive.app.data.network.NoConnectionException
import org.json.JSONObject
import retrofit2.HttpException
import java.net.UnknownHostException

class ErrorMessage(
    private val throwable: Throwable? = null,
    var message: String? = null,
    var error: CommonError = CommonError.UNKNOWN
) {

    init {
        throwable?.let {
            when (throwable) {
                is NoConnectionException -> {
                    message = it.message
                    setError(NETWORK_ERROR)
                }
                else -> onApiFailure(throwable)
            }
        }
    }

    private fun onApiFailure(error: Throwable) {
        if (error is UnknownError || error is UnknownHostException) {
            setError(NETWORK_ERROR)
        } else if (error is HttpException) {
            setError(error.code())
            try {
                val jObjError = JSONObject(error.response()?.errorBody()!!.string())
                message = when {
                    jObjError.has("message") -> jObjError.getString("message")
                    jObjError.has("errors") -> jObjError.getString("errors")
                    else -> "Something went wrong, please check and try again!"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            setError(SERVER_ERROR)
        }
    }

    private fun setError(code: Int) {
        when (code) {
            NETWORK_ERROR -> error = CommonError.NETWORK_ERROR
            TOKEN_EXPIRED, TOKEN_DENIED -> error = CommonError.UNAUTHENTICATED
            BAD_REQUEST -> error = CommonError.BAD_REQUEST
            SERVER_ERROR -> error = CommonError.SERVER_ERROR
            RESULT_ERROR -> error = CommonError.RESULT_ERROR
        }
    }

    companion object {
        const val TOKEN_EXPIRED = 401
        const val TOKEN_DENIED = 403
        const val BAD_REQUEST = 400
        const val SERVER_ERROR = 500
        const val RESULT_ERROR = 404
        const val NETWORK_ERROR = 1
    }
}