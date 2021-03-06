package com.bignerdranch.android.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.criminalintent.database.TaskDatabase
import com.bignerdranch.android.criminalintent.database.migration_2_3
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "task-database"

class TaskRepository private constructor(context: Context) {

    private val database : TaskDatabase = Room.databaseBuilder(
        context.applicationContext,
        TaskDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_2_3).build()
    private val crimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir=context.applicationContext.filesDir

    fun getCrimes(): LiveData<List<Task>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Task?> = crimeDao.getCrime(id)

    fun updateCrime(task: Task) {
        executor.execute {
            crimeDao.updateCrime(task)
        }
    }

    fun addCrime(task: Task) {
        executor.execute {
            crimeDao.addCrime(task)
        }
    }
    fun getPhotoFile(task:Task):
            File=File(filesDir,task.PhotoFileName)

    companion object {
        private var INSTANCE: TaskRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = TaskRepository(context)
            }
        }

        fun get(): TaskRepository {
            return INSTANCE ?:
            throw IllegalStateException("TaskRepository must be initialized")
        }
    }
}