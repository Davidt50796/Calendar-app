package com.example.kotlinexercise_1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Date

class CalendarAdapter(private val onDateClickListener: (Date) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val dates = mutableListOf<Date>()
    private var selectedDate: Date? = null
    private var taskDates = listOf<Date>()
    private var currentMonth: Int = 0

    fun setDates(dates: List<Date>, currentMonth: Int) {
        this.dates.clear()
        this.dates.addAll(dates)
        this.currentMonth = currentMonth
        notifyDataSetChanged()
    }

    fun setSelectedDate(date: Date) {
        selectedDate = date
        notifyDataSetChanged()
    }

    fun setTaskDates(taskDates: List<Date>) {
        this.taskDates = taskDates
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_day_layout, parent, false)
        return CalendarViewHolder(view, onDateClickListener)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(dates[position])
    }

    override fun getItemCount(): Int = dates.size

    inner class CalendarViewHolder(
        itemView: View,
        private val onDateClickListener: (Date) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val dayOfMonth: TextView = itemView.findViewById(R.id.calendarDayText)

        fun bind(date: Date) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            dayOfMonth.text = calendar.get(Calendar.DAY_OF_MONTH).toString()

            if (calendar.get(Calendar.MONTH) == currentMonth) {
                dayOfMonth.setTextColor(Color.WHITE)
            } else {
                dayOfMonth.setTextColor(Color.GRAY)
            }

            val today = Calendar.getInstance()
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                dayOfMonth.setBackgroundResource(R.drawable.today_background)
            }

            if (taskDates.any { taskDate ->
                val taskCalendar = Calendar.getInstance()
                taskCalendar.time = taskDate
                calendar.get(Calendar.YEAR) == taskCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == taskCalendar.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == taskCalendar.get(Calendar.DAY_OF_MONTH)
            }) {
                dayOfMonth.setBackgroundResource(R.drawable.task_day_background)
            }

            if (selectedDate != null) {
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.time = selectedDate!!
                if (calendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH)) {
                    dayOfMonth.setBackgroundResource(R.drawable.selected_day_background)
                }
            }

            itemView.setOnClickListener {
                onDateClickListener(date)
            }
        }
    }
}
