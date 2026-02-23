package com.example.kotlinexercise_1

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomCalendarView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var currentDate: Date = Calendar.getInstance().time
    var onDateClickListener: ((Date) -> Unit)? = null
    private val calendarAdapter = CalendarAdapter { date ->
        onDateClickListener?.invoke(date)
    }

    private val monthYearTextView: TextView
    private val prevMonthButton: ImageView
    private val nextMonthButton: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_calendar, this, true)
        val calendarRecyclerView = findViewById<RecyclerView>(R.id.calendarRecyclerView)
        monthYearTextView = findViewById(R.id.monthYearTextView)
        prevMonthButton = findViewById(R.id.prevMonthButton)
        nextMonthButton = findViewById(R.id.nextMonthButton)

        calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)
        calendarRecyclerView.adapter = calendarAdapter

        prevMonthButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.MONTH, -1)
            currentDate = calendar.time
            updateCalendar()
        }

        nextMonthButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.MONTH, 1)
            currentDate = calendar.time
            updateCalendar()
        }

        updateCalendar()
    }

    fun setTaskDates(taskDates: List<Date>) {
        calendarAdapter.setTaskDates(taskDates)
    }

    private fun updateCalendar() {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate

        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearTextView.text = monthYearFormat.format(calendar.time)

        val dates = mutableListOf<Date>()
        val month = calendar.get(Calendar.MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthBeginning = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginning)

        while (dates.size < 42) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendarAdapter.setDates(dates, month)
    }
}
