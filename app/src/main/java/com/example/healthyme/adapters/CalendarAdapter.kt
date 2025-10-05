package com.example.healthyme.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(
    private val days: List<Date>,
    private val onDateClick: (Date?) -> Unit   // Date? -> null = "All"
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    // First item = "All"
    private val items = listOf<Date?>(null) + days
    private var selectedPos = 0

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.calendarCard)
        val dayNumber: TextView = view.findViewById(R.id.dayNumber)
        val dayName: TextView = view.findViewById(R.id.dayName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = items[position]

        if (date == null) {
            // 🔹 "All" card
            holder.dayNumber.text = "All"
            holder.dayName.text = ""
        } else {
            val cal = Calendar.getInstance().apply { time = date }
            holder.dayNumber.text = cal.get(Calendar.DAY_OF_MONTH).toString()
            holder.dayName.text = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }

        // 🔹 highlight selected card
        if (position == selectedPos) {
            holder.card.strokeWidth = 3
            holder.card.strokeColor =
                holder.card.context.getColor(R.color.blue_primary) // define in colors.xml
        } else {
            holder.card.strokeWidth = 0
        }

        holder.itemView.setOnClickListener {
            val prev = selectedPos
            selectedPos = position
            notifyItemChanged(prev)
            notifyItemChanged(selectedPos)

            onDateClick(date)
        }
    }

    override fun getItemCount() = items.size
}
