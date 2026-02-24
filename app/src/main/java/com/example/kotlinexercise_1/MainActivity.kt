package com.example.kotlinexercise_1

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * The main screen of the application. Displays a calendar and a list of tasks for the selected date.
 */
class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var calendarView: CustomCalendarView
    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var addTaskFab: FloatingActionButton
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var noTasksTextView: TextView

    // Data
    private val allTasks = mutableListOf<Task>()
    private var selectedDate: Long = 0

    // Utilities
    private val gson = Gson()
    private val prefs by lazy { getSharedPreferences("tasks", MODE_PRIVATE) }

    /**
     * Registers a callback for the result of requesting a permission.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // TODO: Inform the user that the feature is unavailable because the feature requires a permission that the user has denied.
        }
    }

    /**
     * Initializes the activity, sets up the views, and loads the initial data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        // Initialize views
        calendarView = findViewById(R.id.calendarView)
        taskRecyclerView = findViewById(R.id.taskRecyclerView)
        addTaskFab = findViewById(R.id.addTaskFab)
        noTasksTextView = findViewById(R.id.noTasksTextView)

        // Set the initial selected date
        selectedDate = Calendar.getInstance().timeInMillis

        // Load tasks from shared preferences
        loadTasks()

        // Set up the task adapter
        taskAdapter = TaskAdapter { task ->
            showUpdateTaskDialog(task)
        }
        taskRecyclerView.adapter = taskAdapter

        calendarView.onDateClickListener = { date: Date ->
            selectedDate = date.time
            filterTasks(selectedDate)
        }

        // Set up the add task button
        addTaskFab.setOnClickListener {
            showAddTaskDialog()
        }

        // Initial filter for today
        filterTasks(selectedDate)
        askNotificationPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().also { intent ->
                    intent.action = "android.settings.REQUEST_SCHEDULE_EXACT_ALARM"
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Shows a dialog to add a new task.
     */
    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskNameEditText = dialogView.findViewById<EditText>(R.id.taskNameEditText)
        val taskStartTimeTextView = dialogView.findViewById<TextView>(R.id.taskStartTimeTextView)
        val taskEndTimeTextView = dialogView.findViewById<TextView>(R.id.taskEndTimeTextView)
        val priorityRadioGroup = dialogView.findViewById<RadioGroup>(R.id.priorityRadioGroup)

        taskStartTimeTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, m ->
                taskStartTimeTextView.text = String.format(Locale.US, "%02d:%02d", h, m)
            }, hour, minute, true).show()
        }

        taskEndTimeTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, m ->
                taskEndTimeTextView.text = String.format(Locale.US, "%02d:%02d", h, m)
            }, hour, minute, true).show()
        }

        AlertDialog.Builder(this, R.style.Theme_KotlinExercise_1_Dialog)
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val taskName = taskNameEditText.text.toString()
                val taskStartTime = taskStartTimeTextView.text.toString()
                val taskEndTime = taskEndTimeTextView.text.toString()
                val selectedRadioButtonId = priorityRadioGroup.checkedRadioButtonId
                val selectedRadioButton = priorityRadioGroup.findViewById<RadioButton>(selectedRadioButtonId)
                val taskPriority = selectedRadioButton.text.toString()

                if (taskName.isNotBlank()) {
                    val newTask = Task(taskName, selectedDate, taskStartTime, taskEndTime, taskPriority)
                    allTasks.add(newTask)
                    saveTasks()
                    filterTasks(selectedDate)
                    scheduleNotification(newTask)
                    updateCalendarView()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows a dialog to update an existing task.
     */
    private fun showUpdateTaskDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val taskNameEditText = dialogView.findViewById<EditText>(R.id.taskNameEditText)
        val taskStartTimeTextView = dialogView.findViewById<TextView>(R.id.taskStartTimeTextView)
        val taskEndTimeTextView = dialogView.findViewById<TextView>(R.id.taskEndTimeTextView)
        val priorityRadioGroup = dialogView.findViewById<RadioGroup>(R.id.priorityRadioGroup)

        taskNameEditText.setText(task.name)
        taskStartTimeTextView.text = task.startTime
        taskEndTimeTextView.text = task.endTime

        when (task.priority) {
            "High" -> priorityRadioGroup.check(R.id.priorityHighRadioButton)
            "Medium" -> priorityRadioGroup.check(R.id.priorityMediumRadioButton)
            "Low" -> priorityRadioGroup.check(R.id.priorityLowRadioButton)
        }

        taskStartTimeTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, m ->
                taskStartTimeTextView.text = String.format(Locale.US, "%02d:%02d", h, m)
            }, hour, minute, true).show()
        }

        taskEndTimeTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, m ->
                taskEndTimeTextView.text = String.format(Locale.US, "%02d:%02d", h, m)
            }, hour, minute, true).show()
        }

        AlertDialog.Builder(this, R.style.Theme_KotlinExercise_1_Dialog)
            .setTitle("Update Task")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val taskName = taskNameEditText.text.toString()
                val taskStartTime = taskStartTimeTextView.text.toString()
                val taskEndTime = taskEndTimeTextView.text.toString()
                val selectedRadioButtonId = priorityRadioGroup.checkedRadioButtonId
                val selectedRadioButton = priorityRadioGroup.findViewById<RadioButton>(selectedRadioButtonId)
                val taskPriority = selectedRadioButton.text.toString()

                if (taskName.isNotBlank()) {
                    task.name = taskName
                    task.startTime = taskStartTime
                    task.endTime = taskEndTime
                    task.priority = taskPriority
                    saveTasks()
                    filterTasks(selectedDate)
                    scheduleNotification(task)
                    updateCalendarView()
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete") { _, _ ->
                allTasks.remove(task)
                saveTasks()
                filterTasks(selectedDate)
                updateCalendarView()
            }
            .show()
    }

    /**
     * Filters the tasks to show only the ones for the selected date.
     */
    private fun filterTasks(selectedDate: Long) {
        val filteredTasks = allTasks.filter { it.date >= getStartOfDay(selectedDate) && it.date < getEndOfDay(selectedDate) }
        taskAdapter.submitList(filteredTasks)
        if (filteredTasks.isEmpty()) {
            noTasksTextView.visibility = View.VISIBLE
            taskRecyclerView.visibility = View.GONE
        } else {
            noTasksTextView.visibility = View.GONE
            taskRecyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Returns the start of the day for the given date.
     */
    private fun getStartOfDay(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Returns the end of the day for the given date.
     */
    private fun getEndOfDay(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Saves the tasks to shared preferences.
     */
    private fun saveTasks() {
        val json = gson.toJson(allTasks)
        prefs.edit {
            putString("tasks", json)
        }
    }

    /**
     * Loads the tasks from shared preferences.
     */
    private fun loadTasks() {
        val json = prefs.getString("tasks", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Task>>() {}.type
            allTasks.clear()
            allTasks.addAll(gson.fromJson(json, type))
        } else {
            // Add some sample data if there are no saved tasks
            val today = Calendar.getInstance().timeInMillis
            allTasks.add(Task("Meeting with team", today, "10:00", "11:00", "High"))
            allTasks.add(Task("Work on project", today, "14:00", "16:00", "Medium"))
        }
        updateCalendarView()
    }

    private fun updateCalendarView() {
        val taskDates = allTasks.map { Date(it.date) }
        calendarView.setTaskDates(taskDates)
    }

    /**
     * Asks for notification permission on Android 13 and above.
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Creates a notification channel for task reminders.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminders"
            val descriptionText = "Channel for task reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("task_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules a notification for a task.
     */
    private fun scheduleNotification(task: Task) {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("task_name", task.name)
            putExtra("task_id", task.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(this, task.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val time = task.startTime.split(":")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = task.date
        calendar.set(Calendar.HOUR_OF_DAY, time[0].toInt())
        calendar.set(Calendar.MINUTE, time[1].toInt())
        calendar.set(Calendar.SECOND, 0)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        val endIntent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("task_name", "Task ending soon: ${task.name}")
            putExtra("task_id", task.hashCode() + 1)
        }

        val endPendingIntent = PendingIntent.getBroadcast(this, task.hashCode() + 1, endIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val endTime = task.endTime.split(":")
        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = task.date
        endCalendar.set(Calendar.HOUR_OF_DAY, endTime[0].toInt())
        endCalendar.set(Calendar.MINUTE, endTime[1].toInt())
        endCalendar.set(Calendar.SECOND, 0)
        endCalendar.add(Calendar.MINUTE, -10)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, endCalendar.timeInMillis, endPendingIntent)
    }
}
