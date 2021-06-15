package com.festfive.app.extension

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.festfive.app.R
import com.festfive.app.customize.listener.DividerWithoutLastItemDecorator
import com.festfive.app.customize.management.CenterLayoutManager
import com.festfive.app.customize.management.GridSpacingItemDecoration

fun RecyclerView.initFleBox(justifyContent: Int = JustifyContent.FLEX_START) {
   FlexboxLayoutManager(context).apply {
        this.flexDirection = FlexDirection.ROW
        this.justifyContent = justifyContent
        this.flexWrap = FlexWrap.WRAP
        layoutManager = this
    }
}

fun RecyclerView.initLinear(type: Int = RecyclerView.HORIZONTAL) {
    this.layoutManager = LinearLayoutManager(this.context, type, false)
}

fun RecyclerView.initGrid(size: Int) {
    this.layoutManager = GridLayoutManager(this.context, size)
}

fun RecyclerView.initSnapHelper() {
    val snapHelper = LinearSnapHelper()
    onFlingListener = null
    snapHelper.attachToRecyclerView(this)
}

fun RecyclerView.initCenterScroll() {
    this.layoutManager = CenterLayoutManager(this.context, RecyclerView.HORIZONTAL, false)
}

fun RecyclerView.onLoadMore(onLoadMore: () -> Unit) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.canScrollVertically(1)) {
                onLoadMore.invoke()
            }
        }
    })
}

fun RecyclerView.addDivider(padding: Int = 0) {
    val dividerDrawable = ContextCompat.getDrawable(context, R.drawable.divider)
    dividerDrawable?.let {
        this.addItemDecoration(
            DividerWithoutLastItemDecorator(it, padding.convertDpToPx())
        )
    }
}

fun RecyclerView.addItemSpacing(space: Int) {
    this.addItemDecoration(GridSpacingItemDecoration(1, space.dp, true))
}