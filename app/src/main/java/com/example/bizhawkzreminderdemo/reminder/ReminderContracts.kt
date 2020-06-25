package com.example.bizhawkzreminderdemo.reminder

import com.example.bizhawkzreminderdemo.data.ReminderData

interface ReminderContracts {

  interface View {

    fun displayExistingReminder(reminderData: ReminderData)

    fun displayTimeDialog(hour: Int, minute: Int)

    fun displayError(errorCode: Int)

    fun displayAdministerError()

    fun close(opCode: Int, reminderData: ReminderData)

  }

  interface Presenter {

    fun start()

    fun timeTapped()

    fun timeSelected(hourOfDay: Int, minute: Int)

    fun saveTapped(name: String, description: String,snooze : String,days: Array<String?>)

    fun deleteTapped()

    fun administeredTapped()
  }
}