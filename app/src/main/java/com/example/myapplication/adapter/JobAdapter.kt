package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.JobModel
import com.example.myapplication.R

class JobAdapter(
    private val jobs: MutableList<JobModel>,
    private val onAccept: (JobModel) -> Unit,
    private val onReject: (JobModel) -> Unit
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        val tvCompany: TextView = itemView.findViewById(R.id.tvCompany)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnAccept: ImageView = itemView.findViewById(R.id.btnAccept)
        val btnReject: ImageView = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_card, parent, false)
        return JobViewHolder(view)
    }

    override fun getItemCount(): Int = jobs.size

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobs[position]

        holder.tvJobTitle.text = job.title
        holder.tvCompany.text = job.company
        holder.tvLocation.text = job.location
        holder.tvDescription.text = job.description

        holder.btnAccept.setOnClickListener {
            onAccept(job)
            removeAt(holder.bindingAdapterPosition)
        }

        holder.btnReject.setOnClickListener {
            onReject(job)
            removeAt(holder.bindingAdapterPosition)
        }
    }

    fun removeAt(position: Int) {
        if (position != RecyclerView.NO_POSITION && position < jobs.size) {
            jobs.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
