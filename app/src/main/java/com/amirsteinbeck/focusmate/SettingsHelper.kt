package com.amirsteinbeck.focusmate.com.amirsteinbeck.focusmate

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object SettingsHelper {

    private const val PREFS = "focusmate_settings"
    private const val DARK_MODE = "dark_mode"
    private const val AUTO_SORT = "auto_sort"

    fun setDarkMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(DARK_MODE, enabled).apply()

        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun isDarkMode(context: Context): Boolean = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(DARK_MODE, false)

    fun setAutoSort(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(AUTO_SORT, enabled).apply()
    }

    fun isAutoSort(context: Context): Boolean = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(AUTO_SORT, true)
}