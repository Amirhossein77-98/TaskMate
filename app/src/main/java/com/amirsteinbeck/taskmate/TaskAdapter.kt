package com.amirsteinbeck.taskmate

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Paint
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.amirsteinbeck.taskmate.com.amirsteinbeck.focusmate.SettingsHelper
import com.amirsteinbeck.taskmate.databinding.ItemTaskBinding
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.math.log

class TaskAdapter (
    private val tasks: MutableList<Task>,
    private val layoutDir: String,
    private val onItemShortClick: (Task, Int) -> Unit,
    private val onItemLongClick: (Task, Int) -> Unit,
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private fun formatRelativeTimestamp(context: Context, timestamp: Long, due: Boolean): String {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000

        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())

        val formattedDate = dateFormat.format(date)
        val formattedTime = timeFormat.format(date)

        val diffDays = (now - timestamp) / oneDay

        if (due) {
            return context.getString(
                R.string.taskDueTimeTemplate,
                formattedDate,
                formattedTime
            )
        }
        return when (diffDays.toInt()) {
            0 -> context.getString(R.string.todayTimestamp, formattedTime)
            1 -> context.getString(R.string.yesterdayTimestamp, formattedTime)
            in 2..6 -> context.getString(R.string.thisWeekTimestampt)
            else -> context.getString(R.string.oldTasksTimestamp, formattedDate)
        }
    }

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun striker() {
            binding.taskTitle.paintFlags = binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.taskDescription.paintFlags = binding.taskDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.taskAddedDate.paintFlags = binding.taskAddedDate.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.taskDueDate.paintFlags = binding.taskDueDate.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.taskTitle.alpha = 0.5f
            binding.taskDescription.alpha = 0.5f
            binding.taskAddedDate.alpha = 0.5f
            binding.taskDueDate.alpha = 0.5f
        }

        fun unStriker() {
            binding.taskTitle.paintFlags = binding.taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding.taskDescription.paintFlags = binding.taskDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding.taskAddedDate.paintFlags = binding.taskAddedDate.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding.taskDueDate.paintFlags = binding.taskDueDate.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding.taskTitle.alpha = 1f
            binding.taskDescription.alpha = 1f
            binding.taskAddedDate.alpha = 0.7f
            binding.taskDueDate.alpha = 1f
        }

        fun isRtl(text: String): Boolean {
            val firstChar = text.trim().firstOrNull() ?: return false
            return firstChar in '\u0600' .. '\u06FF' ||
                    firstChar in '\u0750' .. '\u077F' ||
                    firstChar in '\u08A0' .. '\u08FF'
        }

        fun localizeTextDir() {
            if (layoutDir == "ltr") {
                binding.taskTitle.textAlignment = if (!isRtl(binding.taskTitle.text.toString())) View.TEXT_ALIGNMENT_TEXT_START
                else View.TEXT_ALIGNMENT_TEXT_END
                binding.taskDescription.textAlignment = if (!isRtl(binding.taskDescription.text.toString())) View.TEXT_ALIGNMENT_TEXT_START
                else View.TEXT_ALIGNMENT_TEXT_END
                binding.taskAddedDate.textAlignment = if (!isRtl(binding.taskAddedDate.text.toString())) View.TEXT_ALIGNMENT_TEXT_START
                else View.TEXT_ALIGNMENT_TEXT_END
                binding.taskDueDate.textAlignment = if (!isRtl(binding.taskDueDate.text.toString())) View.TEXT_ALIGNMENT_TEXT_START
                else View.TEXT_ALIGNMENT_TEXT_END
            } else {
                binding.taskTitle.textAlignment = if (!isRtl(binding.taskTitle.text.toString())) View.TEXT_ALIGNMENT_TEXT_END
                else View.TEXT_ALIGNMENT_TEXT_START
                binding.taskDescription.textAlignment = if (!isRtl(binding.taskDescription.text.toString())) View.TEXT_ALIGNMENT_TEXT_END
                else View.TEXT_ALIGNMENT_TEXT_START
                binding.taskAddedDate.textAlignment = if (!isRtl(binding.taskAddedDate.text.toString())) View.TEXT_ALIGNMENT_TEXT_END
                else View.TEXT_ALIGNMENT_TEXT_START
                binding.taskDueDate.textAlignment = if (!isRtl(binding.taskDueDate.text.toString())) View.TEXT_ALIGNMENT_TEXT_END
                else View.TEXT_ALIGNMENT_TEXT_START
            }
        }

        fun bind(task: Task) {
            binding.taskDone.setOnCheckedChangeListener(null)
            val context = binding.root.context
            binding.taskDone.isChecked = task.isDone
            binding.taskTitle.text = task.title
            binding.taskDescription.text = context.getString(R.string.taskDescTemplate, task.description)
            val relativeTimeStamp = formatRelativeTimestamp(binding.root.context, task.id, false)
            binding.taskAddedDate.text = context.getString(R.string.taskAddedTimeTemplate, relativeTimeStamp)
            binding.taskDueDate.text = formatRelativeTimestamp(binding.root.context, task.due, true)

            localizeTextDir()

            binding.root.setOnClickListener {
                onItemShortClick(task, bindingAdapterPosition)
            }

            binding.root.setOnLongClickListener {
                onItemLongClick(task, bindingAdapterPosition)
                true
            }

            val scaleUpX = ObjectAnimator.ofFloat(binding.taskDone, "scaleX", 0.8f, 1.05f, 1f)
            val scaleUpY = ObjectAnimator.ofFloat(binding.taskDone, "scaleY", 0.8f, 1.05f, 1f)
            scaleUpX.duration = 150
            scaleUpY.duration = 150
            binding.taskDone.isChecked = task.isDone

            if (task.isDone) striker() else unStriker()

            binding.taskDone.setOnCheckedChangeListener { _, isChecked ->
                task.isDone = isChecked

                scaleUpX.start()
                scaleUpY.start()

                if (isChecked) striker() else unStriker()

                sortTasks(binding.root.context)
                StorageHelper.saveTasks(binding.root.context, tasks)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.itemView.alpha = 0f
        holder.itemView.animate().alpha(1f).setDuration(150).start()
        holder.bind(tasks[position])
    }

    fun addItem(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    fun addItemAt(position: Int,task: Task) {
        tasks.add(position, task)
        notifyItemInserted(position)
    }

    fun removeItem(position: Int) {
        tasks.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateItem(position: Int, task: Task) {
        tasks[position] = task
        notifyItemChanged(position)
    }

    fun updateData(newList: MutableList<Task>) {
        tasks.clear()
        tasks.addAll(newList)
        notifyDataSetChanged()
    }


    fun sortTasks(context: Context) {
        if (!SettingsHelper.isAutoSort(context)) return

        val sortedList = tasks.sortedWith(compareBy<Task> { it.isDone })

        for (targetIndex in sortedList.indices) {
            val targetTask = sortedList[targetIndex]
            val currentIndex = tasks.indexOf(targetTask)

            if (currentIndex != targetIndex) {
                tasks.removeAt(currentIndex)
                tasks.add(targetIndex, targetTask)
                notifyItemMoved(currentIndex, targetIndex)
            }
        }
    }
}