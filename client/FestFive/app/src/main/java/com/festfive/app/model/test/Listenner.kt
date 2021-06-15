package com.festfive.app.model.test

import android.util.Log
import android.view.View
import android.view.View.OnClickListener

class Listenner (val firstName: String, val lastName: String, val visible: Boolean = true) : View.OnClickListener{
    init {
        val clickListener= OnClickListener {}
    }

    override fun onClick(p0: View?) {
        Log.d("test","onClick 2")
    }


}