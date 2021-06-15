package com.festfive.app.base.others

import android.content.Context
import com.festfive.app.data.preference.IConfigurationPrefs

/**
 * Created by Nhat Vo on 25/11/2020.
 */
interface IBaseDialogView {
    val viewContext: Context
    val configPrefs: IConfigurationPrefs
    fun getDisplayTitle(): String
    fun onClose()
}