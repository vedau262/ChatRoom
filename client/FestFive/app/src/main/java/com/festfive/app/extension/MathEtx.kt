package com.festfive.app.extension

/**
 * Created by Nhat Vo on 28/12/2020.
 */

fun <T> Boolean.triadOperator(firstElement: T, secondElement: T): T {
    if (this) {
        return firstElement
    }
    return secondElement
}