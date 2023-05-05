package com.example.TeethHealth.Cephalometric

import android.content.Context
import android.view.View
import java.util.UUID

class Point (_x: Int, _y: Int, _xConverted: Int, _yConverted: Int, _name: String) {
    public var x: Int
    public var y: Int
    public var xConverted: Int
    public var yConverted: Int
    public var Guid: UUID? = null

    public var isChanged: Boolean = false
    public var name: String
    public var view: View? = null

    init {
        x = _x
        y = _y
        xConverted = _xConverted
        yConverted = _yConverted
        name = _name
    }

    fun getPointByView(_points: MutableList<Point>, _view: View): Point
    {
        _points.forEach()
        {
            if (it.view?.equals(_view) == true)
                return it
        }
        return this
    }
}