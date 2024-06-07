package com.example.todoapp.ui.home

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.adapter.TasksAdapter
import com.example.todoapp.model.Todos
import com.example.todoapp.model.dto.Todo
import com.example.todoapp.model.Calendar
import com.example.todoapp.adapter.MyAdapter
import io.realm.Realm
import com.example.todoapp.R
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.llAddMoreTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), Listener {
    private lateinit var calendarAdapter: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var pendingTaskList: ArrayList<Todos>
    private lateinit var viewModel: MainActivityViewModel
    var itemList = mutableListOf<Todos>() // Initialize your list here

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.blue)
        }


        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        setCalendarAdapter()


        llAddMoreTask?.setOnClickListener {
            showAddTaskDialog()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCalendarAdapter() {
        calendarAdapter = findViewById(R.id.rvCalendar)

        // Set up the LinearLayoutManager with horizontal orientation
        calendarAdapter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val calendarDates = generateCalendarDates()
        calendarDates.forEach { println("Day: ${it.day}, Date: ${it.date}") }

        adapter = MyAdapter(calendarDates, this)
        calendarAdapter.adapter = adapter

        initTasks()
    }

    private fun initTasks() {
        var todo = getAllTodosFromRealm()
        if (!todo.isNullOrEmpty()) {
            convertRealMTo(todo)
            setAdapter()
        } else {
            // Observe the LiveData
            viewModel.itemsList.observe(this, Observer { todoTasks ->
                // Call setAdapter() or a similar method here
                itemList = ArrayList()
                itemList = todoTasks.todos
                setAdapter()
            })
            viewModel.callApi()
        }
    }

    private fun convertRealMTo(todo: RealmResults<com.example.todoapp.model.localdb.Todos>): List<Todos> {
        itemList = ArrayList()
        for (i in todo.indices) {
            var todos = Todos()
            todos.id = todo[i]?.id
            todos.userId = todo[i]?.userId
            todos.todo = todo[i]?.todo
            todos.completed = todo[i]?.completed
            todos.isEditable = false
            itemList.add(todos)
        }
        return itemList
    }
    private fun convert(todos: Todos): com.example.todoapp.model.localdb.Todos {
        var todo: com.example.todoapp.model.localdb.Todos =
            com.example.todoapp.model.localdb.Todos()
        val realm = Realm.getDefaultInstance()
        if (todos.id == null) {
            val lastTodo =
                realm.where(com.example.todoapp.model.localdb.Todos::class.java).findAll()
                    .maxOfOrNull { it.id ?: 0 }
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

    // Method to fetch all data from Realm database
    private fun getAllTodosFromRealm(): RealmResults<com.example.todoapp.model.localdb.Todos>? {
        val realm = Realm.getDefaultInstance()
        return realm.where(com.example.todoapp.model.localdb.Todos::class.java).findAll()
    }

    fun updateTodoInRealm(
        todoId: com.example.todoapp.model.localdb.Todos,
        newTodo: String,
        newCompleted: Boolean
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            try {
                realm.executeTransaction { realm ->
                    val todo =
                        realm.where(com.example.todoapp.model.localdb.Todos::class.java)
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

    private fun deleteTodoFromRealm(todoId: com.example.todoapp.model.localdb.Todos) {
        GlobalScope.launch(Dispatchers.IO) {
            val realm = Realm.getDefaultInstance()
            try {
                realm.executeTransaction { realm ->
                    val todo =
                        realm.where(com.example.todoapp.model.localdb.Todos::class.java)
                            .equalTo("id", todoId.id)
                            .findFirst()
                    todo?.let {
                        it.deleteFromRealm()
                    }
                }
            } catch (e: Exception) {
                // Handle or report the error
            } finally {
                realm.close()
            }
        }
    }

    private fun setAdapter() {
        val adapter = TasksAdapter(itemList as ArrayList<Todos>, this, applicationContext)
        // Get reference to your RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.rvTasks)
        // Set layout manager for your RecyclerView (e.g., LinearLayoutManager)
        recyclerView.layoutManager = GridLayoutManager(applicationContext, 2)
        // Set the adapter on your RecyclerView
        recyclerView.adapter = adapter
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_add_task, null)
        builder.setView(dialogView)

        val etTodo = dialogView.findViewById<EditText>(R.id.etTodo)
        val cbCompleted = dialogView.findViewById<CheckBox>(R.id.cbCompleted)
        val etUserId = dialogView.findViewById<EditText>(R.id.etUserId)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        val alertDialog = builder.create()

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnOk.setOnClickListener {
            val todoDescription = etTodo.text.toString()
            val completed = cbCompleted.isChecked
            val userId = etUserId.text.toString().toIntOrNull()

            if (todoDescription.isNotEmpty() && userId != null) {
                val newTask = Todos(
                    todo = todoDescription,
                    completed = completed,
                    userId = userId,
                    isAdd = true
                )
                // adding the newTask in the realm db
                var t = convertToDto(newTask)
                var tt = convert(newTask)
                // sending a POST request to the remote server

                viewModel.itemsList.observe(this, Observer { todoTasks ->
                    if (todoTasks != null) {
                        itemList = todoTasks.todos
                        initTasks()
                    } else {
                        // Handle the case when todoTasks is null
                        // For example, show a message or a placeholder
                    }
                })

                viewModel.addDataToRemoteServer(newTask, t, applicationContext)

            }
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun convertToDto(newTask: Todos): Todo {
        val todo = Todo()
        todo.todo = newTask.todo
        todo.userId = newTask.userId
        todo.completed = newTask.completed
        return todo
    }

    override fun onBtClick(todos: Todos) {
        super.onBtClick(todos)
        if (todos.isEdit == true) {
            viewModel.itemsList.observe(this, Observer { todoTasks ->
                itemList = ArrayList()
                itemList = todoTasks.todos
                initTasks()
            })
            viewModel.updateTask(todos, applicationContext)
        }
        if (todos.isDelete == true) {
            var todo = convert(todos)
            deleteTodoFromRealm(todo)
        }
    }

    override fun onItemClick(
        calendar: Calendar,
        isSelected: Boolean,
        card: LinearLayout,
        day: TextView,
        date: TextView,
        position: Int
    ) {
        if (isSelected) {
            card.setBackgroundResource(R.drawable.back_orange)
            date.setTextColor(ContextCompat.getColor(this, R.color.white))
            day.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            card.setBackgroundResource(R.drawable.back_gray)
            date.setTextColor(ContextCompat.getColor(this, R.color.black))
            day.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateCalendarDates(): List<Calendar> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val calendarDates = mutableListOf<Calendar>()
        var currentDate = today
        val currentMonth = today.monthValue
        while (currentDate.monthValue == currentMonth) {
            var day = currentDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
            var date = currentDate.format(formatter)
            day = day.substring(0, 3)
            date = date.substring(date.length - 2)
            calendarDates.add(Calendar(day, date))
            currentDate = currentDate.plusDays(1)
        }
        return calendarDates
    }
}