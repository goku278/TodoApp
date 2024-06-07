package com.example.todoapp.model.localdb

import io.realm.RealmObject

open class Todos(
    var id: Int? = null,
    var todo: String? = null,
    var completed: Boolean? = null,
    var userId: Int? = null,
    /*var isEdit: Boolean? = false,
    var isAdd: Boolean? = false*/
): RealmObject()