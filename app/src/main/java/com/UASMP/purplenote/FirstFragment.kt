package com.UASMP.purplenote

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar

class FirstFragment : Fragment(R.layout.fragment_first) {

    private lateinit var activityList: MutableList<ActivityModel>
    private lateinit var adapter: ActivityAdapter
    private lateinit var agendaNext: TextView
    private lateinit var usernameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rev1)
        val btnAdd = view.findViewById<ImageView>(R.id.btnAdd)
        agendaNext = view.findViewById(R.id.agendanext)
        usernameTextView = view.findViewById(R.id.usernameTextView)

        tampilkanUsername()

        activityList = StorageHelper.loadActivityList(requireContext())
        activityList = getSortedActivityList()

        adapter = ActivityAdapter(activityList) { position ->
            val activity = activityList[position]
            cancelAlarm(activity.date, activity.time)
            activityList.removeAt(position)
            StorageHelper.saveActivityList(requireContext(), activityList)
            adapter.updateList(activityList)
            updateNextActivityDisplay()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            showAddDialog()
        }

        activityList.forEach {
            setAlarm(it.date, it.time, it.activity)
        }

        updateNextActivityDisplay()
    }

    override fun onResume() {
        super.onResume()
        tampilkanUsername()
    }

    private fun tampilkanUsername() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            usernameTextView.text = "Halo, pengguna"
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("users").child(uid)
        ref.child("username").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nama = snapshot.getValue(String::class.java)
                usernameTextView.text = "Halo, $nama"
            }

            override fun onCancelled(error: DatabaseError) {
                usernameTextView.text = "Halo, pengguna"
            }
        })
    }

    private fun getSortedActivityList(): MutableList<ActivityModel> {
        val now = Calendar.getInstance()
        return activityList.sortedWith(compareBy { activity ->
            val dateParts = activity.date.split("-")
            val timeParts = activity.time.split(":")
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, dateParts[2].toInt())
                set(Calendar.MONTH, dateParts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
            }
            cal.timeInMillis
        }).toMutableList()
    }

    private fun updateNextActivityDisplay() {
        val now = Calendar.getInstance()
        val sorted = activityList.mapNotNull { activity ->
            val dateParts = activity.date.split("-")
            val timeParts = activity.time.split(":")
            if (dateParts.size != 3 || timeParts.size != 2) return@mapNotNull null

            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, dateParts[2].toInt())
                set(Calendar.MONTH, dateParts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
            }

            if (calendar.after(now)) Pair(calendar.timeInMillis, activity) else null
        }.sortedBy { it.first }

        agendaNext.text = if (sorted.isNotEmpty()) sorted.first().second.activity else "Tidak ada kegiatan"
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_activity, null)
        val edtActivity = dialogView.findViewById<EditText>(R.id.edtActivity)
        val edtTime = dialogView.findViewById<EditText>(R.id.edtTime)
        val edtDate = dialogView.findViewById<EditText>(R.id.edtDate)

        edtTime.isFocusable = false
        edtTime.isClickable = true
        edtDate.isFocusable = false
        edtDate.isClickable = true

        edtTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, m ->
                edtTime.setText(String.format("%02d:%02d", h, m))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        edtDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
                edtDate.setText(String.format("%02d-%02d-%04d", d, m + 1, y))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Kegiatan")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = edtActivity.text.toString().trim()
                val time = edtTime.text.toString().trim()
                val date = edtDate.text.toString().trim()

                if (name.isNotEmpty() && time.isNotEmpty() && date.isNotEmpty()) {
                    activityList.add(ActivityModel(name, time, date))
                    activityList = getSortedActivityList()
                    StorageHelper.saveActivityList(requireContext(), activityList)
                    adapter.updateList(activityList)
                    setAlarm(date, time, name)
                    updateNextActivityDisplay()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setAlarm(date: String, time: String, title: String) {
        val dateParts = date.split("-")
        val timeParts = time.split(":")
        if (dateParts.size != 3 || timeParts.size != 2) return

        val day = dateParts[0].toIntOrNull() ?: return
        val month = dateParts[1].toIntOrNull()?.minus(1) ?: return
        val year = dateParts[2].toIntOrNull() ?: return

        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -10)
        }

        if (calendar.before(Calendar.getInstance())) return

        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("title", "Pengingat Kegiatan")
            putExtra("message", title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            (date + time).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm(date: String, time: String) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            (date + time).hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FirstFragment().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}
