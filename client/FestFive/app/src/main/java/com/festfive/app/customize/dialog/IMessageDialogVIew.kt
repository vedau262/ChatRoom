package com.festfive.app.customize.dialog

import com.festfive.app.base.others.IBaseDialogView

/**
 * Created by Nhat Vo on 10/12/2020.
 */
interface IMessageDialogVIew: IBaseDialogView {
    fun onLeftConfirmAction()
    fun onRightConfirmAction()
    override fun getDisplayTitle(): String = ""
}