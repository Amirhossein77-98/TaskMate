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

        fun openTaskBottomSheet(task: Task? = null, position: Int = -1, isEdit: Boolean) {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottomsheet_edit_task, null)

            val titleInput = view.findViewById<TextInputEditText>(R.id.editTitle)
            val descInput = view.findViewById<TextInputEditText>(R.id.editDescription)
            val saveButton = view.findViewById<Button>(R.id.saveButton)
            val dateTimePickerBtn = view.findViewById<Button>(R.id.dateTimePickerBtn)

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

            var selectedDateTime: LocalDateTime? = null

            dateTimePickerBtn.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.dateDemanderTitle)
                    .build()

                datePicker.show(supportFragmentManager, "DATE_PICKER")

                datePicker.addOnPositiveButtonClickListener { selection ->
                    val selectedDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    val timePicker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(30)
                        .setTitleText("@string/timeDemanderTitle")
                        .build()

                    timePicker.show(supportFragmentManager, "TIME_PICKER")

                    timePicker.addOnPositiveButtonClickListener {
                        val hour = timePicker.hour
                        val minute = timePicker.minute

                        selectedDateTime = LocalDateTime.of(
                            selectedDate,
                            LocalTime.of(hour, minute)
                        )

                        dateTimePickerBtn.text =
                            if (selectedDateTime != null)
                                "${selectedDateTime.year}/${selectedDateTime.monthValue}/${selectedDateTime.dayOfMonth} at ${selectedDateTime.hour}:${selectedDateTime.minute}"
                            else getString(R.string.datePicker)
                    }
                }
            }

            saveButton.setOnClickListener {
                val newTitle = titleInput.text.toString().trim()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                val newDesc = descInput.text.toString().trim()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                val savableDue =
                    if (selectedDateTime != null) selectedDateTime?.atZone(ZoneId.systemDefault())
                    ?.toInstant()
                    ?.toEpochMilli()
                    ?: System.currentTimeMillis()
                    else System.currentTimeMillis()

                val theTask = Task(
                    newTitle,
                    newDesc.ifEmpty { getString(R.string.newTask) },
                    false,
                    due = savableDue)

                if (isEdit) adapter.updateItem(position, theTask) else adapter.addItem(theTask)
                StorageHelper.saveTasks(this, displayList)
                binding.recyclerView.scrollToPosition(displayList.size - 1)


                dialog.dismiss()
                fullList.add(theTask)
                updateLists()
                updateEmptyView()
                adapter.sortTasks(this)
            }
            updateEmptyView()
            dialog.setContentView(view)
            dialog.show()
        }

        adapter = TaskAdapter(
            displayList,
            if (LocaleHelper.getLanguage(this) == "en") "ltr" else "rtl",
            { clickedTask, position -> {}},
            { clickedTask, position -> openTaskBottomSheet(clickedTask, position, true) }
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
            openTaskBottomSheet(isEdit = false)
        }

        binding.userInput.setOnClickListener {
            openTaskBottomSheet(isEdit = false)
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