package com.amirsteinbeck.focusmate

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amirsteinbeck.focusmate.com.amirsteinbeck.focusmate.FadeItemAnimator
import com.amirsteinbeck.focusmate.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fullList: MutableList<Task>
    private lateinit var displayList: MutableList<Task>

    private lateinit var adapter: TaskAdapter

    fun updateLists() {
        val fullList = StorageHelper.loadTasks(this)
        val displayList = fullList.filter { !it.isArchived }.toMutableList()

        adapter.sortTasks()
        adapter.updateData(displayList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, imeInsets.bottom)
            insets
        }

        fullList = StorageHelper.loadTasks(this)
        displayList = fullList.filter { !it.isArchived }.toMutableList()

        fun updateEmptyView() {
            if (adapter.itemCount == 0) {
                binding.emptyTasksView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyTasksView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }



        fun openTaskBottomSheet(task: Task? = null, position: Int = -1, isEdit: Boolean) {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottomsheet_edit_task, null)

            val titleInput = view.findViewById<TextInputEditText>(R.id.editTitle)
            val descInput = view.findViewById<TextInputEditText>(R.id.editDescription)
            val saveButton = view.findViewById<View>(R.id.saveButton)

            if (!isEdit) saveButton.isEnabled = false

            if (task == null) titleInput.setText("") else titleInput.setText(task.title)
            if (task == null) descInput.setText("") else descInput.setText(task.description)

            dialog.setOnShowListener {
                titleInput.requestFocus()
                titleInput.post {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(titleInput, InputMethodManager.SHOW_IMPLICIT)
                }
            }

            titleInput.addTextChangedListener {
                saveButton.isEnabled = !it.isNullOrBlank()
            }

            saveButton.setOnClickListener {
                val newTitle = titleInput.text.toString().trim()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                val newDesc = descInput.text.toString().trim()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                val theTask = Task(
                    newTitle,
                    newDesc.ifEmpty { getString(R.string.newTask) },
                    false)

                if (isEdit) adapter.updateItem(position, theTask) else adapter.addItem(theTask)
                StorageHelper.saveTasks(this, displayList)
                binding.recyclerView.scrollToPosition(displayList.size - 1)


                dialog.dismiss()
                updateLists()
                fullList.add(theTask)
                updateEmptyView()
                adapter.sortTasks()
            }
            updateEmptyView()
            dialog.setContentView(view)
            dialog.show()
        }

        adapter = TaskAdapter(
            displayList,
            { clickedTask, position ->
//                NavigationHelper.goToTaskDetails(this, clickedTask.title, clickedTask.description)

            },
            { clickedTask, position -> openTaskBottomSheet(clickedTask, position, true) }
            )

        adapter.sortTasks()

        val leftSwipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
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
                    ContextCompat.getColor(this@MainActivity, R.color.red)
                )
                    .addSwipeLeftActionIcon(R.drawable.trash_bin_minimalistic_2_svgrepo_com_24)
                    .setSwipeLeftActionIconTint(
                        ContextCompat.getColor(this@MainActivity, R.color.white)
                    )
                    .addSwipeLeftLabel(getString(R.string.delete))
                    .setSwipeLeftLabelColor(
                        ContextCompat.getColor(this@MainActivity, R.color.white)
                    )
                    .create()
                    .decorate()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                updateEmptyView()
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val removedTask = displayList[position]
                displayList.removeAt(position)
                adapter.notifyItemRemoved(position)
                val indexOfRemovedTaskInFullList = fullList.indexOfFirst { it.id == removedTask.id }
                if (indexOfRemovedTaskInFullList != -1) fullList.removeAt(indexOfRemovedTaskInFullList)
                StorageHelper.saveTasks(this@MainActivity, fullList)
                if (fullList.size > 1) adapter.sortTasks()
                updateEmptyView()

                Snackbar.make(binding.root, getString(R.string.removeSnackbarMessage, removedTask.title), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo)) {
                        displayList.add(position, removedTask)
                        adapter.notifyItemInserted(position)
                        fullList.add(indexOfRemovedTaskInFullList, removedTask)
                        StorageHelper.saveTasks(this@MainActivity, fullList)
                        adapter.sortTasks()
                        updateEmptyView()

                    }.show()
            }
        }
        ItemTouchHelper(leftSwipeHandler).attachToRecyclerView(binding.recyclerView)

        val rightSwipeHandler = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
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
                ).addSwipeRightBackgroundColor(
                    ContextCompat.getColor(this@MainActivity, R.color.teal)
                )
                    .addSwipeRightActionIcon(R.drawable.archive_check_svgrepo_com_24)
                    .setSwipeRightActionIconTint(
                        ContextCompat.getColor(this@MainActivity, R.color.white)
                    )
                    .addSwipeRightLabel(getString(R.string.archive))
                    .setSwipeRightLabelColor(
                        ContextCompat.getColor(this@MainActivity, R.color.white)
                    )
                    .create()
                    .decorate()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                updateEmptyView()
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val archivedTask = displayList[position]
                archivedTask.isArchived = true

                val indexInFullList = fullList.indexOfFirst { it.id == archivedTask.id }
                if (indexInFullList != -1) {
                    fullList[indexInFullList] = archivedTask
                    StorageHelper.saveTasks(this@MainActivity, fullList)
                }

                displayList.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateEmptyView()
                if (fullList.size > 1) adapter.sortTasks()

                Snackbar.make(binding.root, getString(R.string.archiveSnackbarMessage, archivedTask.title), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo)) {
                        archivedTask.isArchived = false

                        if (indexInFullList != -1) {
                            fullList[indexInFullList] = archivedTask
                            StorageHelper.saveTasks(this@MainActivity, fullList)
                        }
                        displayList.add(position, archivedTask)
                        adapter.notifyItemInserted(position)
                        updateEmptyView()
                        adapter.sortTasks()
                    }.show()
            }
        }
        ItemTouchHelper(rightSwipeHandler).attachToRecyclerView(binding.recyclerView)

        binding.recyclerView.itemAnimator = FadeItemAnimator()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        updateEmptyView()

        binding.submitButton.setOnClickListener {
            openTaskBottomSheet(isEdit = false)
        }

        binding.userInput.setOnClickListener {
            openTaskBottomSheet(isEdit = false)
        }

        binding.resetButton.setOnClickListener {
            binding.userInput.setText("")
            binding.taskInputLayout.error = null
            binding.taskInputLayout.isErrorEnabled = false
            adapter.clearTasks()
            StorageHelper.saveTasks(this, displayList)
            updateEmptyView()
        }

        binding.rightPageButtonNavigator.setOnClickListener {
            NavigationHelper.goToArchivedTasks(this)
        }

        binding.leftPageButtonNavigator.setOnClickListener {
            NavigationHelper.goToCredentials(this)
        }

        AnimationHelper.applyPressAnimation(this, binding.submitButton)
        AnimationHelper.applyPressAnimation(this, binding.resetButton)
        AnimationHelper.applyPressAnimation(this, binding.rightPageButtonNavigator)
        AnimationHelper.applyPressAnimation(this, binding.leftPageButtonNavigator)
    }

    override fun onResume() {
        super.onResume()

        updateLists()
    }
}