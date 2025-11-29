package com.amirsteinbeck.focusmate

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Paint
import android.icu.text.SimpleDateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.amirsteinbeck.focusmate.databinding.ItemTaskBinding
import org.w3c.dom.Text
import java.util.Date
import java.util.Locale

class TaskAdapter (
    private val tasks: MutableList<Task>,
    private val layoutDir: String,
    private val onItemShortClick: (Task, Int) -> Unit,
    private val onItemLongClick: (Task, Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun striker() {
            binding.taskTitle.paintFlags = binding.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.taskDescription.paintFlags = binding.taskDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.taskTitle.alpha = 0.5f
            binding.taskDescription.alpha = 0.5f
        }

        fun unStriker() {
            binding.taskTitle.paintFlags = binding.taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding.taskDescription.paintFlags = binding.taskDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding.taskTitle.alpha = 1f
            binding.taskDescription.alpha = 1f
        }

        fun timeStamper(timeTag: String, formattedTime: String): String {
            val cntx = binding.root.context
            return when (timeTag) {
                "Today" -> cntx.getString(R.string.todayTimestamp, formattedTime)
                "Yesterday" -> cntx.getString(R.string.yesterdayTimestamp, formattedTime)
                "Week" -> cntx.getString(R.string.thisWeekTimestampt)
                "Old" -> cntx.getString(R.string.oldTasksTimestamp, formattedTime)
                else -> return "Unknown Time"
            }
        }

        fun isRtl(text: String): Boolean {
            val firstChar = text.trim().firstOrNull() ?: return false
            return firstChar in '\u0600' .. '\u06FF' ||
                    firstChar in '\u0750' .. '\u077F' ||
                    firstChar in '\u08A0' .. '\u08FF'
        }

        fun bind(task: Task) {
            binding.taskDone.setOnCheckedChangeListener(null)


            val todayDateMillis = System.currentTimeMillis()
            val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())
            val monthFormatter = SimpleDateFormat("MM", Locale.getDefault())
            val todayDate = dayFormatter.format(todayDateMillis)
            val todayMonth = monthFormatter.format(todayDateMillis)


            binding.taskDone.isChecked = task.isDone
            binding.taskTitle.text = task.title
            binding.taskDescription.text = task.description
            val date = Date(task.id)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedDate = dateFormatter.format(date)
            val formattedTime = timeFormatter.format(date)
            val taskDate = dayFormatter.format(date)
            val taskMonth = monthFormatter.format(date)
            var relativeTimeStamp = timeStamper("Unknown", formattedTime)

            if (taskMonth == todayMonth) {
                if ((todayDate.toInt() - taskDate.toInt()) != 0) {
                    if ((todayDate.toInt() - taskDate.toInt()) == 1) {
                        relativeTimeStamp = timeStamper("Yesterday", formattedTime)
                    } else if (todayDate.toInt() - taskDate.toInt() in 2..7) {
                        relativeTimeStamp = timeStamper("Week", formattedTime)
                    }
                } else {
                    relativeTimeStamp = timeStamper("Today", formattedTime)
                }
            } else {
                relativeTimeStamp = timeStamper("Old", formattedDate)
            }

            binding.taskAddedDate.text = relativeTimeStamp

            if (layoutDir == "ltr") {
                binding.taskTitle.textAlignment = if (!isRtl(task.title)) View.TEXT_ALIGNMENT_TEXT_START
                else View.TEXT_ALIGNMENT_TEXT_END
                binding.taskDescription.textAlignment = if (!isRtl(task.description)) View.TEXT_ALIGNMENT_TEXT_START
                else View.TEXT_ALIGNMENT_TEXT_END
                binding.taskAddedDate.textAlignment = if (!isRtl(relativeTimeStamp)) View.TEXT_ALIGNMENT_TEXT_START
                else View.TEXT_ALIGNMENT_TEXT_END
            } else {
                binding.taskTitle.textAlignment = if (!isRtl(task.title)) View.TEXT_ALIGNMENT_TEXT_END
                else View.TEXT_ALIGNMENT_TEXT_START
                binding.taskDescription.textAlignment = if (!isRtl(task.description)) View.TEXT_ALIGNMENT_TEXT_END
                else View.TEXT_ALIGNMENT_TEXT_START
                binding.taskAddedDate.textAlignment = if (!isRtl(relativeTimeStamp)) View.TEXT_ALIGNMENT_TEXT_END
                else View.TEXT_ALIGNMENT_TEXT_START
            }

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

            binding.taskDone.setOnCheckedChangeListener { _, isChecked ->
                task.isDone = isChecked
                sortTasks()
                notifyItemChanged(bindingAdapterPosition)
            }

            if (task.isDone) striker() else unStriker()

            binding.taskDone.setOnCheckedChangeListener { _, isChecked ->
                task.isDone = isChecked

                scaleUpX.start()
                scaleUpY.start()

                if (isChecked) striker() else unStriker()

                sortTasks()
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

    fun clearTasks() {
        tasks.clear()
        notifyDataSetChanged()
    }

    fun updateData(newList: MutableList<Task>) {
        tasks.clear()
        tasks.addAll(newList)
        notifyDataSetChanged()
    }

    fun sortTasks() {
        tasks.sortWith(
            compareBy<Task> { it.isDone }
        )
        notifyDataSetChanged()
    }
}