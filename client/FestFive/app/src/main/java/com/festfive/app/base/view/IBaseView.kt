package com.festfive.app.base.view

import android.app.Activity
import android.content.Context
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.festfive.app.base.viewmodel.IBaseViewModel
import com.festfive.app.data.preference.IConfigurationPrefs
import com.festfive.app.model.error.ErrorMessage

/**
 * Created by Nhat Vo on 17/11/2020.
 */
interface IBaseView<T: IBaseViewModel> {
    /**
     * Define the layout res id can be used to [Activity.setContentView]
     *
     * @return the layout res id
     */

    @LayoutRes
    fun getLayoutRes(): Int

    fun getViewModel(): T

    fun initView()
    fun initViewModel()
    fun showLoadingDialog()
    fun dismissLoadingDialog()
    fun handleError(errorMessage: ErrorMessage?)
    fun onHandleBackPressed()
    fun getToolbarTitle(): String?
    fun onSearchCallback(searchKey: String?)
    fun getCurrentFragment(id: Int): Fragment?
    fun getFragments(id: Int): MutableList<Fragment>?
    val configPrefs: IConfigurationPrefs
    val viewContext: Context
}