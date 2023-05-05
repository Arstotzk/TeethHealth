package com.example.TeethHealth.Cephalometric

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.TeethHealth.R
import com.example.TeethHealth.Service.Connection

class ImagesRecyclerAdapter(private val imagesToShow: List<ImageToShow>)
    : RecyclerView.Adapter<ImagesRecyclerAdapter.ImageViewHolder>(){

    var onItemClick: ((ImageToShow) -> Unit)? = null

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textStatus: TextView = itemView.findViewById(R.id.textStatus)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(imagesToShow[bindingAdapterPosition])
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_images_recycler, parent, false)
        return ImageViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.textDate.text = imagesToShow[position].GetDateString()
        holder.textStatus.text = imagesToShow[position].GetStatus()

    }

    override fun getItemCount(): Int {
        return imagesToShow.size
    }

}