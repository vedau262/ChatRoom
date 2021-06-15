package com.festfive.app.extension

import androidx.viewpager.widget.ViewPager

/**
 * Created by Nhat Vo on 19/11/2020.
 */

fun ViewPager.onPageChange(onChanged: (Int) -> Unit) {
    this.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            onChanged.invoke(position)
        }
    })
}