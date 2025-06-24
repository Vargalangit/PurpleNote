package com.UASMP.purplenote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActivityAdapter(
    private var activityList: MutableList<ActivityModel>,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvActivity: TextView = view.findViewById(R.id.tvActivity)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false) // ⬅️ pastikan nama layout ini sesuai
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activityList[position]
        holder.tvActivity.text = activity.activity
        holder.tvTime.text = "Jam: ${activity.time}"
        holder.tvDate.text = "Tanggal: ${activity.date}"

        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = activityList.size

    fun updateList(newList: MutableList<ActivityModel>) {
        activityList = newList
        notifyDataSetChanged()
    }
}
