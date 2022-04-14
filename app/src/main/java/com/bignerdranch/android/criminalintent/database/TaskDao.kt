package com.bignerdranch.android.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.criminalintent.Task
import java.util.*

@Dao
interface TaskDao {

    @Query("SELECT * FROM task")
    fun getCrimes(): LiveData<List<Task>>

    @Query("SELECT * FROM task WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Task?>

    @Update
    fun updateCrime(task: Task)

    @Insert
    fun addCrime(task: Task)
}