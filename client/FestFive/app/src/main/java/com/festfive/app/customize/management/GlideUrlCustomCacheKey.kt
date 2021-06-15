package com.festfive.app.customize.manager

import com.bumptech.glide.load.model.GlideUrl

/**
 * Created by Nhat Vo on 18/12/2020.
 */
class GlideUrlCustomCacheKey(
    url: String
) : GlideUrl(url) {

    override fun getCacheKey(): String? {
        val url = toStringUrl()
        return if (url.contains("?")) {
            url.substring(0, url.lastIndexOf("?"))
        } else {
            url
        }
    }
}