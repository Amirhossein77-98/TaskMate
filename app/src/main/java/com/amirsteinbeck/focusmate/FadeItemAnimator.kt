package com.amirsteinbeck.focusmate.com.amirsteinbeck.focusmate

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class FadeItemAnimator: DefaultItemAnimator() {
    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        holder?.itemView?.alpha = 0f
        val animator = holder?.itemView?.animate()
        animator?.alpha(1f)?.setDuration(200)?.setListener(null)
        return true
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
        val animator = holder?.itemView?.animate()
        animator?.alpha(0f)?.setDuration(200)?.withEndAction { dispatchRemoveFinished(holder) }
        return true
    }
}