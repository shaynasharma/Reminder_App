package com.example.bizhawkzreminderdemo.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.example.bizhawkzreminderdemo.R
import com.example.bizhawkzreminderdemo.data.ReminderData
import com.example.bizhawkzreminderdemo.reminder.ReminderDialog
import java.util.*
import java.util.Calendar.*


/**
 * Helpers to assist in scheduling alarms for ReminderData.
 */
object AlarmScheduler {

  /**
   * Schedules all the alarms for [ReminderData].
   *
   * @param context      current application context
   * @param reminderData ReminderData to use for the alarm
   */
  fun scheduleAlarmsForReminder(context: Context, reminderData: ReminderData) {

    // get the AlarmManager reference
    val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Schedule the alarms based on the days to administer the medicine
    val days = context.resources.getStringArray(R.array.days)
    if (reminderData.days != null) {
      for (index in reminderData.days!!.indices) {

        val day = reminderData.days!![index]
        if (day != null) {
          var alarmIntent=createPendingIntent(context, reminderData, day);
          // get the PendingIntent for the alarm
          if(reminderData.snooze=="true") {
            alarmIntent = createPendingInstantIntent(context, reminderData, day)
          }

          // schedule the alarm
          val dayOfWeek = getDayOfWeek(days, day)

          scheduleAlarm(reminderData, dayOfWeek, alarmIntent, alarmMgr,context)
        }
      }
    }
  }

  /**
   * Schedules a single alarm
   */
  private fun scheduleAlarm(reminderData: ReminderData, dayOfWeek: Int, alarmIntent: PendingIntent?, alarmMgr: AlarmManager,context: Context) {
    val minutes:Int;
    // Set up the time to schedule the alarm
    val datetimeToAlarm = getInstance(Locale.getDefault())
    datetimeToAlarm.timeInMillis = System.currentTimeMillis()
    datetimeToAlarm.set(HOUR_OF_DAY, reminderData.hour)
    val minute = when (reminderData.snooze) {
      "true" -> {
        10;
      }
      "false" -> {
        reminderData.minute;
      }
      else -> {
        reminderData.minute;
      }
    }
    datetimeToAlarm.set(MINUTE, minute)
    datetimeToAlarm.set(SECOND, 0)
    datetimeToAlarm.set(MILLISECOND, 0)
    datetimeToAlarm.set(DAY_OF_WEEK, dayOfWeek)

    // Compare the datetimeToAlarm to today
    val today = getInstance(Locale.getDefault())
    if (shouldNotifyToday(dayOfWeek, today, datetimeToAlarm)) {

      // schedule for today
      alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
          datetimeToAlarm.timeInMillis, (1000 * 60 * 60 * 24 * 7).toLong(), alarmIntent)
      // Restart alarm if device is rebooted
      return
    }
    if (reminderData.snooze=="true") {
      Log.e("Alarm","reminderData.snooze : "+reminderData.snooze)
      registerAlarm(context,reminderData)
      // Restart alarm if device is rebooted
      return
    }
    Log.e("Alarm","reminderData not snooze : "+reminderData.snooze)
    // schedule 1 week out from the day
    datetimeToAlarm.roll(WEEK_OF_YEAR, 1)
    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
        datetimeToAlarm.timeInMillis, (1000 * 60 * 60 * 24 * 7).toLong(), alarmIntent)
  }

  /**
   * Creates a [PendingIntent] for the Alarm using the [ReminderData]
   *
   * @param context      current application context
   * @param reminderData ReminderData for the notification
   * @param day          String representation of the day
   */
  private fun createPendingIntent(context: Context, reminderData: ReminderData, day: String?): PendingIntent? {
    // create the intent using a unique type
    val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
      action = context.getString(R.string.action_notify_administer_medication)
      type = "$day-${reminderData.name}-${reminderData.name}-${reminderData.name}"
      putExtra(ReminderDialog.KEY_ID, reminderData.id)
    }

    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }
  private fun createPendingInstantIntent(context: Context, reminderData: ReminderData, day: String?): PendingIntent? {
    // create the intent using a unique type
    val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
      action = context.getString(R.string.action_notify_administer_medication)
      type = "$day-${reminderData.name}-${reminderData.name}-${reminderData.name}"
      putExtra(ReminderDialog.KEY_ID, reminderData.id)
    }

    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  fun registerAlarm(context: Context,reminderData: ReminderData) {
    val i = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
      action = context.getString(R.string.action_notify_administer_medication)
      putExtra(ReminderDialog.KEY_ID, reminderData.id)
    }
    val sender = PendingIntent.getBroadcast(context, 0, i, 0)
    // We want the alarm to go off 3 seconds from now.
    var firstTime = SystemClock.elapsedRealtime()
    firstTime += 3 * 1000.toLong() //start 3 seconds after first register.
    // Schedule the alarm!
    val am = context
      .getSystemService(ALARM_SERVICE) as AlarmManager
    am.setRepeating(
      AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
      600000, sender
    ) //10min interval
  }

  /**
   * Determines if the Alarm should be scheduled for today.
   *
   * @param dayOfWeek day of the week as an Int
   * @param today today's datetime
   * @param datetimeToAlarm Alarm's datetime
   */
  private fun shouldNotifyToday(dayOfWeek: Int, today: Calendar, datetimeToAlarm: Calendar): Boolean {
    return dayOfWeek == today.get(DAY_OF_WEEK) &&
        today.get(HOUR_OF_DAY) <= datetimeToAlarm.get(HOUR_OF_DAY) &&
        today.get(MINUTE) <= datetimeToAlarm.get(MINUTE)
  }

  /**
   * Updates a notification.
   * Note: this just calls [AlarmScheduler.scheduleAlarmsForReminder] since
   * alarms with exact matching pending intents will update if they are already set, otherwise
   * call [AlarmScheduler.removeAlarmsForReminder] if the medicine has been administered.
   *
   * @param context      current application context
   * @param reminderData ReminderData for the notification
   */
  fun updateAlarmsForReminder(context: Context, reminderData: ReminderData) {
    AlarmScheduler.scheduleAlarmsForReminder(context, reminderData)
  }

  /**
   * Removes the notification if it was previously scheduled.
   *
   * @param context      current application context
   * @param reminderData ReminderData for the notification
   */
  /**
   * Returns the int representation for the day of the week.
   *
   * @param days      array from resources
   * @param dayOfWeek String representation of the day e.g "Sunday"
   * @return [Calendar.DAY_OF_WEEK] for given dayOfWeek
   */
  private fun getDayOfWeek(days: Array<String>, dayOfWeek: String): Int {
    return when {
      dayOfWeek.equals(days[0], ignoreCase = true) -> SUNDAY
      dayOfWeek.equals(days[1], ignoreCase = true) -> MONDAY
      dayOfWeek.equals(days[2], ignoreCase = true) -> TUESDAY
      dayOfWeek.equals(days[3], ignoreCase = true) -> WEDNESDAY
      dayOfWeek.equals(days[4], ignoreCase = true) -> THURSDAY
      dayOfWeek.equals(days[5], ignoreCase = true) -> FRIDAY
      else -> SATURDAY
    }
  }

}