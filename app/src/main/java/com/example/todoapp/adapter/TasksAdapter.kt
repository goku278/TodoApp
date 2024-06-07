package com.example.todoapp.adapter

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.model.Todos
import com.example.todoapp.ui.home.Listener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TasksAdapter(private val itemList: ArrayList<Todos>,
                   private val listener: Listener,
                   private val context: Context) :
    RecyclerView.Adapter<TasksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tasks_list, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private var todoTask: TextView = itemView.findViewById(R.id.textViewTodo)
        private var menu: ImageView = itemView.findViewById(R.id.ivMenu)
        private var btSubmit: Button = itemView.findViewById(R.id.btSubmit)

        init {
            val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake_animation)
            val mediaPlayer = MediaPlayer.create(context, R.raw.click_sound)

            cardView.setOnTouchListener { _, _ ->
                cardView.startAnimation(shakeAnimation)
                mediaPlayer.start()
                true
            }

            menu.setOnClickListener {
                showOptionsDialog(
                    itemView.context, adapterPosition,
                    editCallback = {
                        itemList[adapterPosition].isEditable = !itemList[adapterPosition].isEditable!!
                        notifyItemChanged(adapterPosition)
                    },
                    deleteCallback = {
                        // Handle delete task
                        deleteTask(adapterPosition, itemList[adapterPosition])
                        notifyItemChanged(adapterPosition)
                    }
                )
                true
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: Todos, position: Int) {
            val backgroundColor = getCardColor(position)
            cardView.setBackgroundResource(backgroundColor)
            if (!item.todo.isNullOrEmpty()) {
                todoTask.text = item.todo
            }
            todoTask.isEnabled = item.isEditable == true
            todoTask.isFocusable = true
            btSubmit.visibility = if (item.isEditable == true) View.VISIBLE else View.GONE

            btSubmit.setOnClickListener {
                item.isEdit = true
                item.isAdd = false
                item.isDelete = false
                item.todo = todoTask.text.toString()
                btSubmit.isVisible = false
                todoTask.isEnabled = false
                listener.onBtClick(item)
            }
        }

        private fun getCardColor(position: Int): Int {
            return when (position % 7) {
                0 -> R.drawable.rect_blue
                1 -> R.drawable.rect_green
                2 -> R.drawable.rect_pink
                3 -> R.drawable.rect_maroon
                4 -> R.drawable.rect_green
                5 -> R.drawable.rect_red
                6 -> R.drawable.rect_orange
                else -> R.drawable.rect_pink
            }
        }

        private fun showOptionsDialog(
            context: Context,
            position: Int,
            editCallback: () -> Unit,
            deleteCallback: () -> Unit
        ) {
            val options = arrayOf("Edit task", "Delete task")
            MaterialAlertDialogBuilder(context)
                .setTitle("Choose an option")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> editCallback()
                        1 -> deleteCallback()
                    }
                }
                .show()
        }

        private fun deleteTask(position: Int, todos: Todos) {
            // Implement the delete task logic here
            todos.isDelete = true
            todos.isAdd = false
            todos.isEdit = false
            if (itemList.contains(todos)) {
                itemList.remove(todos)
                notifyDataSetChanged()
            }
            listener.onBtClick(todos)
        }
    }
}
