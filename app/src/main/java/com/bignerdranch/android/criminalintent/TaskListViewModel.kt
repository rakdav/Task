package com.bignerdranch.android.criminalintent

import androidx.lifecycle.ViewModel

class TaskListViewModel : ViewModel() {

    private val taskRepository = TaskRepository.get()
    val taskListLiveData = taskRepository.getCrimes()
}