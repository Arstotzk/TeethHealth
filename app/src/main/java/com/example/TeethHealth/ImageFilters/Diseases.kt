package com.example.TeethHealth.ImageFilters

import android.graphics.Bitmap
import android.widget.TextView

class Diseases(private val bitmap: Bitmap?, private val tvStatus: TextView?) {
    companion object {
        fun GetTvStatus(dise: Diseases?): TextView? {
            return dise?.tvStatus
        }

        fun GetBitmap(dise: Diseases?): Bitmap? {
            return dise?.bitmap
        }
    }
}