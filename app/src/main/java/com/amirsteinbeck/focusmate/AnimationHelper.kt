package com.amirsteinbeck.focusmate

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils

object AnimationHelper {
    @SuppressLint("ClickableViewAccessibility")
    fun applyPressAnimation(context: Context, view: View) {
        val down = AnimationUtils.loadAnimation(context, R.anim.scale_down)
        val up = AnimationUtils.loadAnimation(context, R.anim.scale_up)

        view.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> v.startAnimation(down)
                android.view.MotionEvent.ACTION_UP -> v.startAnimation(up)
            }
            false
        }
    }
}