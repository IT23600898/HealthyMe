package com.example.healthyme.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.models.Reminder

class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onToggle: (position: Int, isActive: Boolean) -> Unit,
    private val onDelete: (position: Int) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.reminderIcon)
        val title: TextView = view.findViewById(R.id.reminderTitle)
        val subtitle: TextView = view.findViewById(R.id.reminderSubtitle)
        val time: TextView = view.findViewById(R.id.reminderTime)
        val check: ImageView = view.findViewById(R.id.reminderCheck)
        val delete: ImageView = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.title.text = reminder.title
        holder.subtitle.text = "${reminder.interval} ${holder.itemView.context.getString(R.string.interval_unit)}"
        holder.time.text = if (reminder.interval == 1) {
            holder.itemView.context.getString(R.string.every_n_unit_single, reminder.interval)
        } else {
            holder.itemView.context.getString(R.string.every_n_unit, reminder.interval)
        }

        holder.icon.setImageResource(reminder.iconRes)

        holder.check.setImageResource(
            if (reminder.isActive) R.drawable.img_2 else R.drawable.img_1
        )

        holder.check.setOnClickListener {
            val newState = !reminder.isActive
            reminder.isActive = newState
            notifyItemChanged(position)
            onToggle(position, newState)
        }

        holder.delete.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount() = reminders.size
}
