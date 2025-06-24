package com.UASMP.purplenote

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, resetting alarms...")

            val activityList = StorageHelper.loadActivityList(context)

            for (activity in activityList) {
                val dateParts = activity.date.split("-")
                val timeParts = activity.time.split(":")

                if (dateParts.size != 3 || timeParts.size != 2) continue

                val day = dateParts[0].toIntOrNull() ?: continue
                val month = dateParts[1].toIntOrNull()?.minus(1) ?: continue
                val year = dateParts[2].toIntOrNull() ?: continue
                val hour = timeParts[0].toIntOrNull() ?: continue
                val minute = timeParts[1].toIntOrNull() ?: continue

                val calendar = Calendar.getInstance().apply {
                    set(year, month, day, hour, minute, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MINUTE, -10)
                }

                if (calendar.before(Calendar.getInstance())) continue

                val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("title", "Pengingat Kegiatan")
                    putExtra("message", activity.activity)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (activity.date + activity.time).hashCode(),
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        }
    }
}
