package com.amirsteinbeck.taskmate.com.amirsteinbeck.focusmate

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREFS = "focusmate_settings"
    private const val LANGUAGE = "language"

    fun setLanguage(context: Context, langCode: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(LANGUAGE, langCode)
            .apply()
    }

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(LANGUAGE, "en") ?: "en"

    }

    fun applyLanguage(context: Context): Context {
        val lang = getLanguage(context)
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}