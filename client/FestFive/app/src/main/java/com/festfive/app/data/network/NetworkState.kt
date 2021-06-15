package com.festfive.app.data.network

/*
*Created by Nhat Vo on 9/20/2019.
*/

enum class Status {
    RUNNING,
    SUCCESS,
    BLOCK,
    FAILED
}

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
    val status: Status,
    val msg: String? = null) {
    companion object {
        val LOADED = NetworkState(Status.SUCCESS)
        val LOADING = NetworkState(Status.RUNNING)
        val BLOCK = NetworkState(Status.BLOCK)
        fun error(msg: String?) = NetworkState(
            Status.FAILED,
            msg
        )
    }
}