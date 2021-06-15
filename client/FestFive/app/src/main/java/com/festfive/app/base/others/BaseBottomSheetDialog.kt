package com.festfive.app.base.others

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.festfive.app.R
import com.festfive.app.data.preference.ConfigurationPrefs
import com.festfive.app.data.preference.IConfigurationPrefs

/**
 * Created by Nhat.vo on 8/5/20.
 */

abstract class BaseBottomSheetDialog<V : ViewDataBinding>(context: Context) :
    BottomSheetDialog(context, R.style.BottomSheetDialog), IBaseDialogView {

    protected lateinit var dataBinding: V

    protected abstract fun getLayoutId(): Int
    protected abstract fun init()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        dataBinding = DataBindingUtil.inflate(LayoutInflater.from(context), getLayoutId(), null, false)
        setContentView(dataBinding.root)
        init()
    }

    override val viewContext: Context
        get() = context

    override val configPrefs: IConfigurationPrefs
        get() = ConfigurationPrefs(viewContext)

    override fun onClose() {
        dismiss()
    }
}