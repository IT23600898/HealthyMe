package com.example.healthyme.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.adapters.MoodAdapter
import com.example.healthyme.models.Mood
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoodAdapter
    private val moodList = mutableListOf<Mood>()

    // Chart
    private lateinit var moodChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.moodRecyclerView)
        adapter = MoodAdapter(moodList,
            onEdit = { position -> editMood(position) },
            onDelete = { position -> deleteMood(position) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Chart view
        moodChart = view.findViewById(R.id.moodChart)

        return view
    }

    // 🔹 Save mood
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
        adapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)

        saveToPrefs()
        setupWeeklyMoodChart(moodList)
    }

    // 🔹 Edit Mood Dialog
    private fun editMood(position: Int) {
        val mood = moodList[position]

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_mood, null, false)

        val noteInput = dialogView.findViewById<EditText>(R.id.editNoteInput)
        val emojiSelector = dialogView.findViewById<RadioGroup>(R.id.editEmojiSelector)

        // set current values
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
                    val selectedEmoji = dialogView.findViewById<RadioButton>(selectedId).text.toString()
                    val updatedNote = noteInput.text.toString().trim()

                    val updatedTitle = when (selectedEmoji) {
                        "😊" -> "Happy"
                        "😐" -> "Neutral"
                        "😢", "😔" -> "Sad"
                        "😡" -> "Angry"
                        "😍" -> "Love"
                        else -> "Mood"
                    }

                    // update mood
                    mood.emoji = selectedEmoji
                    mood.note = updatedNote
                    mood.title = updatedTitle

                    adapter.notifyItemChanged(position)
                    saveToPrefs()
                    setupWeeklyMoodChart(moodList)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // 🔹 Delete Mood
    private fun deleteMood(position: Int) {
        moodList.removeAt(position)
        adapter.notifyItemRemoved(position)
        saveToPrefs()
        setupWeeklyMoodChart(moodList)
    }

    // 🔹 Save prefs
    private fun saveToPrefs() {
        val sharedPref = requireContext().getSharedPreferences("moods", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(moodList)
        sharedPref.edit().putString("mood_list", json).apply()
    }

    // 🔹 Load prefs
    override fun onResume() {
        super.onResume()
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val sharedPref = requireContext().getSharedPreferences("moods", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPref.getString("mood_list", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Mood>>() {}.type
            val list: MutableList<Mood> = gson.fromJson(json, type)
            moodList.clear()
            moodList.addAll(list)
            adapter.notifyDataSetChanged()
            setupWeeklyMoodChart(moodList)
        }
    }

    // 🔹 Convert emoji → numeric
    private fun getMoodValue(emoji: String): Int {
        return when (emoji) {
            "😢", "😔" -> 1
            "😐" -> 2
            "😊" -> 3
            "😍" -> 4
            "😡" -> 2
            else -> 2
        }
    }

    // 🔹 Setup chart
    private fun setupWeeklyMoodChart(list: List<Mood>) {
        if (!::moodChart.isInitialized) return
        if (list.isEmpty()) return

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
}
