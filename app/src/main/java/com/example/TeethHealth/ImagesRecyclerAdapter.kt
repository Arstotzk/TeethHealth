package com.example.TeethHealth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class ImagesRecyclerAdapter(private val dates: List<String>, private val statuses: List<String>)
    : RecyclerView.Adapter<ImagesRecyclerAdapter.ImageViewHolder>(){

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textStatus: TextView = itemView.findViewById(R.id.textStatus)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_images_recycler, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.textDate.text = dates[position]
        holder.textStatus.text = statuses[position]
    }

    override fun getItemCount(): Int {
        return dates.size
    }
}