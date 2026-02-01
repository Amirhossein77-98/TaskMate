package com.amirsteinbeck.taskmate

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.amirsteinbeck.taskmate.com.amirsteinbeck.focusmate.FadeItemAnimator
import com.amirsteinbeck.taskmate.com.amirsteinbeck.focusmate.LocaleHelper
import com.amirsteinbeck.taskmate.com.amirsteinbeck.focusmate.SettingsHelper
import com.amirsteinbeck.taskmate.com.amirsteinbeck.taskmate.TaskBottomSheet
import com.amirsteinbeck.taskmate.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fullList: MutableList<Task>
    private lateinit var displayList: MutableList<Task>
    private lateinit var adapter: TaskAdapter

    fun updateLists() {
        val fullList = StorageHelper.loadTasks(this)
        val displayList = fullList.filter { !it.isArchived }.toMutableList()

        adapter.sortTasks(this)
        adapter.updateData(displayList)
    }

    override fun attachBaseContext(newBase: Context?) {
        val context = newBase?.let { LocaleHelper.applyLanguage(it) }
        super.attachBaseContext(context)
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

        AppCompatDelegate.setDefaultNightMode(
            if (SettingsHelper.isDarkMode(this))
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

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

        adapter = TaskAdapter(
            displayList,
            if (LocaleHelper.getLanguage(this) == "en") "ltr" else "rtl",
            { clickedTask, position -> {}},
            { clickedTask, position -> TaskBottomSheet.show(
                activity = this,
                task = clickedTask,
                position = position,
                isEdit = true
            ) { task, pos ->
                adapter.updateItem(pos, task)
                StorageHelper.saveTasks(this, displayList)
                updateLists()
            }
            }
            )

        adapter.sortTasks(this)

        val leftSwipeHandler = LeftSwipeHandler(this, adapter, fullList, displayList) {
            updateEmptyView()
            if (fullList.size > 1) adapter.sortTasks(this)
        }
        ItemTouchHelper(leftSwipeHandler).attachToRecyclerView(binding.recyclerView)

        val rightSwipeHandler = RightSwipeHandler(this, adapter, fullList, displayList) {
            updateEmptyView()
            if (fullList.size > 1) adapter.sortTasks(this)
        }
        ItemTouchHelper(rightSwipeHandler).attachToRecyclerView(binding.recyclerView)


        binding.recyclerView.itemAnimator = FadeItemAnimator()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        updateEmptyView()

        binding.submitButton.setOnClickListener {
            TaskBottomSheet.show(
                activity = this,
                isEdit = false
            ) { task, _ ->
                adapter.addItem(task)
                StorageHelper.saveTasks(this, displayList)
                updateLists()
            }
        }

        binding.userInput.setOnClickListener {
            TaskBottomSheet.show(
                activity = this,
                isEdit = false
            ) { task, _ ->
                adapter.addItem(task)
                StorageHelper.saveTasks(this, displayList)
                updateLists()
            }
        }

        binding.settingsButton.setOnClickListener {
            NavigationHelper.goToSettings(this)
        }

        binding.archivesButton.setOnClickListener {
            NavigationHelper.goToArchivedTasks(this)
        }

        binding.appInfoButton.setOnClickListener {
            NavigationHelper.goToCredentials(this)
        }

        AnimationHelper.applyPressAnimation(this, binding.submitButton)
        AnimationHelper.applyPressAnimation(this, binding.settingsButton)
        AnimationHelper.applyPressAnimation(this, binding.archivesButton)
        AnimationHelper.applyPressAnimation(this, binding.appInfoButton)
    }

    override fun onResume() {
        super.onResume()

        updateLists()
        adapter.sortTasks(this@MainActivity)
    }
}