package com.example.healthyme.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.models.Mood

class MoodAdapter(
    private val moods: MutableList<Mood>,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiText: TextView = itemView.findViewById(R.id.emojiText)
        val noteTitle: TextView = itemView.findViewById(R.id.noteTitle)
        val noteText: TextView = itemView.findViewById(R.id.noteText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]
        holder.emojiText.text = mood.emoji
        holder.noteTitle.text = mood.title
        holder.noteText.text = mood.note
        holder.dateText.text = mood.date

        holder.editButton.setOnClickListener { onEdit(position) }
        holder.deleteButton.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount(): Int = moods.size
}

