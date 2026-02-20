package com.example.kotlinexercise_1

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * A broadcast receiver that receives and displays task reminder notifications.
 */
class NotificationReceiver : BroadcastReceiver() {

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val taskName = intent.getStringExtra("task_name")
        val taskId = intent.getIntExtra("task_id", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification
        val notification = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(taskName)
            .setContentText("Your task is starting now.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Show the notification
        notificationManager.notify(taskId, notification)
    }
}
