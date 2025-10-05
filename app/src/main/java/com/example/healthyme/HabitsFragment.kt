package com.example.healthyme.ui

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthyme.R
import com.example.healthyme.adapters.HabitAdapter
import com.example.healthyme.models.Habit
import com.example.healthyme.utils.HabitsStorage
import com.github.lzyzsd.circleprogress.DonutProgress

class HabitsFragment : Fragment() {

    private lateinit var rvHabits: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private var habitList = mutableListOf<Habit>()
    private lateinit var progressDonut: DonutProgress
    private lateinit var storage: HabitsStorage

    // ✅ Dummy habits (always visible)
    private val dummyHabits = listOf(
        Habit(1, "Meditation", "Morning", false),
        Habit(2, "Reading", "Evening", true),
        Habit(3, "Exercise", "Afternoon", false)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        storage = HabitsStorage(requireContext())

        rvHabits = view.findViewById(R.id.rvHabits)
        rvHabits.layoutManager = LinearLayoutManager(requireContext())

        progressDonut = view.findViewById(R.id.progressDonut)

        // ✅ Merge dummy + saved habits
        val savedHabits = storage.loadHabits()
        habitList = (dummyHabits + savedHabits).toMutableList()

        habitAdapter = HabitAdapter(
            habits = habitList,
            onHabitToggled = { updatedHabit ->
                saveUserHabits()
                Toast.makeText(requireContext(), "Toggled: ${updatedHabit.title}", Toast.LENGTH_SHORT).show()
                updateProgress()
            },
            onEditHabit = { habit, position ->
                showEditHabitDialog(habit, position)
                updateProgress()
            },
            onDeleteHabit = { habit, position ->
                // ✅ Only remove from list here
                habitList.removeAt(position)
                habitAdapter.updateData(habitList)
                saveUserHabits()
                Toast.makeText(requireContext(), "Deleted ${habit.title}", Toast.LENGTH_SHORT).show()
                updateProgress()
            }
        )

        rvHabits.adapter = habitAdapter

        // ✅ Add Habit button
        view.findViewById<View>(R.id.btnAddHabit).setOnClickListener {
            showAddHabitDialog()
        }

        // Initial progress
        updateProgress()

        return view
    }

    override fun onResume() {
        super.onResume()
        // ✅ Always reload dummy + saved
        val savedHabits = storage.loadHabits()
        habitList = (dummyHabits + savedHabits).toMutableList()
        habitAdapter.updateData(habitList)
        updateProgress()
    }

    // --- Save only user-added habits ---
    private fun saveUserHabits() {
        val userHabits = habitList.filter { habit ->
            dummyHabits.none { it.title == habit.title && it.time == habit.time }
        }
        storage.saveHabits(userHabits)
    }

    // --- Update progress donut ---
    private fun updateProgress() {
        if (habitList.isEmpty()) {
            progressDonut.progress = 0f
            return
        }
        val completed = habitList.count { it.isCompleted }
        val percent = (completed.toFloat() / habitList.size.toFloat()) * 100
        progressDonut.progress = percent
    }

    // --- Edit Habit Dialog ---
    private fun showEditHabitDialog(habit: Habit, position: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_habit, null)

        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val etHabitTime = dialogView.findViewById<EditText>(R.id.etHabitTime)

        etHabitName.setText(habit.title)
        etHabitTime.setText(habit.time)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedHabit = habit.copy(
                    title = etHabitName.text.toString(),
                    time = etHabitTime.text.toString()
                )
                habitList[position] = updatedHabit
                habitAdapter.editItem(position, updatedHabit)
                saveUserHabits()
                updateProgress()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // --- Add Habit Dialog ---
    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_habit, null)

        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val etHabitTime = dialogView.findViewById<EditText>(R.id.etHabitTime)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val newHabit = Habit(
                    id = (habitList.maxOfOrNull { it.id } ?: 0) + 1,
                    title = etHabitName.text.toString(),
                    time = etHabitTime.text.toString(),
                    isCompleted = false
                )
                habitList.add(newHabit)
                habitAdapter.updateData(habitList)
                saveUserHabits()
                updateProgress()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
