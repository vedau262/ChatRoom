package com.festfive.app.extension

import com.google.gson.Gson

/**
 * Created by Nhat Vo on 9/18/20.
 */

fun <T : Any> Any.copyObject(inputClass: Class<T>): T {
    return Gson().fromJson<T>(Gson().toJson(this), inputClass)
}
