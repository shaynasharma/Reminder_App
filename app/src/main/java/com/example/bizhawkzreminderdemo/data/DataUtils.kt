package com.example.bizhawkzreminderdemo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.bizhawkzreminderdemo.notif.AlarmScheduler
import java.util.*

/**
 * Contains static helper functions for the local database.
 */
object DataUtils {

  /**
   * Creates [ContentValues] from a [Cursor].
   *
   * @param reminderData ReminderData we want to insert values for
   * @return the ContentValues to insert
   */
  fun createContentValues(reminderData: ReminderData): ContentValues {

    return ContentValues().apply {
      put(ReminderContract.KEY_NAME, reminderData.name)
      put(ReminderContract.KEY_DESC, reminderData.desc)
      put(ReminderContract.KEY_SNOOZE, reminderData.snooze)
      put(ReminderContract.KEY_HOUR, reminderData.hour)
      put(ReminderContract.KEY_MINUTE, reminderData.minute)
      val stringBuilder: StringBuilder
      if (reminderData.days != null) {
        stringBuilder = StringBuilder()
        for (i in reminderData.days!!.indices) {
          if (reminderData.days!![i] == null) {
            continue
          }
          stringBuilder.append(reminderData.days!![i])
          if (i < reminderData.days!!.size - 1) {
            stringBuilder.append("-")
          }
        }
        put(ReminderContract.KEY_DAYS, stringBuilder.toString())
      }
    }
  }

  /**
   * Creates the [ReminderData] from a [Cursor].
   *
   * @param cursor the Cursor we want to read data from.
   * @return ReminderData
   */
  fun createReminderFromCursor(cursor: Cursor): ReminderData {
    return ReminderData().apply {
      id = cursor.getInt(cursor.getColumnIndex(ReminderContract.ID))
      name = cursor.getString(cursor.getColumnIndex(ReminderContract.KEY_NAME))
      desc = cursor.getString(cursor.getColumnIndex(ReminderContract.KEY_DESC))
      snooze = cursor.getString(cursor.getColumnIndex(ReminderContract.KEY_SNOOZE))
      hour = cursor.getInt(cursor.getColumnIndex(ReminderContract.KEY_HOUR))
      minute = cursor.getInt(cursor.getColumnIndex(ReminderContract.KEY_MINUTE))
      val daysLocal = cursor.getString(cursor.getColumnIndex(ReminderContract.KEY_DAYS))
      if (daysLocal != null) {
        days = daysLocal.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      }

    }
  }

  fun getReminderById(id: Int): ReminderData? {
    val sqLiteDatabase = ReminderDbHelper().writableDatabase
    val cursor = sqLiteDatabase.query(ReminderContract.TABLE_NAME, null, ReminderContract.ID + " =?",
        arrayOf(id.toString()), null, null, null, "1")
    var reminderData: ReminderData? = null
    if (cursor.moveToFirst()) {
      reminderData = DataUtils.createReminderFromCursor(cursor)
    }
    cursor.close()
    return reminderData
  }

  fun setMedicineAdministered(reminderId: Int, administered: Boolean) {
    val sqLiteDatabase = ReminderDbHelper().writableDatabase
    val contentValues = ContentValues()

    sqLiteDatabase.update(ReminderContract.TABLE_NAME, contentValues,
        ReminderContract.ID + " =?", arrayOf(reminderId.toString()))

  }

  /**
   * Schedules alarms for data in the local database that has not been administered.
   *
   * @param context current application context
   */
  fun scheduleAlarmsForData(context: Context) {
    val reminderDataList = DataUtils.loadUnAdministeredReminders()
    for (reminderData in reminderDataList) {
      AlarmScheduler.scheduleAlarmsForReminder(context, reminderData)
    }
  }

  /**
   * Deletes alarms for all the data in the local database.
   *
   * @param context current application context
   */

  /*
      Helper method to lookup the data in the local database to assist in scheduling the alarms.
  */
  private fun loadUnAdministeredReminders(): List<ReminderData> {

    return loadAllReminders()
//    return loadAllReminders(ReminderContract.KEY_ADMINISTERED + " = 0")
  }

  private fun loadAllReminders(): List<ReminderData> {

    val sqLiteDatabase = ReminderDbHelper().readableDatabase
    val cursor = sqLiteDatabase.query(ReminderContract.TABLE_NAME, null,
        null, null, null, null, null)
    val reminders = ArrayList<ReminderData>(cursor.count)
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast) {
        val reminderData = DataUtils.createReminderFromCursor(cursor)
        reminders.add(reminderData)
        cursor.moveToNext()
      }
    }
    cursor.close()

    return reminders
  }

}