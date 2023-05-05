package com.example.TeethHealth.ImageFilters

import android.view.View

object VisualElements {
    fun SetVisibles(visibles: Int, _visualElements: Array<View?>) {
        for (i in _visualElements.indices) {
            _visualElements[i]?.visibility = visibles
        }
    }
}