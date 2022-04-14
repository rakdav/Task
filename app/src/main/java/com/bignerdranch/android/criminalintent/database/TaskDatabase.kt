package com.bignerdranch.android.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.android.criminalintent.Task

@Database(entities = [ Task::class ], version=2)
@TypeConverters(TaskTypeConverters::class)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun crimeDao(): TaskDao
}