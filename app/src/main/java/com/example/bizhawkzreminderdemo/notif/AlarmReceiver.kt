package com.example.bizhawkzreminderdemo.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.bizhawkzreminderdemo.R
import com.example.bizhawkzreminderdemo.data.DataUtils
import com.example.bizhawkzreminderdemo.reminder.ReminderDialog

class AlarmReceiver : BroadcastReceiver() {

  private val TAG = AlarmReceiver::class.java.simpleName

  override fun onReceive(context: Context?, intent: Intent?) {
    Log.d(TAG, "onReceive() called with: context = [$context], intent = [$intent]")

    if (context != null && intent != null && intent.action != null) {
      Log.d(TAG, "onReceive() called with: action = ${intent.action}")
      val reminderData = DataUtils.getReminderById(intent.extras!!.getInt(ReminderDialog.KEY_ID))
      if (reminderData != null) {
        Log.d(TAG, "onReceive() called with: reminderData = ${reminderData}")
        NotificationHelper.createNotificationForPet(context, reminderData)
      }else{
        Log.d(TAG, "onReceive() called with: reminderData empty = ${reminderData}")
      }
//      if (intent.action!!.equals(context.getString(R.string.action_notify_administer_medication), ignoreCase = true)) {
//        if (intent.extras != null) {
//          val reminderData = DataUtils.getReminderById(intent.extras!!.getInt(ReminderDialog.KEY_ID))
//          if (reminderData != null) {
//            NotificationHelper.createNotificationForPet(context, reminderData)
//          }
//        }
//      }
    }
  }
}