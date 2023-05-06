package com.example.TeethHealth.Activitys

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.TeethHealth.*
import com.example.TeethHealth.Cephalometric.ImageToShow
import com.example.TeethHealth.Cephalometric.ImagesCallBack
import com.example.TeethHealth.Cephalometric.ImagesRecyclerAdapter
import com.example.TeethHealth.Service.Connection
import com.example.TeethHealth.Service.InteractionService
import org.json.JSONArray
import java.util.*

class ImagesActivity : AppCompatActivity() {

    var recyclerView: RecyclerView? = null

    var connection: Connection = Connection(false)

    var loadingPanel: RelativeLayout? = null
    public override fun onResume() {
        super.onResume()
        loadingPanel!!.setVisibility(View.VISIBLE)
        var emptyAdapter = ImagesRecyclerAdapter(listOf<ImageToShow>())
        recyclerView!!.adapter = emptyAdapter

        val service = InteractionService(connection.serviceAddress!!, applicationContext)
        service.getImages(connection.idDevice!!, connection.userName!!, object : ImagesCallBack {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSuccess(images: JSONArray) {
                loadingPanel!!.setVisibility(View.GONE)
                var imagesToShow = mutableListOf<ImageToShow>()
                var statuses = mutableListOf<String>()
                for (num in 0 until images.length()) {
                    val image = images.getJSONArray(num)
                    val guid = image[0]
                    val path = image[1]
                    val user = image[2]
                    val date = image[3]
                    val status = image[4]

                    if (date.toString() != "null" && status.toString() != "null") {
                        var imageToShow = ImageToShow(UUID.fromString(guid.toString()), date.toString(), status.toString())
                        imagesToShow.add(imageToShow)
                    }
                }
                var adapter = ImagesRecyclerAdapter(imagesToShow)
                adapter.onItemClick = { imageToShow ->
                    val intent = Intent(this@ImagesActivity, ImageActivity::class.java)
                    intent.putExtra("connection", connection)
                    intent.putExtra("imageToShow", imageToShow)
                    startActivity(intent)
                }
                recyclerView!!.adapter = adapter


            }
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)
        loadingPanel = findViewById(R.id.loadingImagesPanel) as RelativeLayout

        connection = intent.getSerializableExtra("connection") as Connection

        val service = InteractionService(connection.serviceAddress!!, applicationContext)
        service.getImages(connection.idDevice!!, connection.userName!!, object : ImagesCallBack {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSuccess(images: JSONArray) {
                loadingPanel!!.setVisibility(View.GONE)
                var imagesToShow = mutableListOf<ImageToShow>()
                var statuses = mutableListOf<String>()
                for (num in 0 until images.length()) {
                    val image = images.getJSONArray(num)
                    val guid = image[0]
                    val path = image[1]
                    val user = image[2]
                    val date = image[3]
                    val status = image[4]

                    if (date.toString() != "null" && status.toString() != "null") {
                        var imageToShow = ImageToShow(UUID.fromString(guid.toString()), date.toString(), status.toString())
                        imagesToShow.add(imageToShow)
                    }
                }
                var adapter = ImagesRecyclerAdapter(imagesToShow)
                adapter.onItemClick = { imageToShow ->
                    val intent = Intent(this@ImagesActivity, ImageActivity::class.java)
                    intent.putExtra("connection", connection)
                    intent.putExtra("imageToShow", imageToShow)
                    startActivity(intent)
                }
                recyclerView!!.adapter = adapter


            }
        })

        recyclerView = findViewById<LinearLayout>(R.id.ImagesRecyclerView) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(this)
    }
    fun onClickToIntoImage(view: View?) {

    }
}