package com.example.bizhawkzreminderdemo.reminder

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.example.bizhawkzreminderdemo.R
import com.example.bizhawkzreminderdemo.data.ReminderData
import com.example.bizhawkzreminderdemo.data.ReminderDbHelper
import com.example.bizhawkzreminderdemo.notif.AlarmScheduler
import com.getbase.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class ReminderDialog : DialogFragment(), ReminderContracts.View{

  // Threading
  private var handlerThread: HandlerThread? = null
  private var backgroundHandler: Handler? = null
  private val foregroundHandler = Handler(Looper.getMainLooper())

  // Presenter
  private var presenter: ReminderContracts.Presenter? = null

  // Callback interface
  private var onCloseListener: OnCloseListener? = null

  // Views
  private var triggerActiveSnooze: Boolean = false
  private var toolbar: Toolbar? = null
  private var starred1fab: FloatingActionButton? = null
  private var starred2fab: FloatingActionButton? = null
  private var textInputName: EditText? = null
  private var buttonDate: Button? = null
  private var textInputDesc: TextInputEditText? = null
  private var buttonTime: Button? = null
  private var mSnooze: String = "null"
  private var linearLayoutDays: LinearLayout? = null
  private var fabSaveReminder: ImageView? = null

  interface OnCloseListener {
    fun onClose(opCode: Int, reminderData: ReminderData)
  }

  companion object {

    const val TAG = "ReminderDialog"

    // key for passed in data
    const val KEY_DATA = "reminder_data"
    const val KEY_ID = "id"
    const val KEY_ADMINISTERED = "administered"

    // opcodes for success
    const val REMINDER_CREATED = 0
    const val REMINDER_UPDATED = 1
    const val REMINDER_DELETED = 2

    // error states for validation
    const val ERROR_NO_NAME = 0
    const val ERROR_NO_MEDICINE = 1
    const val ERROR_NO_TIME = 2
    const val ERROR_NO_DAYS = 3
    const val ERROR_NO_DESC = 4
    const val ERROR_SAVE_FAILED = 5
    const val ERROR_DELETE_FAILED = 6
    const val ERROR_UPDATE_FAILED = 7
    private val mDateText: TextView? =
      null
    private var mYear =
      0
    private  var mMonth:Int = 0
    private  var mHour:Int = 0
    private  var mMinute:Int = 0
    private  var mDay:Int = 0
    private var mDate: String? = null

    fun newInstance(args: Bundle?): ReminderDialog {
      val reminderDialog = ReminderDialog()
      if (args != null) {
        reminderDialog.arguments = args
      }
      return reminderDialog
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    handlerThread = HandlerThread("ReminderBAckgroudn")
    handlerThread!!.start()
    backgroundHandler = Handler(handlerThread!!.looper)

    val args = arguments
    var reminderData: ReminderData? = null
    if (args != null) {
      reminderData = args.getParcelable(KEY_DATA)
    }
    val reminderDbHelper = ReminderDbHelper()
    val model = Model(reminderDbHelper, reminderData)

    presenter = Presenter(this, model)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_reminder_dialog, container, false)
    cacheViews(view)
    toolbar?.setNavigationOnClickListener(navigationListener)
    buttonDate?.setOnClickListener(dateListner)
    starred2fab?.setOnClickListener(fabStarred2)
    starred1fab?.setOnClickListener(fabStarred1)
    buttonTime?.setOnClickListener(timeListener)
//    starred1?.setOnClickListener {
//      starred1Activate(it)
//    }
//    starred2?.setOnClickListener {
//      starred2Activate(it)
//    }
    fabSaveReminder!!.setOnClickListener(saveListener)
    buildCheckBoxes()
    return view
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.let { super.onAttach(it) }
    try {
      onCloseListener = context as OnCloseListener?
    } catch (ex: ClassCastException) {
      throw RuntimeException("Parent must implement the ReminderDialog.OnCloseListener interface ")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    backgroundHandler!!.post { presenter!!.start() }
  }

  override fun onDestroy() {
    super.onDestroy()
    handlerThread!!.quit()
    foregroundHandler.removeCallbacksAndMessages(null)
    backgroundHandler!!.removeCallbacksAndMessages(null)
  }

  override fun displayExistingReminder(reminderData: ReminderData) {
    Log.e("displayExistingReminder", "displayExistingReminder > ${reminderData.snooze}")
    foregroundHandler.post {
      toolbar!!.title = getString(R.string.edit_reminder)
      textInputName!!.setText(reminderData.name)
      textInputDesc!!.setText(reminderData.desc)
      mSnooze= reminderData.snooze!!
      when(reminderData.snooze){
        "true"-> {
          triggerActiveSnooze=true
          starred2fab?.visibility=View.VISIBLE
          starred1fab?.visibility=View.GONE
        }
          "false"-> {
            triggerActiveSnooze=true
            starred1fab?.visibility=View.VISIBLE
            starred2fab?.visibility=View.GONE
          }
        else->{
          triggerActiveSnooze=true
          starred1fab?.visibility=View.VISIBLE
          starred2fab?.visibility=View.GONE
        }
      }
      setTimeButtonText(reminderData.hour, reminderData.minute)
      setupDaysCheckBoxes(reminderData)
    }
  }

  override fun displayTimeDialog(hour: Int, minute: Int) {
    val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
      Log.e("minute","minute : $minute")
      presenter!!.timeSelected(hourOfDay, minute)
      setTimeButtonText(hourOfDay, minute)
    }, hour, minute, false)
    timePickerDialog.show()
  }

  override fun displayError(errorCode: Int) {
    foregroundHandler.post {
      val message: String
      when (errorCode) {
        ERROR_NO_NAME -> message = getString(R.string.name_required)
        ERROR_NO_MEDICINE -> message = getString(R.string.medicine_required)
        ERROR_NO_DESC -> message = getString(R.string.desc_required)
        ERROR_NO_TIME -> message = getString(R.string.time_required)
        ERROR_NO_DAYS -> message = getString(R.string.days_required)
        ERROR_SAVE_FAILED -> message = getString(R.string.save_failed)
        ERROR_UPDATE_FAILED -> message = getString(R.string.update_failed)
        ERROR_DELETE_FAILED -> message = getString(R.string.delete_failed)
        else -> message = getString(R.string.unknown_error)
      }

      if (view != null) {
        Snackbar.make(view!!, message, Snackbar.LENGTH_SHORT).show()
      }
    }
  }

  override fun displayAdministerError() {
    if (view != null) {
      Snackbar.make(view!!, getString(R.string.cant_administer_medicine), Snackbar.LENGTH_SHORT).show()
    }
  }

  override fun close(opCode: Int, reminderData: ReminderData) {
    foregroundHandler.post {
      if (opCode == ReminderDialog.REMINDER_CREATED && context != null) {

        // schedule an alarm to notify later
        AlarmScheduler.scheduleAlarmsForReminder(context!!, reminderData)

      } else if (opCode == ReminderDialog.REMINDER_UPDATED && context != null) {
        // update the alarm
        AlarmScheduler.updateAlarmsForReminder(context!!, reminderData)
        // Create a new notification
      } else if (opCode == ReminderDialog.REMINDER_DELETED && context != null) {

        // remove alarm
        AlarmScheduler.scheduleAlarmsForReminder(context!!, reminderData)
      }

      onCloseListener!!.onClose(opCode, reminderData)
      dismiss()
    }
  }

  private fun cacheViews(view: View) {
    toolbar = view.findViewById(R.id.toolbarReminder)
    starred1fab = view.findViewById(R.id.starred1)
    starred2fab = view.findViewById(R.id.starred2)
    // Setup Toolbar
    textInputName = view.findViewById(R.id.textInputName)
    buttonDate = view.findViewById(R.id.buttonDate)
    textInputDesc = view.findViewById(R.id.textInputDesc)
    buttonTime = view.findViewById(R.id.buttonTime)
    linearLayoutDays = view.findViewById(R.id.linearLayoutDates)
    fabSaveReminder = view.findViewById(R.id.fabSaveReminder)
  }

  private fun buildCheckBoxes() {
    linearLayoutDays!!.removeAllViews()
    val days = resources.getStringArray(R.array.days)
    for (day in days) {
      val checkBox = CheckBox(context)
      checkBox.text = day
      linearLayoutDays!!.addView(checkBox)
    }
  }

  private fun setTimeButtonText(hourOfDay: Int, minute: Int) {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
    calendar.set(Calendar.MINUTE, minute)
    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    buttonTime!!.text = dateFormat.format(calendar.time)
  }

  private fun setupDaysCheckBoxes(reminderData: ReminderData) {
    for (i in 0 until linearLayoutDays!!.childCount) {
      if (linearLayoutDays!!.getChildAt(i) is CheckBox) {
        val checkBox = linearLayoutDays!!.getChildAt(i) as CheckBox
        for (j in reminderData.days!!.indices) {
          if (checkBox.text.toString().equals(reminderData.days!![j], ignoreCase = true)) {
            checkBox.isChecked = true
          }
        }
      }
    }
  }

  private val navigationListener = View.OnClickListener { dismiss() }
  private val fabStarred1 = View.OnClickListener {
    triggerActiveSnooze = toolbar?.title==getString(R.string.edit_reminder)
    Log.e("fabStarred1", "fabStarred1 clicked > triggerActiveSnooze : $triggerActiveSnooze")

    if(triggerActiveSnooze){
      if(it.id==R.id.starred1){
        mSnooze = "true"
        it.visibility=View.GONE
        starred2fab?.visibility=View.VISIBLE
        Log.e("fabStarred1","fabStarred1 clicked > starred2 found : "+mSnooze)
      }
    }

//    starred1Activate(view)
  }
  private val fabStarred2 = View.OnClickListener {
    triggerActiveSnooze = toolbar?.title==getString(R.string.edit_reminder)
    Log.e("fabStarred1", "fabStarred1 clicked > triggerActiveSnooze : $triggerActiveSnooze")

    if(triggerActiveSnooze) {
      if (it.id == R.id.starred2) {
        mSnooze = "false"
        it.visibility = View.GONE
        starred1fab?.visibility = View.VISIBLE
        Log.e("fabStarred2", "fabStarred2 clicked : " + mSnooze)
      }
    }
  }

  private val timeListener = View.OnClickListener { presenter!!.timeTapped() }
  private val dateListner = View.OnClickListener {
    Snackbar.make(view!!, "This Feature Comming Soon", Snackbar.LENGTH_SHORT).show()
  }

  private val administerListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked -> presenter!!.administeredTapped() }

  private val saveListener = View.OnClickListener {
    // Gather all the fields
    val name = textInputName!!.text!!.toString()
    val desc = textInputDesc!!.text!!.toString()
    val snooze = mSnooze

    Log.e("Reminder","name : $name\ndesc : $desc\nactive : $snooze")


    val daysItems = resources.getStringArray(R.array.days)

    for (i in 0 until linearLayoutDays!!.childCount) {
      if (linearLayoutDays!!.getChildAt(i) is CheckBox) {
        val checkBox = linearLayoutDays!!.getChildAt(i) as CheckBox
        if (!checkBox.isChecked) {
          daysItems[i] = null
        }
      }
    }
    backgroundHandler!!.post { presenter!!.saveTapped(name, desc, snooze,daysItems) }
  }
}

