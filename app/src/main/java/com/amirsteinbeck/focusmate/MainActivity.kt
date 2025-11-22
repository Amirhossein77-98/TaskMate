package com.amirsteinbeck.focusmate

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amirsteinbeck.focusmate.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        lateinit var adapter: TaskAdapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, imeInsets.bottom)
            insets
        }

        val items = StorageHelper.loadTasks(this)

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

            if (task == null) titleInput.setText("") else titleInput.setText(task.title)
            if (task == null) descInput.setText("") else descInput.setText(task.description)

            dialog.setOnShowListener {
                titleInput.requestFocus()
                titleInput.post {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(titleInput, InputMethodManager.SHOW_IMPLICIT)
                }
            }

            saveButton.setOnClickListener {
                val newTitle = titleInput.text.toString().trim()
                val newDesc = descInput.text.toString().trim()

                val theTask = Task(newTitle, newDesc, false)

                if (isEdit) adapter.updateItem(position, theTask) else adapter.addItem(theTask)
                StorageHelper.saveTasks(this, items)
                binding.recyclerView.scrollToPosition(items.size - 1)


                dialog.dismiss()
            }
            updateEmptyView()
            dialog.setContentView(view)
            dialog.show()

        }

        adapter = TaskAdapter(
            items,
            { clickedTask, position ->
                Snackbar.make(binding.root, "Chose: ${clickedTask.title}", Snackbar.LENGTH_SHORT).show()
                NavigationHelper.goToTaskDetails(this, clickedTask.title, clickedTask.description)

            },
            { clickedTask, position -> openTaskBottomSheet(clickedTask, position, true) }
            )

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val removedTask = items[position]
                adapter.removeItem(position)
                StorageHelper.saveTasks(this@MainActivity, items)
                updateEmptyView()

                Snackbar.make(binding.root, "Task (${removedTask.title}) Removed", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        adapter.addItemAt(position, removedTask)
                        StorageHelper.saveTasks(this@MainActivity, items)
                        updateEmptyView()
                    }.show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerView)


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
            StorageHelper.saveTasks(this, items)
            updateEmptyView()
        }

        binding.rightPageButtonNavigator.setOnClickListener {
            NavigationHelper.goToTaskDetails(this,
                getString(R.string.titlePlaceholder),
                    getString(R.string.descriptionPlaceholder))
        }

        binding.leftPageButtonNavigator.setOnClickListener {
            NavigationHelper.goToCredentials(this)
        }

        AnimationHelper.applyPressAnimation(this, binding.submitButton)
        AnimationHelper.applyPressAnimation(this, binding.resetButton)
        AnimationHelper.applyPressAnimation(this, binding.rightPageButtonNavigator)
        AnimationHelper.applyPressAnimation(this, binding.leftPageButtonNavigator)
    }
}