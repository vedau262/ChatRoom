package com.festfive.app.model

import io.realm.RealmObject

open class Frog (
    var name: String = "",
    var age : Int = 0,
    var species : String? = null,
    var owner : String? = null
) : RealmObject()