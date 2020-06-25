package com.example.bizhawkzreminderdemo

import android.content.Context
import com.example.bizhawkzreminderdemo.data.ReminderData

interface MainContracts {

  interface View {

    fun displayEmptyState()

    fun displayReminders(reminderList: List<ReminderData>)

    fun displayCreateReminder()

    fun displayEditReminder(reminderData: ReminderData)

    fun displaySampleDataInserted(count: Int)
  }

  interface Presenter {

    fun start()

    fun loadSampleData(sampleData: List<ReminderData>)

    fun createReminder()

    fun editReminder(reminderData: ReminderData)

    fun scheduleAlarmsForSampleData(context: Context)

  }

}