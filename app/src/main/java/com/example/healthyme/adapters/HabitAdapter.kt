package com.example.healthyme.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.models.Habit

class HabitAdapter(
    private var habits: MutableList<Habit>,
    private val onHabitToggled: (Habit) -> Unit,
    private val onEditHabit: (Habit, Int) -> Unit,
    private val onDeleteHabit: (Habit, Int) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val tvHabitTime: TextView = itemView.findViewById(R.id.tvHabitTime)
        val imgHabitCheck: ImageView = itemView.findViewById(R.id.imgHabitCheck)
        val imgOptions: ImageView = itemView.findViewById(R.id.imgOptions) // 3-dot menu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.tvHabitName.text = habit.title
        holder.tvHabitTime.text = habit.time

        // Toggle completion
        holder.imgHabitCheck.setImageResource(
            if (habit.isCompleted) R.drawable.img_1 else R.drawable.img
        )
        holder.imgHabitCheck.setColorFilter(
            ContextCompat.getColor(
                holder.itemView.context,
                if (habit.isCompleted) R.color.blue_light else android.R.color.holo_blue_light
            )
        )

        holder.imgHabitCheck.setOnClickListener {
            habit.isCompleted = !habit.isCompleted
            notifyItemChanged(position)
            onHabitToggled(habit)
        }

        // Options menu (Edit/Delete)
        holder.imgOptions.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.imgOptions)
            popup.menuInflater.inflate(R.menu.menu_habit_item, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        onEditHabit(habit, position)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteHabit(habit, position)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = habits.size

    fun updateData(newList: MutableList<Habit>) {
        habits = newList
        notifyDataSetChanged()
    }

    fun editItem(position: Int, newHabit: Habit) {
        habits[position] = newHabit
        notifyItemChanged(position)
    }
}
