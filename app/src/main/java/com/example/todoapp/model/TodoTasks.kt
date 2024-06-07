package com.example.todoapp.model

import com.example.todoapp.model.Todos
import com.google.gson.annotations.SerializedName


data class TodoTasks(
    @SerializedName("todos") var todos: ArrayList<Todos> = arrayListOf(),
    @SerializedName("total") var total: Int? = null,
    @SerializedName("skip") var skip: Int? = null,
    @SerializedName("limit") var limit: Int? = null
)