package com.example.todoapp.network

import com.example.todoapp.model.dto.Todo
import com.example.todoapp.model.TodoTasks
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("todos")
    fun getTodoTasks(): Call<TodoTasks>

    @POST("todos/add")
    fun addTodo(@Body todo: Todo): Call<Todo>

    @PUT("todos/{id}")
    fun updateTodo(@Path("id") id: Int, @Body todo: Todo): Call<Todo>
}
