package com.example.bizhawkzreminderdemo.reminder

import android.view.View
import com.example.bizhawkzreminderdemo.data.ReminderData
import java.util.*

class Presenter(
    private val view: ReminderContracts.View,
    private val model: Model
) : ReminderContracts.Presenter {

  override fun start() {
    if (model.getReminderData() != null) {
      view.displayExistingReminder(model.getReminderData()!!)
    }
  }

  override fun timeTapped() {
    val reminderData = model.getReminderData()
    if (reminderData != null) {
      view.displayTimeDialog(reminderData.hour, reminderData.minute)
    } else {
      val date = Calendar.getInstance()
      val hour = date.get(Calendar.HOUR_OF_DAY)
      var minute = date.get(Calendar.MINUTE)
      minute += when(reminderData?.snooze){
          "true"-> {
            10;
          }
          "false"-> {
            0;
          }
          else->{
            0;
          }
      }
      view.displayTimeDialog(hour, minute)
    }
  }

  override fun timeSelected(hourOfDay: Int, minute: Int) {
    model.setHourAndMinute(hourOfDay, minute)
  }
  override fun saveTapped(name: String, description: String,snooze: String, days: Array<String?>) {

    //.. required field error checking
    if (name.trim().isEmpty()) {
      view.displayError(ReminderDialog.ERROR_NO_NAME)
      return
    } else if (description.trim().isEmpty()) {
      view.displayError(ReminderDialog.ERROR_NO_DESC)
      return
    } else if (model.getReminderData() == null) {
      view.displayError(ReminderDialog.ERROR_NO_TIME)
      return
    } else if (!validDays(days)) {
      view.displayError(ReminderDialog.ERROR_NO_DAYS)
      return
    }

    //.. handle save or update operation
    if (model.getIsEditing()) {
      val affectedRows = model.updateReminder(name, description,snooze, days)
      if (affectedRows > 0) {
        view.close(ReminderDialog.REMINDER_UPDATED, model.getReminderData()!!)
      } else {
        view.displayError(ReminderDialog.ERROR_UPDATE_FAILED)
      }
    } else {
      val rowId = model.createReminder(name, description,snooze,days)
      if (rowId > -1) {
        view.close(ReminderDialog.REMINDER_CREATED, model.getReminderData()!!)
      } else {
        view.displayError(ReminderDialog.ERROR_SAVE_FAILED)
      }
    }
  }

  override fun deleteTapped() {
    val reminderData = model.getReminderData() ?: return
    val rowsAffected = model.deleteReminder(reminderData.id)
    if (rowsAffected > 0) {
      view.close(ReminderDialog.REMINDER_DELETED, reminderData)
    } else {
      view.displayError(ReminderDialog.ERROR_DELETE_FAILED)
    }

  }

  override fun administeredTapped() {
    if (!model.getIsEditing()) {
      view.displayAdministerError()
    }
  }

  private fun validDays(days: Array<String?>): Boolean {
    for (day in days) {
      if (day != null) {
        return true
      }
    }
    return false
  }
}