package com.example.bizhawkzreminderdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.bizhawkzreminderdemo.data.DataUtils
import com.example.bizhawkzreminderdemo.notif.AlarmScheduler
import com.example.bizhawkzreminderdemo.reminder.ReminderDialog

/**
 * Handles any global application broadcasts.
 * <p>
 * Note: this really only handles the action from the
 * pet notification to administer the medicine but it could be used for any other actions.
 */
class AppGlobalReceiver : BroadcastReceiver() {

  companion object {
    const val NOTIFICATION_ID = "notification_id"
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    if (context != null && intent != null && intent.action != null) {

      // Handle the action to set the Medicine Administered
      if (intent.action!!.equals(context.getString(R.string.action_medicine_administered), ignoreCase = true)) {

        val extras = intent.extras
        if (extras != null) {

          val notificationId = extras.getInt(NOTIFICATION_ID)

          val reminderId = extras.getInt(ReminderDialog.KEY_ID)
          val medicineAdministered = extras.getBoolean(ReminderDialog.KEY_ADMINISTERED)

          // Lookup the reminder for sanity
          val reminderData = DataUtils.getReminderById(reminderId)

          if (reminderData != null) {

            // Update the database
            DataUtils.setMedicineAdministered(reminderId, medicineAdministered)

            // set the alarm
            AlarmScheduler.scheduleAlarmsForReminder(context, reminderData)
          }

          // finally, cancel the notification
          if (notificationId != -1) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(notificationId)
            notificationManager.cancelAll() // testing
          }
        }
      }
    }
  }
}