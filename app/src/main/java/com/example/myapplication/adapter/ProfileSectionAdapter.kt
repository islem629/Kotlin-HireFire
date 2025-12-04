package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileSectionAdapter(
    private val sections: MutableList<ProfileSection>,
    private val onEditClicked: (ProfileSection, Int) -> Unit
) : RecyclerView.Adapter<ProfileSectionAdapter.ProfileSectionViewHolder>() {

    inner class ProfileSectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvSectionTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvSectionContent)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditSection)
        val expandableLayout: View = itemView.findViewById(R.id.layoutExpandable)
        val ivArrow: ImageView = itemView.findViewById(R.id.ivArrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileSectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_section, parent, false)
        return ProfileSectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileSectionViewHolder, position: Int) {
        val section = sections[position]

        holder.tvTitle.text = section.title
        holder.tvContent.text = section.content

        // Expanded / collapsed
        holder.expandableLayout.visibility = if (section.isExpanded) View.VISIBLE else View.GONE
        holder.ivArrow.rotation = if (section.isExpanded) 180f else 0f

        // Toggle expand when clicking card
        holder.itemView.setOnClickListener {
            section.isExpanded = !section.isExpanded
            notifyItemChanged(position)

            // Small arrow animation
            holder.ivArrow.animate()
                .rotation(if (section.isExpanded) 180f else 0f)
                .setDuration(150)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        // Edit button
        holder.btnEdit.setOnClickListener {
            onEditClicked(section, position)
        }
    }

    override fun getItemCount(): Int = sections.size
}
