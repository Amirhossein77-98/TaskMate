package com.amirsteinbeck.focusmate

import android.app.Activity
import android.content.Intent

object NavigationHelper {
    fun goToGreet(activity: Activity, name: String) {
        val intent = Intent(activity, GreetActivity::class.java)
        intent.putExtra("USERNAME", name)
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