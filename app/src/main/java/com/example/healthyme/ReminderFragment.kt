package com.example.healthyme

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.adapters.CalendarAdapter
import com.example.healthyme.adapters.ReminderAdapter
import com.example.healthyme.models.Reminder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter
    private val reminderList = mutableListOf<Reminder>()

    private lateinit var calendarRecycler: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter

    private val PREFS_NAME = "reminder_prefs"
    private val REMINDER_KEY = "reminder_list"
    private val gson = Gson()

    private val TEST_MODE = true  // quick test → use minutes instead of hours

    private val habitList = listOf(
        "Drink Water 💧",
        "Exercise / Workout 🏋️",
        "Morning Walk 🚶",
        "Meditation 🧘",
        "Reading 📖",
        "Healthy Meal 🥗",
        "Sleep Early 😴",
        "Wake Up Early ⏰",
        "Limit Screen Time 📱",
        "Take Medicine 💊",
        "Custom..."
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_reminder, container, false)

        // calendar strip
        calendarRecycler = view.findViewById(R.id.calendarRecyclerView)
        calendarRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val days = generateNextDays(14)
        calendarAdapter = CalendarAdapter(days) { date ->
            if (date == null) {
                // All reminders
                updateReminderList(reminderList)
            } else {
                // Filter reminders by date
                val cal = Calendar.getInstance().apply {
                    time = date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = cal.timeInMillis
                val endOfDay = startOfDay + (24 * 60 * 60 * 1000)

                val filtered = reminderList.filter { r ->
                    r.triggerTimeMillis in startOfDay until endOfDay
                }
                updateReminderList(filtered)
            }
        }
        calendarRecycler.adapter = calendarAdapter

        // reminders list (default all)
        recyclerView = view.findViewById(R.id.reminderRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReminderAdapter(reminderList,
            onToggle = { pos, isActive ->
                val r = reminderList[pos]
                r.isActive = isActive
                if (isActive) {
                    if (r.triggerTimeMillis > 0) scheduleOneTimeReminder(r) else scheduleReminder(r)
                } else {
                    cancelReminder(r)
                }
                saveReminders()
            },
            onDelete = { pos -> showDeleteDialog(pos) }
        )
        recyclerView.adapter = adapter

        loadReminders()

        val fab = view.findViewById<FloatingActionButton>(R.id.addReminderFab)
        fab.setOnClickListener { showAddReminderDialog() }

        return view
    }

    // 🔹 helper: update list dynamically (all/filtered)
    private fun updateReminderList(list: List<Reminder>) {
        adapter = ReminderAdapter(list.toMutableList(),
            onToggle = { pos, isActive ->
                val r = list[pos]
                r.isActive = isActive
                if (isActive) {
                    if (r.triggerTimeMillis > 0) scheduleOneTimeReminder(r) else scheduleReminder(r)
                } else {
                    cancelReminder(r)
                }
                saveReminders()
            },
            onDelete = { pos ->
                val r = list[pos]
                showDeleteDialog(reminderList.indexOf(r)) // delete from original
            }
        )
        recyclerView.adapter = adapter
    }

    private fun showAddReminderDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_reminder, null, false)

        val spinner: Spinner = dialogView.findViewById(R.id.habitSpinner)
        val titleInput: TextInputEditText = dialogView.findViewById(R.id.reminderTitleInput)
        val intervalInput: TextInputEditText = dialogView.findViewById(R.id.reminderIntervalInput)
        val activeSwitch: SwitchCompat = dialogView.findViewById(R.id.reminderActiveSwitch)
        val iconPreview: ImageView = dialogView.findViewById(R.id.iconPreview)

        var selectedTimeMillis: Long = 0L

        // pick date + time button
        val pickDateBtn: View = dialogView.findViewById(R.id.pickDateButton)
        pickDateBtn.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                TimePickerDialog(requireContext(), { _, h, min ->
                    cal.set(y, m, d, h, min, 0)
                    selectedTimeMillis = cal.timeInMillis
                    Log.d("Reminder", "Selected date-time: ${Date(selectedTimeMillis)}")
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val adapterSpinner =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, habitList)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner

        AlertDialog.Builder(requireContext())
            .setTitle("Add Reminder")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val selected = spinner.selectedItem as String
                val titleText = titleInput.text?.toString()?.trim()?.ifEmpty { selected }
                val interval = intervalInput.text?.toString()?.toIntOrNull() ?: 1
                val icon = getHabitIcon(selected)

                val reminder = Reminder(
                    title = titleText,
                    interval = interval,
                    isActive = activeSwitch.isChecked,
                    iconRes = icon,
                    triggerTimeMillis = selectedTimeMillis
                )

                reminderList.add(reminder)
                saveReminders()
                updateReminderList(reminderList)

                if (reminder.isActive) {
                    if (reminder.triggerTimeMillis > 0) scheduleOneTimeReminder(reminder)
                    else scheduleReminder(reminder)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleOneTimeReminder(reminder: Reminder) {
        val delay = reminder.triggerTimeMillis - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putString("title", reminder.title)
            .build()

        val req = OneTimeWorkRequestBuilder<com.example.healthyme.workers.ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(reminder.id)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(req)

        Log.d("Reminder", "⏰ Scheduled reminder for ${Date(reminder.triggerTimeMillis)}")
    }

    private fun saveReminders() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(reminderList)
        prefs.edit().putString(REMINDER_KEY, json).apply()
    }

    private fun loadReminders() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(REMINDER_KEY, null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<Reminder>>() {}.type
            val savedList: MutableList<Reminder> = gson.fromJson(json, type)

            reminderList.clear()
            reminderList.addAll(savedList)

            updateReminderList(reminderList)

            for (r in reminderList) {
                if (r.isActive) {
                    cancelReminder(r)
                    if (r.triggerTimeMillis > 0) scheduleOneTimeReminder(r)
                    else scheduleReminder(r)
                }
            }
        }
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete reminder")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Yes") { _, _ ->
                val r = reminderList[position]
                cancelReminder(r)
                reminderList.removeAt(position)
                saveReminders()
                updateReminderList(reminderList)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun scheduleReminder(reminder: Reminder) {
        val data = Data.Builder().putString("title", reminder.title).build()
        val wm = WorkManager.getInstance(requireContext())

        wm.cancelAllWorkByTag(reminder.id)

        if (TEST_MODE) {
            val req = OneTimeWorkRequestBuilder<com.example.healthyme.workers.ReminderWorker>()
                .setInitialDelay(reminder.interval.toLong(), TimeUnit.MINUTES)
                .setInputData(data)
                .addTag(reminder.id)
                .build()
            wm.enqueue(req)
        } else {
            val req =
                PeriodicWorkRequestBuilder<com.example.healthyme.workers.ReminderWorker>(
                    reminder.interval.toLong(), TimeUnit.HOURS
                )
                    .setInputData(data)
                    .addTag(reminder.id)
                    .build()
            wm.enqueue(req)
        }
    }

    private fun cancelReminder(reminder: Reminder) {
        WorkManager.getInstance(requireContext())
            .cancelAllWorkByTag(reminder.id)

        Log.d("ReminderFragment", "❌ Cancelled reminder job for ${reminder.title}")
    }

    private fun getHabitIcon(habit: String): Int {
        return when {
            habit.contains("Water") || habit.contains("Drink") -> R.drawable.ic_water
            habit.contains("Exercise") || habit.contains("Workout") -> R.drawable.ic_fitness
            habit.contains("Walk") -> R.drawable.ic_walk
            habit.contains("Meditation") -> R.drawable.ic_meditation
            habit.contains("Reading") -> R.drawable.ic_book
            habit.contains("Meal") || habit.contains("Healthy") -> R.drawable.ic_food
            habit.contains("Sleep") -> R.drawable.ic_sleep
            habit.contains("Wake") -> R.drawable.ic_alarm
            habit.contains("Screen") -> R.drawable.ic_phone
            habit.contains("Medicine") || habit.contains("pill") || habit.contains("Take Medicine") -> R.drawable.ic_pills
            else -> R.drawable.ic_default
        }
    }

    private fun generateNextDays(count: Int): List<Date> {
        val list = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        repeat(count) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return list
    }
}
