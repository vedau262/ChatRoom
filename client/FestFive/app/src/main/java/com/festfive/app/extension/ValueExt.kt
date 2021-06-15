package com.festfive.app.extension

import java.text.CharacterIterator
import java.text.NumberFormat
import java.text.StringCharacterIterator
import java.util.*
import kotlin.math.abs
import kotlin.math.pow


/**
 * Created by Nhat.vo on 8/16/20.
 */

fun String?.getDefault(): String {
    return this ?: ""
}

fun String?.getDefaultDisplayValue(): String {
    return if (this.isNullOrEmpty()) {
        "-"
    } else {
        this
    }
}

fun Int?.getDefault(): Int {
    return this ?: 0
}

fun Double?.getDefault(): Double {
    return this ?: 0.0
}


fun Long?.getDefault(): Long {
    return this ?: 0
}

fun Boolean?.getDefault(): Boolean {
    return this ?: false
}


fun Any?.formatPriceWithoutCurrency(): String {
    if (this != null) {
        kotlin.runCatching {
            val numberFormat = NumberFormat.getCurrencyInstance(Locale.US)
            numberFormat.maximumFractionDigits = 2
            when (this) {
                is Int -> this.toDouble()
                is Float -> this.toDouble()
                is Double -> this.toDouble()
                is String -> this.toDouble()
                else -> 0.0
            }.apply {
                val parse = withSuffix()
                val format = numberFormat.format(parse.first).replace("$", "").replace(".00", "")
                return "$format${parse.second}"
            }
        }
    }
    return "-"
}

fun Double.withSuffix(): Pair<Double, String> {
    val mill = 10.0.pow(6)
    val bill = 10.0.pow(9)

    return when {
        this / bill > 1 -> {
            Pair(this / bill, "B")
        }
        this / mill > 1 -> {
            Pair(this / mill, "M")
        }
        else -> {
            Pair(this, "")
        }
    }
}

fun Int.parseCount(): String {
    val k = 10.0.pow(3)

    return when {
        this / k > 1 -> {
            String.format("%.1fk", this / k)
        }
        else -> {
            "$this"
        }
    }
}


fun Long.formatFileSize(): String {
    val absB = if (this == Long.MIN_VALUE) Long.MAX_VALUE else abs(this)

    if (absB < 1024) {
        return "$this B"
    }
    var value = absB
    val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
    var i = 40
    while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
        value = value shr 10
        ci.next()
        i -= 10
    }
    value *= java.lang.Long.signum(this).toLong()
    return String.format("%.1f %cB", value / 1024.0, ci.current())
}

