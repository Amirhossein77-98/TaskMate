package com.amirsteinbeck.focusmate

import android.app.Activity
import android.content.Intent

object NavigationHelper {
    fun goToArchivedTasks(activity: Activity) {
        val intent = Intent(activity, ArchivedTasksActivity::class.java)
        activity.startActivity(intent)
//        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun goToCredentials(activity: Activity) {
        val intent = Intent(activity, CredentialsActivity::class.java)
        activity.startActivity(intent)
//        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun goBack(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
//        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}