package com.festfive.app.extension

import android.graphics.Color
import org.jsoup.Jsoup
import java.util.regex.Pattern

/**
 * Created by Nhat.vo on 16/11/2020.
 */
fun String?.isValidateHex(): Boolean {
    if (this != null) {
        val pattern = Pattern.compile("^([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
        val matcher = pattern.matcher(this)
        return matcher.matches()
    }
    return false
}

fun String?.parseStringColor(): Int {
    val code = this?.replace("#", "")
    if (code != null && code.isValidateHex()) {
        code
    } else {
        "FFFFFF"
    }.let {
        return Color.parseColor("#$it")
    }
}

fun String?.parseStringColorWithPercent(percent: Int): Int {
    val code = this?.replace("#", "")
    if (code != null && code.isValidateHex()) {
        code
    } else {
        "FFFFFF"
    }.let {
        kotlin.runCatching {
            val initColor = Color.parseColor("#$code")
            val r = Color.red(initColor)
            val g = Color.green(initColor)
            val b = Color.blue(initColor)
            val alpha = 255.0.coerceAtMost(((percent.toFloat() / 100) * 255.0))
            return Color.argb(alpha.toInt(), r, g, b)
        }
        return Color.WHITE
    }
}

fun String?.html2text(): String {
    return Jsoup.parse(this.getDefault()).text()
}

fun toString(value: Int) = value.toString()