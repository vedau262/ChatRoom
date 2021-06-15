package com.festfive.app.data.preference

/**
 * Created by Nhat.vo on 4/16/2020.
 */

interface IConfigurationPrefs {
    var token: String?
    var language: String
    fun clear()
}