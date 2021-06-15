package com.festfive.app.customize.dialog

import com.festfive.app.base.others.IBaseDialogView

/**
 * Created by Nhat Vo on 25/11/2020.
 */
interface IErrorDialogView: IBaseDialogView {
    fun onErrorConfirmed()
    override fun getDisplayTitle(): String = ""

}