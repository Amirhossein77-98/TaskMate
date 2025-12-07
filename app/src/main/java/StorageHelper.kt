package com.amirsteinbeck.taskmate

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

object StorageHelper {

    private const val PREF_NAME = "focusmate_prefs"
    private const val KEY_TASKS = "tasks_list"

    fun saveTasks(context: Context, tasks: List<Task>) {
        val gson = Gson()
        val json = gson.toJson(tasks)

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_TASKS, json) }
    }

    fun loadTasks(context: Context): MutableList<Task> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TASKS, null) ?: return mutableListOf()

        val type = object : TypeToken<MutableList<Task>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun clearTasks(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_TASKS).apply()
    }
}