package com.festfive.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.festfive.app.BuildConfig


/**
 * Created by Nhat.vo on 26,November,2020
 */

class SharePreferencesUtils {
    companion object {
        var context:Context? = null

        fun getString(key:String, default:String = "") : String {
            val preferences: SharedPreferences? = context?.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
            val value = preferences?.getString(key, default)?: default
            return value
        }

        fun setString(key:String, value:String) {
            val editor: SharedPreferences.Editor? = context?.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)?.edit()
            editor?.putString(key, value)
            editor?.apply()
        }

        fun getBoolean(key:String, default:Boolean = false) : Boolean {
            val preferences: SharedPreferences? = context?.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
            return preferences?.getBoolean(key, default)?: default
        }

        fun setBoolean(key:String, value:Boolean) {
            val editor: SharedPreferences.Editor? = context?.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean(key, value)
            editor?.apply()
        }

        fun existKey(key:String): Boolean {
            val preferences: SharedPreferences? = context?.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
            return preferences?.contains(key)?: false
        }

        fun removeKey(key:String) {
            if (existKey(key)) {
                val editor: SharedPreferences.Editor? = context?.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)?.edit()
                editor?.remove(key)
                editor?.apply()
            }
        }
    }
}