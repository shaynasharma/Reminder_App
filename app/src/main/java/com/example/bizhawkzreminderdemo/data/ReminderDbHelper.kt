package com.example.bizhawkzreminderdemo.data

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.bizhawkzreminderdemo.MainContext

class ReminderDbHelper : SQLiteOpenHelper(MainContext.INSTANCE.context, DATABASE_NAME, null, DATABASE_VERSION) {

  companion object {
    const val DATABASE_NAME = "ReminderDatabase.db"
    const val DATABASE_VERSION = 1
  }

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(ReminderContract.CREATE_TABLE)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS " + ReminderContract.TABLE_NAME)
    onCreate(db)
  }
}