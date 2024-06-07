package com.example.todoapp.model

import com.google.gson.annotations.SerializedName

data class Todos(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("todo") var todo: String? = null,
    @SerializedName("completed") var completed: Boolean? = null,
    @SerializedName("userId") var userId: Int? = null,
    var isEditable: Boolean? = false,
    var isEdit: Boolean? = false,
    var isAdd: Boolean? = false,
    var isDelete: Boolean? = false
)