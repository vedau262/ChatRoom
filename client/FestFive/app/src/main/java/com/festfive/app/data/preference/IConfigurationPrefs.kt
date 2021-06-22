package com.festfive.app.data.preference

import com.festfive.app.model.OnlineUser

/**
 * Created by Nhat.vo on 4/16/2020.
 */

interface IConfigurationPrefs {
    var token: String?
    var language: String
    var userInfo: OnlineUser
    fun clear()
}