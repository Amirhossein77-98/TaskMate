package com.amirsteinbeck.taskmate.com.amirsteinbeck.taskmate

import android.content.Context
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.amirsteinbeck.taskmate.R
import com.amirsteinbeck.taskmate.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import androidx.core.widget.addTextChangedListener
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

object TaskBottomSheet {
    fun show(
        activity: AppCompatActivity,
        task: Task? = null,
        position: Int = -1,
        isEdit: Boolean = false,
        onSave: (Task, Int) -> Unit
    ) {
        val dialog = BottomSheetDialog(activity)
        val view = LayoutInflater.from(activity)
            .inflate(R.layout.bottomsheet_edit_task, null)

        val titleInput = view.findViewById<TextInputEditText>(R.id.editTitle)
        val descInput = view.findViewById<TextInputEditText>(R.id.editDescription)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val dateTimePickerBtn = view.findViewById<Button>(R.id.dateTimePickerBtn)

        if (!isEdit) saveButton.isEnabled = false

        titleInput.setText(task?.title.orEmpty())
        descInput.setText(task?.description.orEmpty())

        dialog.setOnShowListener {
            titleInput.requestFocus()
            titleInput.post {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                imm.showSoftInput(titleInput, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        titleInput.addTextChangedListener {
            if (!it.isNullOrBlank() && it.length > 2) {

                saveButton.isEnabled = true
            } else {

                saveButton.isEnabled = false
            }
        }

        var selectedDateTime: LocalDateTime? = null

        dateTimePickerBtn.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.dateDemanderTitle)
                .build()

            datePicker.show(activity.supportFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(30)
                    .build()

                timePicker.show(activity.supportFragmentManager, "TIME_PICKER")

                timePicker.addOnPositiveButtonClickListener {
                    selectedDateTime = LocalDateTime.of(
                        date,
                        LocalTime.of(timePicker.hour, timePicker.minute)
                    )

                    dateTimePickerBtn.text =
                        activity.getString(R.string.DateTimePickerBtnTemplate,
                            date.year.toString(),
                            date.monthValue.toString(),
                            date.dayOfMonth.toString(),
                            timePicker.hour.toString(),
                            timePicker.minute.toString())
                }
            }
        }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }

            val desc = descInput.text.toString().trim()
                .ifEmpty { activity.getString(R.string.newTask) }

            val due = selectedDateTime
                ?.atZone(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
                ?: System.currentTimeMillis()

            val newTask = Task(title, desc, false, due = due)

            onSave(newTask, position)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
