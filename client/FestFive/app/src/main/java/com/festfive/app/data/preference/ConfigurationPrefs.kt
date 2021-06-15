package com.festfive.app.data.preference

import android.content.Context
import com.festfive.app.BuildConfig
import com.festfive.app.extension.getDefault
import com.festfive.app.utils.Utils
import com.google.gson.Gson
import javax.inject.Inject

/*
*Created by Nhat.vo on 8/22/2019.
*/
class ConfigurationPrefs @Inject constructor(
    private val context: Context
) : IConfigurationPrefs {

    companion object {
        private const val KEY_API_TOKEN = "KEY_API_TOKEN"
        private const val KEY_DEFAULT_LANGUAGE = "KEY_DEFAULT_LANGUAGE"
        private val defaultLanguage = Utils.languageDefault()
    }

    private val pref by lazy {
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
    }

    /**
     * Token
     */
    override var token: String?
        get() = pref.getString(KEY_API_TOKEN, "")
        set(token) = pref.edit().putString(KEY_API_TOKEN, Gson().toJson(token)).apply()

    override var language: String
        get() = pref.getString(KEY_DEFAULT_LANGUAGE, "").getDefault()
        set(language) = pref.edit().putString(KEY_DEFAULT_LANGUAGE, language).apply()

    /**
     * Clear data
     */
    override fun clear() {
        pref.edit().clear().apply()
    }
}
