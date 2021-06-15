package com.festfive.app.base.view

import androidx.navigation.NavController
import com.festfive.app.base.viewmodel.IBaseViewModel

/**
 * Created by Nhat Vo on 17/11/2020.
 */
interface IBaseFragment<V : IBaseViewModel> : IBaseView<V> {
    val navController: NavController
    fun getParentActivity(): IBaseActivity<*>
    fun getParentViewModel(): IBaseViewModel
}