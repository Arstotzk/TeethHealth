package com.example.TeethHealth.Activitys

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.TeethHealth.Cephalometric.ImageCallBack
import com.example.TeethHealth.Cephalometric.ImageToShow
import com.example.TeethHealth.Cephalometric.ImagesCallBack
import com.example.TeethHealth.Cephalometric.Point
import com.example.TeethHealth.Cephalometric.ZoomLayout
import com.example.TeethHealth.R
import com.example.TeethHealth.Service.Connection
import com.example.TeethHealth.Service.InteractionService
import org.json.JSONArray
import java.util.*


class ImageActivity : AppCompatActivity() {

    var connection: Connection = Connection(false)
    var imageToShow: ImageToShow? = null
    private var container: FrameLayout? = null
    private var xDelta:Int = 0
    private var yDelta:Int = 0
    var resizeCoef: Float = 1F
    var heightShift: Int = 0
    var zoomView: ZoomLayout? = null

    var points: MutableList<Point> = mutableListOf<Point>()

    public override fun onResume() {
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        connection = intent.getSerializableExtra("connection") as Connection
        imageToShow = intent.getSerializableExtra("imageToShow") as ImageToShow

        val iv = findViewById<View>(R.id.imageView) as ImageView

        zoomView = findViewById(R.id.zoom_layout) as ZoomLayout

        container = findViewById(R.id.container) as FrameLayout
        //myZoomView!!.addView(container)

        val service = InteractionService(connection.serviceAddress!!, applicationContext)
        service.getImage(connection.idDevice!!, connection.userName!!, imageToShow!!.guid, object : ImageCallBack {
            override fun onSuccess(bitmap: Bitmap) {
                Log.d("ImageActivity bitmap", bitmap.toString())
                //imageView.setImageBitmap(bitmap)
                val mutableBitmap: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutableBitmap)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.setColor(Color.GREEN)
                canvas.drawCircle(50F, 50F, 10F, paint)
                iv.setImageBitmap(mutableBitmap)

                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)

                var width = displayMetrics.widthPixels
                var height = displayMetrics.heightPixels
                var bw = bitmap.width
                var bh = bitmap.height
                var ivw = iv.width
                var ivh = iv.height

                resizeCoef = (width.toFloat() / bw)

                heightShift = ((1200 - bh * resizeCoef) / 2).toInt()
                Log.d("resizeCoef", "w = $width; h = $height bw = $bw; bh = $bh resize = $resizeCoef; heightShift = $heightShift ivw = $ivw ivh = $ivh")

                if (imageToShow!!.status == "complete")
                {
                    service.getPoints(connection.idDevice!!, connection.userName!!, imageToShow!!.guid, object : ImagesCallBack {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onSuccess(pointsJSON: JSONArray) {
                            var imagesToShow = mutableListOf<ImageToShow>()
                            var statuses = mutableListOf<String>()
                            for (num in 0 until pointsJSON.length()) {
                                val pointJSON = pointsJSON.getJSONArray(num)
                                val x = pointJSON[0]
                                val y = pointJSON[1]
                                val name = pointJSON[2].toString()
                                val imageGuid = pointJSON[3]
                                val pointGuid = pointJSON[4]


                                val xConvert = ((x as Int) * resizeCoef).toInt()
                                val yConvert = ((y as Int) * resizeCoef).toInt() + heightShift
                                Log.d("getPoints","name == $name, x = $x; y = $y; xd = $xConvert; yd = $yConvert;")
                                var point = Point(x, y, xConvert, yConvert, name)
                                point.Guid = UUID.fromString(pointGuid.toString())
                                points.add(point)

                                AddPoint(point)

                            }
                        }
                    })
                }
            }
        })


    }
    fun onClickToUpdatePoints(view: View?) {
        var anyPointChanged = false
        points.forEach()
        {
            val service = InteractionService(connection.serviceAddress!!, applicationContext)
            if(it.isChanged == true)
            {
                service.postPoint(connection.idDevice!!, connection.userName!!, it)
                anyPointChanged = true
            }
        }
        if (anyPointChanged == true)
        {
            val service = InteractionService(connection.serviceAddress!!, applicationContext)
            service.postFindParams(connection.idDevice!!, connection.userName!!, imageToShow!!.guid)
        }
        //AddPoint(100,100, "t")
    }
    fun AddPoint(point: Point){
        val imageView = ImageView(this@ImageActivity)
        val textView = TextView(this@ImageActivity)
        val params = FrameLayout.LayoutParams(100, 100)
        params.leftMargin = point.xConverted
        params.topMargin = point.yConverted
        imageView.layoutParams = params
        val bmp: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.point)
        imageView.setImageBitmap(bmp)
        imageView.setOnTouchListener(touchListener)
        point.view = imageView
        textView.layoutParams = params
        textView.text = " " + point.name
        textView.textSize = 12f
        textView.setTextColor(Color.rgb(0,112,221))

        container!!.addView(imageView)
        container!!.addView(textView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private val touchListener = View.OnTouchListener { view, event ->
        var point = Point(0,0,0,0, "").getPointByView(points,view)
        val x = (event.rawX).toInt()
        val y = (event.rawY).toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val lParams = view.layoutParams as FrameLayout.LayoutParams
                xDelta = ((x - lParams.leftMargin) ).toInt()
                yDelta = ((y - lParams.topMargin) ).toInt()
            }
            MotionEvent.ACTION_UP -> {
                //Toast.makeText(applicationContext, "Объект перемещён", Toast.LENGTH_SHORT).show()
                val scale = zoomView!!.scale
                Log.d("MotionEvent.ACTION_UP", "x = $x; y = $y; xd = $xDelta; yd = $yDelta; scale = $scale")
            }
            MotionEvent.ACTION_MOVE -> {
                if ((x - xDelta + view.width <= container!!.width) && (y - yDelta + view.height <= container!!.height) && (x - xDelta >= 0) && (y - yDelta >= 0)) {
                    val layoutParams = view.layoutParams as FrameLayout.LayoutParams
                    val newX = ((x - xDelta))
                    val newY = ((y - yDelta))
                    Log.d("MotionEvent.Move", "x = $x; y = $y; xd = $xDelta; yd = $yDelta;")
                    layoutParams.leftMargin = newX
                    layoutParams.topMargin = newY
                    layoutParams.rightMargin = 0
                    layoutParams.bottomMargin = 0
                    view.layoutParams = layoutParams

                    point.isChanged = true
                    point.xConverted = newX
                    point.yConverted = newY
                    point.x = (newX / resizeCoef).toInt()
                    point.y = ((newY - heightShift) / resizeCoef).toInt()
                }
            }
        }
        container!!.invalidate()
        true
    }
}