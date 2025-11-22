package com.amirsteinbeck.focusmate

import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import com.amirsteinbeck.focusmate.databinding.ItemTaskBinding

class TaskAdapter (
    private val tasks: MutableList<Task>,
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

        fun bind(task: Task) {
            binding.taskDone.setOnCheckedChangeListener(null)

            binding.taskDone.isChecked = task.isDone
            binding.taskTitle.text = task.title
            binding.taskDescription.text = task.description

            binding.root.setOnClickListener {
                onItemShortClick(task, bindingAdapterPosition)
            }

            binding.root.setOnLongClickListener {
                onItemLongClick(task, bindingAdapterPosition)
                true
            }

            binding.taskDone.isChecked = task.isDone

            binding.taskDone.setOnCheckedChangeListener { _, isChecked ->
                task.isDone = isChecked
                notifyItemChanged(bindingAdapterPosition)
            }

            if (task.isDone) striker() else unStriker()

            binding.taskDone.setOnCheckedChangeListener { _, isChecked ->
                task.isDone = isChecked

                if (isChecked) striker() else unStriker()

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
        holder.bind(tasks[position])
    }

    fun addItem(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    fun addItemAt(position: Int,task: Task) {
        tasks.add(position, task)
        notifyItemInserted(tasks.size - 1)
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
}