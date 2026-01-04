package com.amirsteinbeck.taskmate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class RightSwipeHandler(
    private val context: Context,
    private val adapter: TaskAdapter,
    private var fullList: MutableList<Task>,
    private var displayList: MutableList<Task>,
    private val onSwipeCallback: () -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

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
            .addSwipeRightBackgroundColor(ContextCompat.getColor(context, R.color.teal))
            .addSwipeRightActionIcon(R.drawable.archive_check_svgrepo_com_24)
            .setSwipeRightActionIconTint(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorSurfaceInverse,
                    Color.WHITE
                )
            )
            .addSwipeRightLabel(context.getString(R.string.archive))
            .setSwipeRightLabelColor(
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
        val archivedTask = displayList[position]

        // Update task state and save
        archivedTask.isArchived = true
        val indexInFullList = fullList.indexOfFirst { it.id == archivedTask.id }
        if (indexInFullList != -1) {
            fullList[indexInFullList] = archivedTask
            StorageHelper.saveTasks(context, fullList)
        }

        // Remove from display list and notify adapter
        displayList.removeAt(position)
        adapter.notifyItemRemoved(position)
        onSwipeCallback()

        // Show Snackbar with Undo action
        Snackbar.make(viewHolder.itemView.rootView, context.getString(R.string.archiveSnackbarMessage, archivedTask.title), Snackbar.LENGTH_LONG)
            .setAction(context.getString(R.string.undo)) {
                archivedTask.isArchived = false
                if (indexInFullList != -1) {
                    fullList[indexInFullList] = archivedTask
                    StorageHelper.saveTasks(context, fullList)
                }
                displayList.add(position, archivedTask)
                adapter.notifyItemInserted(position)
                onSwipeCallback()
            }.show()
    }
}
