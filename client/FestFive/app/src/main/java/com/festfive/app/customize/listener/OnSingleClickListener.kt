package com.festfive.app.customize.listener

import android.view.View
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Nhat Vo on 20/11/2020.
 */
class OnSingleClickListener(
    private val clickListener: View.OnClickListener,
    private val intervalMs: Long = 1000
) : View.OnClickListener {
    private var canClick = AtomicBoolean(true)
    override fun onClick(v: View?) {
        if (canClick.getAndSet(false)) {
            v?.run {
                postDelayed({
                    canClick.set(true)
                }, intervalMs)
                clickListener.onClick(v)
            }
        }
    }
}