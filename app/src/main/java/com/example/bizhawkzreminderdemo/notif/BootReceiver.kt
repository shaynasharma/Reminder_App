package com.example.bizhawkzreminderdemo.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.bizhawkzreminderdemo.data.DataUtils

/**
 * Receives the BOOT_COMPLETED action and schedules the alarms after reboot.
 */
class BootReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context?, intent: Intent?) {
    if (context != null && intent?.action.equals("android.intent.action.BOOT_COMPLETED")) {

      // Reschedule every alarm here
      DataUtils.scheduleAlarmsForData(context)
    }
  }
}