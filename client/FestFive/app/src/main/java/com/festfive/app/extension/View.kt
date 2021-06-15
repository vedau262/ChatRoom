package com.festfive.app.extension

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.festfive.app.R


fun View.visible(isShow: Boolean = true) {
    this.visibility = if (isShow) View.VISIBLE else View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.enable() {
    this.isEnabled = true
}

fun View.disable() {
    this.isEnabled = false
}

fun View.showKeyBoard(activity: Activity?) {
    activity?.let {
        this.requestFocus()
        val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}

fun View.hideKeyboard(activity: Activity?) {
    activity?.let {
        this.clearFocus()
        val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}


fun View.setOnDelayClickListener(method: (View) -> Unit) {
    this.setOnClickListener {
        this.isEnabled = false
        method.invoke(it)
        this.postDelayed({
            this.isEnabled = true
        }, 600)
    }
}

/**
 * Image View
 */

fun ImageView.bindImage(
    url: String?,
    @DrawableRes holder: Int = R.color.white
) {
    if (TextUtils.isEmpty(url)) {
        this.setImageResource(holder)
    } else {
        Glide.with(context)
            .load(url)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(ContextCompat.getDrawable(context, holder))
            )
            .into(this)

    }
}

fun AppCompatEditText.toString(): String {
    return this.text?.toString().getDefault()
}