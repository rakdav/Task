package com.bignerdranch.android.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.bignerdranch.android.criminalintent.Task

@Database(entities = [ Task::class ], version=3)
@TypeConverters(TaskTypeConverters::class)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun crimeDao(): TaskDao
}
val migration_2_3=object :Migration(2,3){
    override fun migrate(database: SupportSQLiteDatabase){
        database.execSQL("ALTER TABLE task ADD COLUMN suspect TEXT NOT NULL DEFAULT ''");
    }
}