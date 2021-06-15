package com.festfive.app.base.others

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleableRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import com.festfive.app.R
import com.festfive.app.base.view.IBaseView
import com.festfive.app.customize.listener.OnSingleClickListener
import com.festfive.app.customize.view.ShapeRoundView
import com.festfive.app.data.preference.ConfigurationPrefs
import com.festfive.app.data.preference.IConfigurationPrefs
import com.festfive.app.extension.*
import com.festfive.app.utils.Utils

/**
 * Created by Nhat.vo on 18/11/2019.
 */

abstract class BaseCustomView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    var listener: ((Any) -> Unit)? = null
    protected var prefs: IConfigurationPrefs = ConfigurationPrefs(context)

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.initViewDataBinging(inflater)
        onSetupView(attrs)
    }

    private fun onSetupView(attrs: AttributeSet?) {
        onCreatedView()
        try {
            if (attrs != null && getStyleable() != null) {
                val styleable = context.obtainStyledAttributes(attrs, getStyleable()!!, 0, 0)
                applyStyleable(styleable)
                styleable.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    abstract fun initViewDataBinging(inflater: LayoutInflater): ViewDataBinding

    @StyleableRes
    open fun getStyleable(): IntArray? = null

    open fun onCreatedView() {}

    @SuppressLint("Recycle")
    open fun applyStyleable(styleable: TypedArray) {
    }

    open fun onSpecialClicked(listener: ((Any) -> Unit)?) {
        this.listener = listener
    }

    companion object {
        @BindingAdapter("app:bindImage")
        @JvmStatic
        fun bindImage(imageView: ImageView, url: String?) {
            imageView.bindImage(url)
        }

        @BindingAdapter("app:bindAvatar")
        @JvmStatic
        fun bindAvatar(imageView: ImageView, url: String?) {
            imageView.bindImage(url/*, R.drawable.ic_user*/)
        }

        @BindingAdapter("app:bindImage")
        @JvmStatic
        fun bindImage(imageView: ImageView, @DrawableRes drawable: Int?) {
            if (drawable != null) {
                imageView.apply {
                    visible()
                    setImageDrawable(ContextCompat.getDrawable(context, drawable))
                }
            } else {
                imageView.gone()
            }
        }

        @BindingAdapter("onSingleClick")
        @JvmStatic
        fun View.setOnSingleClickListener(clickListener: OnClickListener?) {
            clickListener?.also {
                setOnClickListener(OnSingleClickListener(it))
            } ?: setOnClickListener(null)
        }

        @BindingAdapter("onSelected")
        @JvmStatic
        fun View.onSelected(isSelected: Boolean?) {
            setSelected(isSelected.getDefault())
        }

        @BindingAdapter("onVisible")
        @JvmStatic
        fun View.onVisible(isVisible: Boolean?) {
            visibility = if (isVisible.getDefault()) View.VISIBLE else View.GONE
        }

        @BindingAdapter("onHide")
        @JvmStatic
        fun View.onHide(isHide: Boolean?) {
            visibility = if (isHide.getDefault()) View.INVISIBLE else View.VISIBLE
        }

        @BindingAdapter("app:setAfterTextChanged")
        @JvmStatic
        fun AppCompatEditText.setAfterTextChanged(iView: IBaseView<*>) {
            doAfterTextChanged {
                iView.onSearchCallback(it?.toString())
            }
        }

        @BindingAdapter("app:setDrawableTint")
        @JvmStatic
        fun AppCompatTextView.setDrawableTint(color: Int) {
            compoundDrawablesRelative.forEach { drawable ->
                drawable?.colorFilter =
                        PorterDuffColorFilter(
                                color,
                                PorterDuff.Mode.SRC_IN
                        )
            }
        }

        @BindingAdapter("app:borderColor")
        @JvmStatic
        fun ShapeRoundView.borderColor(color: Int) {
            borderColor = color
            requiresShapeUpdate()
        }

        @BindingAdapter("app:backgroundColor")
        @JvmStatic
        fun ShapeRoundView.backgroundColor(color: Int) {
            setCardBackgroundColor(color)
            requiresShapeUpdate()
        }

        @BindingAdapter("app:setViewDisable")
        @JvmStatic
        fun View.setViewDisable(isDisable: Boolean) {
            isEnabled = !isDisable
        }

        @BindingAdapter("app:changeTypeFace")
        @JvmStatic
        fun AppCompatTextView.changeTypeFace(style: Int) {
            setTypeface(Utils.getDefaultTypeFace(context), style)
        }
    }
}