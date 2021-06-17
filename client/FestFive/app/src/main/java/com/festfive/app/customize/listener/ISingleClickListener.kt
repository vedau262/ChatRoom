package com.festfive.app.customize.listener

interface ISingleClickListener<T> {
    fun onSingleClicked(data: T)
}

interface IClickUser<T, Boolean> {
    fun onClickUser(data: T, isCall: Boolean)
}