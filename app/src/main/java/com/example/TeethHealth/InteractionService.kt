package com.example.TeethHealth

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class InteractionService(url: String, _context: Context) {

    var serviceUrl: String
    var context: Context

    init {
        serviceUrl = url
        context = _context
    }
    fun getUser(idDevice: String, name: String, callBack: UserCallBack){

        var isUserExist = false
        val queue = Volley.newRequestQueue(context)
        var mParams = HashMap<String, String>()
        mParams["idDevice"] = idDevice
        mParams["name"] = name

        val request = object : JsonObjectRequest(
                Request.Method.GET,
                serviceUrl + "/user",
                null,
                Response.Listener<JSONObject> {response ->
                    Log.d("InteractionService","response is: $response")
                    isUserExist = response.getBoolean("isUserExist")
                    callBack.onSuccess(isUserExist)
                },
                Response.ErrorListener {error ->
                    Log.d("InteractionService","error is: $error")
                }
        ) {
            override fun getUrl(): String? {
                val stringBuilder = StringBuilder(serviceUrl + "/user")
                var i = 1
                for ((key1, value1) in mParams) {
                    var key: String
                    var value: String
                    try {
                        key = URLEncoder.encode(key1, "UTF-8")
                        value = URLEncoder.encode(value1, "UTF-8")
                        if (i == 1) {
                            stringBuilder.append("?$key=$value")
                        } else {
                            stringBuilder.append("&$key=$value")
                        }
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                    i++
                }
                return stringBuilder.toString()
            }
        }

        queue.add(request)
    }
    fun postNewUser(idDevice: String, name: String){

        val queue = Volley.newRequestQueue(context)

        val request = object : StringRequest(
                Method.POST,
                serviceUrl + "/user/new",
                Response.Listener {
                    Log.d("InteractionService","response is: $it")
                },
                Response.ErrorListener {
                    Log.d("InteractionService","error is: $it")
                }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): MutableMap<String, String> {
                var params = HashMap<String, String>()
                params["idDevice"] = idDevice
                params["name"] = name
                return params
            }
        }

        queue.add(request)
    }
    fun postImage(bitmap: Bitmap, idDevice: String, name: String){

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
            @Throws(AuthFailureError::class)
            override fun getParams(): MutableMap<String, String> {
                var params = HashMap<String, String>()
                params["idDevice"] = idDevice
                params["name"] = name
                return params
            }
        }

        queue.add(request)
    }

    fun getImages(idDevice: String, name: String, callBack: ImagesCallBack)
    {
        var jsonArray = JSONObject()
        val queue = Volley.newRequestQueue(context)
        var mParams = HashMap<String, String>()
        mParams["idDevice"] = idDevice
        mParams["name"] = name

        val request = object : JsonArrayRequest(
                Request.Method.GET,
                serviceUrl + "/image/all",
                null,
                Response.Listener {response ->
                    Log.d("InteractionService","response is: $response")
                    callBack.onSuccess(response)
                },
                Response.ErrorListener {error ->
                    Log.d("InteractionService","error is: $error")
                }
        ) {
            override fun getUrl(): String? {
                val stringBuilder = StringBuilder(serviceUrl + "/image/all")
                var i = 1
                for ((key1, value1) in mParams) {
                    var key: String
                    var value: String
                    try {
                        key = URLEncoder.encode(key1, "UTF-8")
                        value = URLEncoder.encode(value1, "UTF-8")
                        if (i == 1) {
                            stringBuilder.append("?$key=$value")
                        } else {
                            stringBuilder.append("&$key=$value")
                        }
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                    i++
                }
                return stringBuilder.toString()
            }
        }

        queue.add(request)
    }
}