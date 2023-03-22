package com.example.TeethHealth

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.ByteArrayOutputStream

class InteractionService(url: String, _context: Context) {

    var serviceUrl: String
    var context: Context

    init {
        serviceUrl = url
        context = _context
    }

    fun getUsers(){

        val queue = Volley.newRequestQueue(context)

        val stringRequest = StringRequest(Request.Method.GET, serviceUrl + "/users",
                Response.Listener<String> { response ->
                    val toast = Toast.makeText(context, response, Toast.LENGTH_LONG)
                    toast.show()
                },
                Response.ErrorListener { error ->
                    val toast = Toast.makeText(context, "Ошибка при отправки запроса на сервис:" + error.toString(), Toast.LENGTH_LONG)
                    toast.show()
                })

        queue.add(stringRequest)
    }
    fun postImage(bitmap: Bitmap){

        val queue = Volley.newRequestQueue(context)

        val request = object : VolleyMultipartRequest(
                Method.POST,
                serviceUrl + "/find/points",
                Response.Listener {
                    Log.d("InteractionService","response is: $it")
                },
                Response.ErrorListener {
                    Log.d("InteractionService","error is: $it")
                }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                params["imageFile"] = FileDataPart("image", stream.toByteArray(), "jpeg")
                return params
            }
        }

        queue.add(request)
    }
}