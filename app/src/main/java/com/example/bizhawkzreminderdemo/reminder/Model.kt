package com.example.bizhawkzreminderdemo.reminder

import com.example.bizhawkzreminderdemo.data.DataUtils
import com.example.bizhawkzreminderdemo.data.ReminderContract
import com.example.bizhawkzreminderdemo.data.ReminderData
import com.example.bizhawkzreminderdemo.data.ReminderDbHelper

class Model(
    private val reminderDbHelper: ReminderDbHelper,
    private var reminderData: ReminderData? = null
) {

  var editing = reminderData != null

  fun getReminderData(): ReminderData? {
    return reminderData
  }

  fun getIsEditing(): Boolean {
    return editing
  }

  fun setHourAndMinute(hourOfDay: Int, minute: Int) {
    if (reminderData == null) {
      reminderData = ReminderData()
    }
    reminderData?.hour = hourOfDay
    reminderData?.minute = minute
  }

  // the row ID of the newly inserted row, or -1 if an error occurred
  fun createReminder(name: String,
                     desc: String,snooze:String, days: Array<String?>?): Long {

    reminderData?.name = name
    reminderData?.desc = desc
    reminderData?.snooze = snooze
    reminderData?.days = days

    val sqLiteDatabase = reminderDbHelper.writableDatabase
    val contentValues = DataUtils.createContentValues(reminderData as ReminderData)
    val rowId = sqLiteDatabase.insert(ReminderContract.TABLE_NAME, null, contentValues)

    reminderData?.id = rowId.toInt()

    return rowId
  }

  // returns number of rows affected
  fun updateReminder(name: String,
                     desc: String,snooze: String, days: Array<String?>?): Int {

    reminderData?.name = name
    reminderData?.desc = desc
    reminderData?.snooze = snooze
    reminderData?.days = days

    val sqLiteDatabase = reminderDbHelper.writableDatabase
    val contentValues = DataUtils.createContentValues(reminderData as ReminderData)
    return sqLiteDatabase.update(ReminderContract.TABLE_NAME, contentValues, ReminderContract.ID + " =? ",
        arrayOf(reminderData?.id.toString()))
  }


  // returns number of rows affected
  fun deleteReminder(id: Int): Int {
    val sqLiteDatabase = reminderDbHelper.writableDatabase
    return sqLiteDatabase.delete(ReminderContract.TABLE_NAME, ReminderContract.ID + " =? ",
        arrayOf(id.toString()))
  }
}