package com.amirsteinbeck.focusmate

import android.app.Activity
import android.content.Intent

object NavigationHelper {
    fun goToTaskDetails(activity: Activity, title: String, description: String) {
        val intent = Intent(activity, TaskDetailsActivity::class.java)
        intent.putExtra("TASKTITLE", title)
        intent.putExtra("TASKDESCRIPTION", description)
        activity.startActivity(intent)
//        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun goToCredentials(activity: Activity) {
        val intent = Intent(activity, CredentialsActivity::class.java)
        activity.startActivity(intent)
//        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun goBack(activity: Activity) {
        activity.finish()
//        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}