package com.example.kotlinexercise_1

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the task list. This class takes a list of tasks and displays them in a RecyclerView.
 */
class TaskAdapter(private val onTaskClick: (Task) -> Unit) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasks: List<Task> = emptyList()

    /**
     * Creates a new ViewHolder for a task item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    /**
     * Binds the data to the views in a ViewHolder.
     */
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount(): Int = tasks.size

    /**
     * Submits a new list of tasks to the adapter.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for a task item. This class holds the views for a single task item.
     */
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskNameTextView: TextView = itemView.findViewById(R.id.taskNameTextView)
        private val taskTimeTextView: TextView = itemView.findViewById(R.id.taskTimeTextView)
        private val cardView = itemView as CardView

        /**
         * Binds a task to the views in the ViewHolder.
         */
        fun bind(task: Task) {
            taskNameTextView.text = task.name
            taskTimeTextView.text = "${task.startTime} - ${task.endTime}"
            itemView.setOnClickListener { onTaskClick(task) }

            val background = when (task.priority) {
                "High" -> R.drawable.task_background_high
                "Medium" -> R.drawable.task_background_medium
                "Low" -> R.drawable.task_background_low
                else -> android.R.color.transparent
            }
            cardView.setBackgroundResource(background)
            val textColor = when (task.priority) {
                "High" -> Color.WHITE
                else -> Color.BLACK
            }
            taskNameTextView.setTextColor(textColor)
            taskTimeTextView.setTextColor(textColor)
        }
    }
}
