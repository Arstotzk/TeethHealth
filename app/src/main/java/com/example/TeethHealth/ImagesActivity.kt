package com.example.TeethHealth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ImagesActivity : AppCompatActivity() {

    var recyclerView: RecyclerView? = null

    var userName: String = ""
    var serviceAddress: String = ""
    var idDevice: String = ""

    public override fun onResume() {
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)

        userName = intent.extras!!.getString("userName")!!
        serviceAddress = intent.extras!!.getString("serviceAddress")!!
        idDevice = intent.extras!!.getString("idDevice")!!

        val service = InteractionService(serviceAddress, applicationContext)
        service.getImages(idDevice, userName, object : ImagesCallBack {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSuccess(images: JSONArray) {
                var dates = mutableListOf<String>()
                var statuses = mutableListOf<String>()
                for (num in 0 until images.length()) {
                    val image = images.getJSONArray(num)
                    try {
                        val guid = image[0]
                        val path = image[1]
                        val user = image[2]
                        val date = image[3]
                        val status = image[4]

                        Log.d("ImagesActivity","error is: $image")
                        Log.d("ImagesActivity","error is: $date")
                        if (date.toString() != "null") {
                            val localDateTime = LocalDateTime.parse(date.toString(), DateTimeFormatter.RFC_1123_DATE_TIME)
                            dates.add(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        }
                        if (status.toString() != "null"){
                            if (status.toString() == "processing")
                                statuses.add("На обработке")
                            else if (status.toString() == "complete")
                                statuses.add("Обработано")
                        }
                    }
                    catch (e: UnsupportedEncodingException)
                    {

                    }
                }
                recyclerView!!.adapter = ImagesRecyclerAdapter(dates, statuses)
            }
        })

        recyclerView = findViewById<LinearLayout>(R.id.ImagesRecyclerView) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        //recyclerView!!.adapter = ImagesRecyclerAdapter(fillList())
    }

    private fun fillList(): List<String> {
        val data = mutableListOf<String>()
        (0..30).forEach { i -> data.add("$i element") }
        return data
    }
}