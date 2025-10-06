package com.example.healthyme.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.adapters.MoodAdapter
import com.example.healthyme.models.Mood
import com.example.healthyme.utils.MoodStorage
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoodAdapter
    private lateinit var moodChart: LineChart
    private lateinit var storage: MoodStorage
    private val moodList = mutableListOf<Mood>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        recyclerView = view.findViewById(R.id.moodRecyclerView)
        moodChart = view.findViewById(R.id.moodChart)
        storage = MoodStorage(requireContext())

        adapter = MoodAdapter(
            moods = moodList,
            onEdit = { position -> editMood(position) },
            onDelete = { position -> deleteMood(position) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 🩵 connect Save button
        val saveButton = view.findViewById<Button>(R.id.saveMoodButton)
        saveButton?.setOnClickListener {
            addMoodFromInput(view)
        }

        loadMoods()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadMoods()
    }

    // 🔹 Get user input and save
    private fun addMoodFromInput(view: View) {
        val noteInput = view.findViewById<EditText>(R.id.noteInput)
        val emojiSelector = view.findViewById<RadioGroup>(R.id.emojiSelector)

        val selectedId = emojiSelector.checkedRadioButtonId
        if (selectedId == -1) {
            return // No emoji selected
        }

        val selectedEmoji = view.findViewById<RadioButton>(selectedId).text.toString()
        val note = noteInput.text.toString().trim()
        if (note.isNotEmpty()) {
            saveMood(selectedEmoji, note)
            noteInput.text.clear()
            emojiSelector.clearCheck()
        }
    }

    // 🔹 Add Mood
    private fun saveMood(emoji: String, note: String) {
        val title = when (emoji) {
            "😊" -> "Happy"
            "😐" -> "Neutral"
            "😢", "😔" -> "Sad"
            "😡" -> "Angry"
            "😍" -> "Love"
            else -> "Mood"
        }

        val date = SimpleDateFormat("yyyy-MM-dd, hh:mm a", Locale.getDefault()).format(Date())
        val mood = Mood(emoji, title, note, date)

        moodList.add(0, mood)
        storage.saveEntries(moodList)

        if (::adapter.isInitialized) {
            adapter.notifyItemInserted(0)
            recyclerView.scrollToPosition(0)
        }

        setupWeeklyMoodChart(moodList)
    }

    // 🔹 Edit Mood
    private fun editMood(position: Int) {
        if (position !in moodList.indices) return
        val mood = moodList[position]

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_mood, null, false)

        val noteInput = dialogView.findViewById<EditText>(R.id.editNoteInput)
        val emojiSelector = dialogView.findViewById<RadioGroup>(R.id.editEmojiSelector)

        noteInput.setText(mood.note)
        for (i in 0 until emojiSelector.childCount) {
            val rb = emojiSelector.getChildAt(i) as? RadioButton
            if (rb?.text.toString() == mood.emoji) {
                rb?.isChecked = true
                break
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Mood")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val selectedId = emojiSelector.checkedRadioButtonId
                if (selectedId != -1) {
                    val selectedEmoji =
                        dialogView.findViewById<RadioButton>(selectedId).text.toString()
                    val updatedNote = noteInput.text.toString().trim()

                    mood.emoji = selectedEmoji
                    mood.note = updatedNote
                    mood.title = when (selectedEmoji) {
                        "😊" -> "Happy"
                        "😐" -> "Neutral"
                        "😢", "😔" -> "Sad"
                        "😡" -> "Angry"
                        "😍" -> "Love"
                        else -> "Mood"
                    }

                    storage.saveEntries(moodList)
                    adapter.notifyItemChanged(position)
                    setupWeeklyMoodChart(moodList)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // 🔹 Delete Mood
    private fun deleteMood(position: Int) {
        if (position !in moodList.indices) return
        moodList.removeAt(position)
        storage.saveEntries(moodList)
        adapter.notifyItemRemoved(position)
        setupWeeklyMoodChart(moodList)
    }

    // 🔹 Load Moods
    private fun loadMoods() {
        moodList.clear()
        moodList.addAll(storage.loadEntries())

        if (moodList.isEmpty()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd, hh:mm a", Locale.getDefault())
            moodList.addAll(
                listOf(
                    Mood("😊", "Happy", "Had a great day!", dateFormat.format(Date())),
                    Mood("😐", "Neutral", "Just an average mood.", dateFormat.format(Date())),
                    Mood("😢", "Sad", "Feeling down today.", dateFormat.format(Date())),
                    Mood("😍", "Love", "Loved the sunset!", dateFormat.format(Date()))
                )
            )
            storage.saveEntries(moodList)
        }

        adapter.notifyDataSetChanged()
        setupWeeklyMoodChart(moodList)
    }

    // 🔹 Weekly Chart
    private fun setupWeeklyMoodChart(list: List<Mood>) {
        if (!::moodChart.isInitialized || list.isEmpty()) {
            moodChart.clear()
            moodChart.invalidate()
            return
        }

        val entries = ArrayList<Entry>()
        val recentMoods = list.take(7).reversed()

        recentMoods.forEachIndexed { index, mood ->
            entries.add(Entry(index.toFloat(), getMoodValue(mood.emoji).toFloat()))
        }

        val dataSet = LineDataSet(entries, "Mood Trend").apply {
            color = resources.getColor(R.color.blue_primary, null)
            setCircleColor(resources.getColor(R.color.blue_primary, null))
            circleRadius = 5f
            valueTextSize = 0f
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        moodChart.data = LineData(dataSet)

        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        moodChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(days)
            granularity = 1f
            setDrawGridLines(false)
        }

        val emojis = listOf("", "😢", "😐", "😊", "😍")
        moodChart.axisLeft.apply {
            granularity = 1f
            axisMinimum = 1f
            axisMaximum = 4f
            valueFormatter = IndexAxisValueFormatter(emojis)
        }

        moodChart.axisRight.isEnabled = false
        moodChart.description.isEnabled = false
        moodChart.legend.isEnabled = false
        moodChart.animateY(800)
        moodChart.invalidate()
    }

    private fun getMoodValue(emoji: String): Int = when (emoji) {
        "😢", "😔" -> 1
        "😐" -> 2
        "😊" -> 3
        "😍" -> 4
        "😡" -> 2
        else -> 2
    }
}
