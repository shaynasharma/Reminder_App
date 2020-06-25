package com.example.bizhawkzreminderdemo

import android.content.Context
import com.example.bizhawkzreminderdemo.data.DataUtils
import com.example.bizhawkzreminderdemo.data.ReminderData

class MainPresenter(
    private val view: MainContracts.View,
    private val mainModel: MainModel
) : MainContracts.Presenter {
  override fun start() {
    val reminderDataList = mainModel.loadAllReminders()
    if (!reminderDataList.isNullOrEmpty()) {
      view.displayReminders(reminderDataList)
    } else {
      view.displayEmptyState()
    }
  }

  override fun loadSampleData(sampleData: List<ReminderData>) {
    view.displaySampleDataInserted(mainModel.insertSampleReminders(sampleData))
    //.. call start after sample data inserted
    start()
  }

  override fun createReminder() {
    view.displayCreateReminder()
  }

  override fun editReminder(reminderData: ReminderData) {
    view.displayEditReminder(reminderData)
  }

  override fun scheduleAlarmsForSampleData(context: Context) {
    DataUtils.scheduleAlarmsForData(context)
  }

}