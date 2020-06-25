package com.example.bizhawkzreminderdemo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bizhawkzreminderdemo.data.ReminderData
import com.example.bizhawkzreminderdemo.data.ReminderDbHelper
import com.example.bizhawkzreminderdemo.data.SampleDataHelper
import com.example.bizhawkzreminderdemo.notif.NotificationHelper
import com.example.bizhawkzreminderdemo.reminder.ReminderDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), MainContracts.View, MainAdapter.Listener, ReminderDialog.OnCloseListener {

  private val delayTime = 500L


  private lateinit var handlerThread: HandlerThread
  private lateinit var backgroundHandler: Handler
  private lateinit var presenter: MainContracts.Presenter
  private lateinit var mainAdapter: MainAdapter

  private lateinit var textViewNoReminders: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var recyclerView: RecyclerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val mainContext = MainContext.INSTANCE
    mainContext.initialize(this)

    NotificationHelper.createNotificationChannel(this,
      NotificationManagerCompat.IMPORTANCE_MAX, true,
      getString(R.string.app_name), "App notification channel.")

    handlerThread = HandlerThread("BackgroundWorker")
    handlerThread.start()
    backgroundHandler = Handler(handlerThread.looper)

    val mainModel = MainModel(ReminderDbHelper())
    presenter = MainPresenter(this, mainModel)

    val toolbar: Toolbar = findViewById(R.id.toolbar)
    setSupportActionBar(toolbar)

    textViewNoReminders = findViewById(R.id.textViewNoReminders)
    progressBar = findViewById(R.id.progressBar)
    recyclerView = findViewById(R.id.recyclerViewReminders)
    val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    recyclerView.layoutManager = layoutManager
    recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

    val fabCreateReminder: FloatingActionButton = findViewById(R.id.fabCreateReminder)
    fabCreateReminder.setOnClickListener {
      backgroundHandler.post {
        presenter.createReminder()
      }
    }
  }

  override fun onResume() {
    super.onResume()

    loadData(delayTime)
  }

  override fun onDestroy() {
    super.onDestroy()
    handlerThread.quit()
    backgroundHandler.removeCallbacksAndMessages(null)
  }

  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    val menuItemManageChannels = menu.findItem(R.id.action_manage_channels)
    menuItemManageChannels.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId


  if (id == R.id.action_manage_channels) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
      }
    }

    return super.onOptionsItemSelected(item)
  }

  private fun deleteEverything() {
    displayDataDeletedNotification()
  }

  private fun displayDataDeletedNotification() {
    NotificationHelper.createSampleDataNotification(this@MainActivity,
        getString(R.string.sample_data_deleted_title),
        getString(R.string.sample_data_deleted_message),
        getString(R.string.sample_data_deleted_big_text), true)
  }

  override fun onClick(reminderData: ReminderData) {
    presenter.editReminder(reminderData)
  }

  override fun displayReminders(reminderList: List<ReminderData>) {
    runOnUiThread {
      recyclerView.visibility = View.VISIBLE
      textViewNoReminders.visibility = View.GONE
      mainAdapter = MainAdapter(this@MainActivity, reminderList)
      recyclerView.adapter = mainAdapter
      progressBar.visibility = View.GONE
      invalidateOptionsMenu()
    }
  }

  override fun displayEmptyState() {
    runOnUiThread {
      recyclerView.visibility = View.GONE
      textViewNoReminders.visibility = View.VISIBLE
      progressBar.visibility = View.GONE
      invalidateOptionsMenu()
    }
  }

  override fun displayCreateReminder() {
    //.. ReminderDialog for new reminders
    val reminderDialog = ReminderDialog.newInstance(null)
    reminderDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme)
    reminderDialog.show(supportFragmentManager, ReminderDialog.TAG)
  }

  override fun displayEditReminder(reminderData: ReminderData) {
    val args = Bundle()
    args.putParcelable(ReminderDialog.KEY_DATA, reminderData)
    val reminderDialog = ReminderDialog.newInstance(args)
    reminderDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme)
    reminderDialog.show(supportFragmentManager, ReminderDialog.TAG)
  }

  override fun displaySampleDataInserted(count: Int) {
    Snackbar.make(recyclerView, getString(R.string.snackbar_msg_sample_data_loaded, count), Snackbar.LENGTH_LONG)
        .setAction(getString(R.string.undo)) { backgroundHandler.post { deleteEverything() } }.show()

    backgroundHandler.post { presenter.scheduleAlarmsForSampleData(this@MainActivity) }
  }

  override fun onClose(opCode: Int, reminderData: ReminderData) {
    val message = when (opCode) {
      ReminderDialog.REMINDER_CREATED -> getString(R.string.created_reminder, reminderData.name)
      ReminderDialog.REMINDER_UPDATED -> getString(R.string.updated_reminder, reminderData.name)
      ReminderDialog.REMINDER_DELETED -> getString(R.string.deleted_reminder, reminderData.name)
      else -> getString(R.string.unknown_error)
    }
    Snackbar.make(recyclerView, message, Snackbar.LENGTH_SHORT).show()
    loadData(0L)
  }

  private fun loadData(delayMillis: Long) {
    progressBar.visibility = View.VISIBLE
    backgroundHandler.postDelayed({ presenter.start() }, delayMillis) //.. simulate load time
  }
}