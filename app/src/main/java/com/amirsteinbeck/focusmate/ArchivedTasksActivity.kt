package com.amirsteinbeck.focusmate

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amirsteinbeck.focusmate.databinding.ActivityArchivedTasksBinding

class ArchivedTasksActivity : AppCompatActivity() {

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
            { clickedTask, position ->},
            {clickedTask, position ->}
        )

        val leftSwipeHelper = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

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

                Snackbar.make(binding.root, "Task (${unArchivedTask.title}) unarchived!", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        unArchivedTask.isArchived = true

                        if (fullListIndex != -1) {
                            fullList[fullListIndex] = unArchivedTask
                            StorageHelper.saveTasks(this@ArchivedTasksActivity, fullList)
                        }

                        displayList.add(position, unArchivedTask)
                        adapter.notifyItemInserted(position)
                        updateEmptyView()
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