package com.example.todoapp.ui.home

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.todoapp.model.Todos
import com.example.todoapp.model.dto.Todo
import com.example.todoapp.network.ApiService
import com.example.todoapp.model.TodoTasks
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {
    @Inject
    lateinit var apiService: ApiService

    private val _itemsList = MutableLiveData<TodoTasks>()
    var itemsList: LiveData<TodoTasks> get() = _itemsList

    init {
        (application as MyApplication).getRetroComponent().inject(this)
        itemsList = MutableLiveData()
    }

    fun getLiveDataListObserver(): MutableLiveData<TodoTasks> {
        return _itemsList
    }

    private fun convert(todos: Todos): com.example.todoapp.model.localdb.Todos {
        var todo: com.example.todoapp.model.localdb.Todos =
            com.example.todoapp.model.localdb.Todos()
        val realm = Realm.getDefaultInstance()
        if (todos.id == null) {
            val lastTodo = realm.where(com.example.todoapp.model.localdb.Todos::class.java).findAll().maxOfOrNull { it.id ?: 0 }
            val nextId = (lastTodo ?: 0) + 1
            todo.id = nextId
        } else {
            todo.id = todos.id
        }
        todo.completed = todos.completed
        todo.todo = todos.todo
        todo.userId = todos.userId
        realm.close()
        return todo
    }

    fun addTodoToRealm(todo: com.example.todoapp.model.localdb.Todos) {
        GlobalScope.launch(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransactionAsync { realm ->
                realm.copyToRealm(todo)
            }
        }
    }



    fun callApi() {
        // Your existing code...
        val call = apiService.getTodoTasks()
        call.enqueue(object : Callback<TodoTasks> {
            override fun onResponse(call: Call<TodoTasks>, response: Response<TodoTasks>) {
                if (response.isSuccessful) {
                    val todoTasks = response.body()
                    Log.d("MainActivity", "todoTasks => $todoTasks")
                    for (i in todoTasks!!.todos.indices) {
                        var todo = convert(todoTasks.todos[i])
                        addTodoToRealm(todo)
                        var todoo = convertToDao(todoTasks.todos[i])
                        GlobalScope.launch(Dispatchers.Main) {
                            _itemsList.value?.todos?.add(todoo)
                            _itemsList.value = _itemsList.value // Trigger LiveData update
                        }
                    }
                } else {
                    // Handle errors
                    Log.d("MainActivity", "fetch all tasks error, reason is  => ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TodoTasks>, t: Throwable) {
                // Handle failures
                Log.d("MainActivity", "fetch all tasks error, reason is  => ${t.message}")
            }
        })
    }

    private fun convertToDto(newTask: Todos): Todo {
        val todo = Todo()
        todo.todo = newTask.todo
        todo.userId = newTask.userId
        todo.completed = newTask.completed
        return todo
    }

    fun updateTask(todos: Todos, context: Context) {
        var todo = convert(todos)
        var todoo = convertToDto(todos)
        todos.id?.let {
            apiService.updateTodo(it, todoo).enqueue(object : Callback<Todo> {
                override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                    if (response.isSuccessful) {
                        // Handle the response
                        val todoResponse = response.body()
                        Log.d("MainActivity", "After editing data in the remote server, response is => ${todoResponse.toString()}")
                        Toast.makeText(context, "Updated an existing task in the remote server", Toast.LENGTH_SHORT).show()
                    } else {
                        // Handle the error
                        Toast.makeText(context, "Server failure due to ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Todo>, t: Throwable) {
                    // Handle the failure
                    Log.d("MainActivity", "Editing data in the remote server failure is => ${t.message}")
                }
            })
        }
        todo.todo?.let { todo.completed?.let { it1 -> updateTodoInRealm(todo, it, it1) } }
    }
    /*fun addDataToRemoteServer(todos: Todos, todo: Todo, context: Context) {
        // Create an instance of the API service
        var tt = convert(todos)

        apiService.addTodo(todo).enqueue(object : Callback<Todo> {
            override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                if (response.isSuccessful) {
                    // Handle the response
                    val todoResponse = response.body()
                    Log.d("MainActivity", "After adding data in the remote server, response is => ${todoResponse.toString()}")
                    Toast.makeText(context, "Added a new task in the remote server", Toast.LENGTH_SHORT).show()
                    var t = convertToModel(todoResponse)
                    _itemsList.value?.todos = _itemsList.value?.todos?.toMutableList()?.apply { add(t) } as List<Todos>
                    _itemsList.postValue(_itemsList.value) // Trigger LiveData update
                    addTodoToRealm(tt)
                } else {
                    // Handle the error
                }
            }

            override fun onFailure(call: Call<Todo>, t: Throwable) {
                // Handle the failure
            }
        })
    }
*/
  /*  fun addDataToRemoteServer(todos: Todos, todo: Todo, context: Context) {
        // Create an instance of the API service
        var tt = convert(todos)

        apiService.addTodo(todo).enqueue(object : Callback<Todo> {
            override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                if (response.isSuccessful) {
                    // Handle the response
                    val todoResponse = response.body()
                    Log.d("MainActivity", "After adding data in the remote server, response is => ${todoResponse.toString()}")
                    Toast.makeText(context, "Added a new task in the remote server", Toast.LENGTH_SHORT).show()
                    var t = convertToModel(todoResponse)
                    _itemsList.value?.todos = _itemsList.value?.todos?.toMutableList()?.apply { add(t) } as ArrayList<Todos>
                    _itemsList.postValue(_itemsList.value) // Trigger LiveData update
                    addTodoToRealm(tt)
                } else {
                    // Handle the error
                }
            }

            override fun onFailure(call: Call<Todo>, t: Throwable) {
                // Handle the failure
            }
        })
    }*/

    fun addDataToRemoteServer(todos: Todos, todo: Todo, context: Context) {
        // Create an instance of the API service
        var tt = convert(todos)

        apiService.addTodo(todo).enqueue(object : Callback<Todo> {
            override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                if (response.isSuccessful) {
                    // Handle the response
                    val todoResponse = response.body()
                    Log.d("MainActivity", "After adding data in the remote server, response is => ${todoResponse.toString()}")
                    Toast.makeText(context, "Added a new task in the remote server", Toast.LENGTH_SHORT).show()
                    var t = convertToModel(todoResponse)
                    if (_itemsList.value == null) {
                        _itemsList.value = TodoTasks(todos = arrayListOf(t))
                    } else {
                        _itemsList.value?.todos?.add(t)
                    }
                    _itemsList.postValue(_itemsList.value) // Trigger LiveData update
                    addTodoToRealm(tt)
                } else {
                    // Handle the error
                }
            }

            override fun onFailure(call: Call<Todo>, t: Throwable) {
                // Handle the failure
            }
        })
    }

    private fun convertToModel(todoResponse: Todo?): Todos {
        var todos = Todos()
        todos.id = todoResponse?.id
        todos.userId = todoResponse?.userId
        todos.completed = todoResponse?.completed
        todos.todo = todoResponse?.todo
        return todos
    }
    private fun updateTodoInRealm(todoId: com.example.todoapp.model.localdb.Todos, newTodo: String, newCompleted: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            try {
                realm.executeTransaction { realm ->
                    val todo = realm.where(com.example.todoapp.model.localdb.Todos::class.java)
                        .equalTo("id", todoId.id)
                        .findFirst()
                    todo?.let {
                        it.todo = newTodo
                        it.completed = newCompleted
                    }
                }
            } catch (e: Exception) {
                // Handle or report the error
                Log.d("MainActivity", "error is => ${e.message}")
            } finally {
//                btSubmit.isVisible = false
                realm.close()
            }
        }
    }
    private fun convertToDao(todos: Todos): Todos {
        var todo = Todos()
        todo.id = todos.id
        todo.completed = todos.completed
        todo.userId = todos.userId
        todo.todo = todos.todo
        return todo
    }


}