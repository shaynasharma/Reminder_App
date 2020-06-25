package com.example.bizhawkzreminderdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bizhawkzreminderdemo.R
import com.example.bizhawkzreminderdemo.data.ReminderData
import java.text.SimpleDateFormat
import java.util.*

class MainAdapter(
    private val listener: Listener,
    private val reminderDataList: List<ReminderData>?
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

  private val dateFormat = SimpleDateFormat("h:mma",Locale.getDefault());

  interface Listener {
    fun onClick(reminderData: ReminderData)
  }

  override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
    val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_reminder_row, viewGroup, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
    if (reminderDataList != null) {
      val reminderData = reminderDataList[i]

      viewHolder.textViewName.text = reminderData.name
      viewHolder.textViewDesc.text = reminderData.desc
      when(reminderData.snooze){
        "true"-> {
          viewHolder.imageViewNotification.setImageResource(R.drawable.ic_notifications_on_white)
        }
        "false"-> {
          viewHolder.imageViewNotification.setImageResource(R.drawable.ic_notifications_off_grey)
        }
        else->{
          viewHolder.imageViewNotification.setImageResource(R.drawable.ic_notifications_off_grey)
        }
      }
      val date = Calendar.getInstance()
      date.set(Calendar.HOUR_OF_DAY, reminderData.hour)
      date.set(Calendar.MINUTE, reminderData.minute)

      viewHolder.textViewTimeToAdminister.text = dateFormat.format(date.time).toLowerCase(Locale.getDefault())

      var daysText = Arrays.toString(reminderData.days)
      daysText = daysText.replace("[", "")
      daysText = daysText.replace("]", "")
      daysText = daysText.replace(",", " ·")
      viewHolder.textViewDays.text = daysText

      viewHolder.itemView.setOnClickListener {
        listener.onClick(reminderData)
      }

    }
  }

  override fun getItemCount(): Int {
    return reminderDataList?.size ?: 0
  }


  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var textViewName: TextView = itemView.findViewById(R.id.textViewName)
    var textViewDesc: TextView = itemView.findViewById(R.id.textViewDesc)
    var imageViewNotification: ImageView = itemView.findViewById(R.id.imageViewNotification)
    var textViewTimeToAdminister: TextView = itemView.findViewById(R.id.textViewTimeToAdminister)
    var textViewDays: TextView = itemView.findViewById(R.id.textViewDays)

  }
}