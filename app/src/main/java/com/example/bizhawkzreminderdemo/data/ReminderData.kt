package com.example.bizhawkzreminderdemo.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Data representation for a reminder we schedule and issue notifications for.
 */
data class ReminderData(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("note")
    var note: String? = null,
    @SerializedName("desc")
    var desc: String? = null,
    @SerializedName("snooze")
    var snooze: String? = null,
    @SerializedName("hour")
    var hour: Int = 0,
    @SerializedName("minute")
    var minute: Int = 0,
    @SerializedName("days")
    var days: Array<String?>? = null
) : Parcelable {

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.apply {
      writeInt(id)
      writeString(name)
      writeString(note)
      writeString(desc)
      writeString(snooze)
      writeInt(hour)
      writeInt(minute)
      writeStringArray(days)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ReminderData

    if (id != other.id) return false
    if (name != other.name) return false
    if (note != other.note) return false
    if (desc != other.desc) return false
      if (snooze != other.snooze) return false
    if (hour != other.hour) return false
    if (minute != other.minute) return false
    if (days != null) {
      if (other.days == null) return false
      if (!(days as Array).contentEquals(other.days as Array<out String>)) return false
    } else if (other.days != null) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id
    result = 31 * result + (name?.hashCode() ?: 0)
    result = 31 * result + (note?.hashCode() ?: 0)
    result = 31 * result + (desc?.hashCode() ?: 0)
    result = 31 * result + (snooze?.hashCode() ?: 0)
    result = 31 * result + hour
    result = 31 * result + minute
    result = 31 * result + (days?.contentHashCode() ?: 0)
    return result
  }

  companion object CREATOR : Parcelable.Creator<ReminderData> {
    override fun createFromParcel(source: Parcel): ReminderData {
      return ReminderData().apply {
        id = source.readInt()
        name = source.readString()
        note = source.readString()
        desc = source.readString()
        snooze = source.readString()
        hour = source.readInt()
        minute = source.readInt()
        source.readStringArray(days)
      }
    }

    override fun newArray(size: Int): Array<ReminderData?> {
      return arrayOfNulls(size)
    }
  }


}