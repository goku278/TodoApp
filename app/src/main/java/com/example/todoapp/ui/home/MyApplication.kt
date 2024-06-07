package com.example.todoapp.ui.home

import android.app.Application
import com.example.todoapp.di.ApplicationComponent
import com.example.todoapp.R
import com.example.todoapp.di.DaggerApplicationComponent
import com.example.todoapp.di.NetworkModule
import io.realm.Realm

class MyApplication : Application() {
    private lateinit var retroComponent: ApplicationComponent
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        retroComponent = DaggerApplicationComponent.builder()
            .networkModule(NetworkModule())
            .build()

    }

    fun getRetroComponent(): ApplicationComponent {
        return retroComponent
    }
}