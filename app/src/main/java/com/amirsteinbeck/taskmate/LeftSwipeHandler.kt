package com.amirsteinbeck.taskmate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class LeftSwipeHandler(
    private val context: Context,
    private val adapter: TaskAdapter,
    private var fullList: MutableList<Task>,
    private var displayList: MutableList<Task>,
    private val onSwipeCallback: () -> Unit // To update the empty view
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            .addSwipeLeftBackgroundColor(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorOnError,
                    Color.RED
                )
            )
            .addSwipeLeftActionIcon(R.drawable.trash_bin_minimalistic_2_svgrepo_com_24)
            .setSwipeLeftActionIconTint(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorSurfaceInverse,
                    Color.WHITE
                )
            )
            .addSwipeLeftLabel(context.getString(R.string.delete))
            .setSwipeLeftLabelColor(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorSurfaceInverse,
                    Color.WHITE
                )
            )
            .create()
            .decorate()
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        val removedTask = displayList[position]

        // Remove from lists and notify adapter
        displayList.removeAt(position)
        adapter.notifyItemRemoved(position)
        val indexOfRemovedTaskInFullList = fullList.indexOfFirst { it.id == removedTask.id }
        if (indexOfRemovedTaskInFullList != -1) {
            fullList.removeAt(indexOfRemovedTaskInFullList)
        }
        StorageHelper.saveTasks(context, fullList)
        onSwipeCallback() // Call the callback to update UI

        // Show Snackbar with Undo action
        Snackbar.make(viewHolder.itemView.rootView, context.getString(R.string.removeSnackbarMessage, removedTask.title), Snackbar.LENGTH_LONG)
            .setAction(context.getString(R.string.undo)) {
                displayList.add(position, removedTask)
                adapter.notifyItemInserted(position)
                if (indexOfRemovedTaskInFullList != -1) {
                    fullList.add(indexOfRemovedTaskInFullList, removedTask)
                }
                StorageHelper.saveTasks(context, fullList)
                onSwipeCallback()
            }.show()
    }
}
