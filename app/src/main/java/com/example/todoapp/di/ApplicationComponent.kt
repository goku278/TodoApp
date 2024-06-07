package com.example.todoapp.di

import com.example.todoapp.ui.home.MainActivityViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface ApplicationComponent {
    fun inject(mainActivityViewModel: MainActivityViewModel)
}