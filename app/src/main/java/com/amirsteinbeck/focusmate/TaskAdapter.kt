package com.amirsteinbeck.focusmate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amirsteinbeck.focusmate.databinding.ItemTaskBinding

class TaskAdapter (
    private val tasks: MutableList<Task>,
    private val onItemShortClick: (Task, Int) -> Unit,
    private val onItemLongClick: (Task, Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.taskTitle.text = task.title
            binding.taskDescription.text = task.description

            binding.root.setOnClickListener {
                onItemShortClick(task, bindingAdapterPosition)
            }

            binding.root.setOnLongClickListener {
                onItemLongClick(task, bindingAdapterPosition)
                true
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

    fun addItem(user: Task) {
        tasks.add(user)
        notifyItemInserted(tasks.size - 1)
    }

    fun removeItem(position: Int) {
        tasks.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateItem(position: Int, user: Task) {
        tasks[position] = user
        notifyItemChanged(position)
    }
}