package com.festfive.app.extension

import android.content.Context
import com.festfive.app.R
import com.festfive.app.utils.Constants
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Nhat Vo on 19/11/2020.
 */

fun String?.timeFormat(dateTimeFormat: String?, pattern: String = Constants.PATTERN_PARSE_TIME_SERVER): String {
    kotlin.runCatching {
        var timeFormat = dateTimeFormat
        if (timeFormat.isNullOrEmpty()) {
            timeFormat = "dd MMM yyyy"
        }
        val millis = this.convertStringToMillis(pattern, isApplyTimezone = true)
        return if (millis != 0.toLong()) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            val format = SimpleDateFormat(timeFormat, Locale.getDefault())
            format.format(cal.time)
        } else {
            "-"
        }
    }
    return "-"
}

fun String?.convertStringToMillis(
    format: String = Constants.PATTERN_PARSE_TIME_SERVER,
    isApplyTimezone: Boolean = true
): Long {
    if (this.isNullOrEmpty()) {
        return 0
    }
    val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
    if (isApplyTimezone) {
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT")
    }

    return try {
        simpleDateFormat.parse(this)?.time ?: 0
    } catch (ignored: ParseException) {
        0
    }
}

fun Long.getTimeAgo(context: Context): String {
    val h = this.div(60)
    val m = this.rem(60)

    Timber.e("$h - $m")

    return when {
        h > 1 -> context.getString(R.string.text_x_hours_ago, h)
        h == 1.toLong() -> context.getString(R.string.text_1_hour_ago)
        m > 1 -> context.getString(R.string.text_x_hours_ago, h)
        m == 1.toLong() -> context.getString(R.string.text_1_minute_ago)
        else -> context.getString(R.string.text_now)
    }
}
