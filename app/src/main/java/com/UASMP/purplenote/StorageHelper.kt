package com.UASMP.purplenote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StorageHelper {
    private const val PREF_NAME = "ActivityPrefs"
    private const val KEY_ACTIVITIES = "activity_list"

    fun saveActivityList(context: Context, list: List<ActivityModel>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(list)
        prefs.edit().putString(KEY_ACTIVITIES, json).apply()
    }

    fun loadActivityList(context: Context): MutableList<ActivityModel> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ACTIVITIES, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<ActivityModel>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}
