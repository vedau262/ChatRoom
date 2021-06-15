package com.festfive.app.customize.dialog

import android.content.Context
import com.festfive.app.R
import com.festfive.app.base.others.BaseDialog
import com.festfive.app.databinding.DialogErrorLayoutBinding

/*
*Created by Nhat.vo on 10/4/2019.
*/

class ErrorDialog(
    context: Context,
    private var message: String?,
    private val textConfirm: String? = context.getString(R.string.btn_ok),
    private val onConfirmed: (() -> Unit)? = null
) : BaseDialog<DialogErrorLayoutBinding>(context), IErrorDialogView {

    override fun getLayoutId(): Int = R.layout.dialog_error_layout

    override fun init() {
        dataBinding.apply {
            messageText = message
            buttonText = textConfirm
            listener = this@ErrorDialog
        }
    }

    override fun onErrorConfirmed() {
        dismiss()
        onConfirmed?.invoke()
    }

    fun setMessage(message: String) {
        this.message = message
    }
}