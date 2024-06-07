package com.example.todoapp.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.ui.home.Listener
import com.example.todoapp.model.Calendar
import kotlinx.android.synthetic.main.date_list.view.tvDate
import kotlinx.android.synthetic.main.date_list.view.tvDay

class MyAdapter(
    private val itemList: List<Calendar>,
    private val listener: Listener
) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.date_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)

        // Color the first card
        if (position == 0) {
            holder.card.setBackgroundResource(R.drawable.back_orange)
            holder.date.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            holder.day.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }

        holder.itemView.setOnClickListener {
            val isSelected = toggleSelection(position)
            listener.onItemClick(item, isSelected, holder.card, holder.day, holder.date, position)
            notifyItemChanged(position)
        }

        // Update view based on selection
        if (selectedPositions.contains(position)) {
            holder.card.setBackgroundResource(R.drawable.back_orange)
            holder.date.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            holder.day.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else if (position != 0) { // Don't change the color of the first card
            holder.card.setBackgroundResource(R.drawable.back_gray)
            holder.date.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.day.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    private fun toggleSelection(position: Int): Boolean {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position)
        } else {
            selectedPositions.clear()
            selectedPositions.add(position)
        }
        return selectedPositions.contains(position)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val day: TextView = itemView.findViewById(R.id.tvDay)
        val card: LinearLayout = itemView.findViewById(R.id.llCalendar)

        fun bind(calendar: Calendar) {
            // Bind data to views

            itemView.tvDay.text = calendar.day
            itemView.tvDate.text = calendar.date
        }
    }
}
