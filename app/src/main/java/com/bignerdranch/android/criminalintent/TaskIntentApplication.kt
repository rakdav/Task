package com.bignerdranch.android.criminalintent

import android.app.Application

class TaskIntentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)
    }
}