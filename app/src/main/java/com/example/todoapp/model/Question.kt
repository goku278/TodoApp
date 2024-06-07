package com.example.todoapp.model
data class Question(
    val id: Int,
    val question: String,
    val imageResourceId: Int,
    val options: List<String>,
    val correctOptionIndex: Int
)