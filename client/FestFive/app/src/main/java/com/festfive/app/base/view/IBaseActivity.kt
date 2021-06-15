package com.festfive.app.base.view

import android.content.Intent
import com.festfive.app.base.viewmodel.IBaseViewModel

/**
 * Created by Nhat Vo on 17/11/2020.
 */
interface IBaseActivity<T: IBaseViewModel> : IBaseView<T> {
    fun onChangeStatusBarColor()
    fun onCheckNewIntent(intent: Intent?)
    fun onAuthenticationHandler()
    fun getNavId(): Int?
    fun onShowErrorDialog(message: String)
}