package com.festfive.app.extension

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.festfive.app.application.MyApp

/**
 * Created by Nhat Vo on 01/04/2021.
 */

fun Int.getString(): String {
    return MyApp.instance.getString(this)
}

fun Int.getColor(): Int {
    return ContextCompat.getColor(MyApp.instance.applicationContext, this)
}

fun Int.getDrawable(): Drawable? {
    return ContextCompat.getDrawable(MyApp.instance.applicationContext, this)
}