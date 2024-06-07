package com.example.todoapp.ui.home

import android.widget.LinearLayout
import android.widget.TextView
import com.example.todoapp.model.Todos
import com.example.todoapp.model.Calendar

interface Listener {
    fun onItemClick(calendar: Calendar, isSelected: Boolean, card: LinearLayout, day: TextView, date: TextView, position: Int)

//    fun onItemClick(calendar: Calendar, isSelected: Boolean)

    fun onBtClick(todos: Todos) {}
}