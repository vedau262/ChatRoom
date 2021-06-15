package com.festfive.app.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.view.inputmethod.InputMethodManager
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import com.festfive.app.R
import java.util.*

/**
 * Created by Nhat.vo on 16/11/2020.
 */
object Utils {

    fun languageDefault(): String =
        if (Locale.getDefault().language != "en" && Locale.getDefault().language != "ar") "en"
        else Locale.getDefault().language


    fun hideSoftKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = activity.window.decorView
        }
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun getDefaultTypeFace(
        context: Context,
        @FontRes id: Int = R.font.proxima_nova_regular
    ): Typeface? = ResourcesCompat.getFont(context, id)

    fun copyText(context: Context, value: String) {
        val clip = ClipData.newPlainText("Copied Text", value)
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
            clip
        )
    }
}