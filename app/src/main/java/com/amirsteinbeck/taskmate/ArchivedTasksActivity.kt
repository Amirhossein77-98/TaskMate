package com.amirsteinbeck.taskmate

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amirsteinbeck.taskmate.com.amirsteinbeck.focusmate.LocaleHelper
import com.amirsteinbeck.taskmate.databinding.ActivityArchivedTasksBinding
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class ArchivedTasksActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val context = newBase?.let { LocaleHelper.applyLanguage(it) }
        super.attachBaseContext(context)
    }

    private lateinit var binding: ActivityArchivedTasksBinding
    private lateinit var fullList: MutableList<Task>
    private lateinit var displayList: MutableList<Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchivedTasksBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        lateinit var adapter: TaskAdapter
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        fullList = StorageHelper.loadTasks(this)
        displayList = fullList.filter { it.isArchived } as MutableList<Task>

        fun updateEmptyView() {
            if (adapter.itemCount == 0) {
                binding.emptyTasksView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyTasksView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        adapter = TaskAdapter(
            displayList,
            if (LocaleHelper.getLanguage(this) == "en") "ltr" else "rtl",
            { clickedTask, position ->},
            {clickedTask, position ->}
        )
        adapter.sortTasks(this)

        val leftSwipeHelper = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
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
                RecyclerViewSwipeDecorator.Builder(
                    c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                ).addSwipeLeftBackgroundColor(
                    ContextCompat.getColor(this@ArchivedTasksActivity, R.color.purple_200)
                )
                    .addSwipeLeftActionIcon(R.drawable.unarchive_svgrepo_com)
                    .setSwipeLeftActionIconTint(
                        ContextCompat.getColor(this@ArchivedTasksActivity, R.color.white)
                    )
                    .addSwipeLeftLabel(getString(R.string.unarchive))
                    .setSwipeLeftLabelColor(
                        ContextCompat.getColor(this@ArchivedTasksActivity, R.color.white)
                    )
                    .create()
                    .decorate()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                updateEmptyView()
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val unArchivedTask = displayList[position]
                unArchivedTask.isArchived = false

                val fullListIndex = fullList.indexOfFirst { it.id == unArchivedTask.id }
                if (fullListIndex != -1) {
                    fullList[fullListIndex] = unArchivedTask
                    StorageHelper.saveTasks(this@ArchivedTasksActivity, fullList)
                }

                displayList.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateEmptyView()
                if (fullList.size > 1) adapter.sortTasks(this@ArchivedTasksActivity)

                Snackbar.make(binding.root, getString(R.string.unarchiveSnackbarMessage, unArchivedTask.title), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo)) {
                        unArchivedTask.isArchived = true

                        if (fullListIndex != -1) {
                            fullList[fullListIndex] = unArchivedTask
                            StorageHelper.saveTasks(this@ArchivedTasksActivity, fullList)
                        }

                        displayList.add(position, unArchivedTask)
                        adapter.notifyItemInserted(position)
                        updateEmptyView()
                        adapter.sortTasks(this@ArchivedTasksActivity)
                    }.show()
            }
        }
        ItemTouchHelper(leftSwipeHelper).attachToRecyclerView(binding.recyclerView)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        updateEmptyView()

        binding.backButton.setOnClickListener {
            NavigationHelper.goBack(this)
        }
    }
}