package com.bignerdranch.android.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class TaskDetailViewModel : ViewModel() {

    private val crimeRepository = TaskRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    val taskLiveData: LiveData<Task?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }
    
    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId
    }

    fun saveCrime(task: Task) {
        crimeRepository.updateCrime(task)
    }
}