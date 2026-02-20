package com.example.kotlinexercise_1

/**
 * Represents a single task with a name, date, start time, end time, and priority.
 */
data class Task(var name: String, val date: Long, var startTime: String, var endTime: String, var priority: String = "Low")
