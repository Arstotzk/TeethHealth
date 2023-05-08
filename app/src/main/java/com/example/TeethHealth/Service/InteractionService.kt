package com.example.TeethHealth.Service

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.TeethHealth.Cephalometric.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import kotlin.collections.HashMap

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
                    callBack.onError(error.toString())
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
    fun getImage(idDevice: String, name: String, imageGuid: UUID, callBack: ImageCallBack)
    {
        var jsonArray = JSONObject()
        val queue = Volley.newRequestQueue(context)
        var mParams = HashMap<String, String>()
        mParams["idDevice"] = idDevice
        mParams["name"] = name
        mParams["imageGuid"] = imageGuid.toString()

        val imageRequest =  object : ImageRequest(serviceUrl + "/image", Response.Listener
        {
            response: Bitmap ->
            Log.d("ImageRequest", response.toString())
            callBack.onSuccess(response)
        }, 0,0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,  Response.ErrorListener
        {
            response: VolleyError ->
            Log.d("ImageRequest Error", response.toString())
        })
        {
            override fun getUrl(): String? {
                val stringBuilder = StringBuilder(serviceUrl + "/image")
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
        queue.add(imageRequest)
    }

    fun getPoints(idDevice: String, name: String, imageGuid: UUID, callBack: ImagesCallBack)
    {
        var jsonArray = JSONObject()
        val queue = Volley.newRequestQueue(context)
        var mParams = HashMap<String, String>()
        mParams["idDevice"] = idDevice
        mParams["name"] = name
        mParams["imageGuid"] = imageGuid.toString()

        val request = object : JsonArrayRequest(
                Request.Method.GET,
                serviceUrl + "/image/points",
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
                val stringBuilder = StringBuilder(serviceUrl + "/image/points")
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

    fun getParams(idDevice: String, name: String, imageGuid: UUID, callBack: ImagesCallBack)
    {
        var jsonArray = JSONObject()
        val queue = Volley.newRequestQueue(context)
        var mParams = HashMap<String, String>()
        mParams["idDevice"] = idDevice
        mParams["name"] = name
        mParams["imageGuid"] = imageGuid.toString()

        val request = object : JsonArrayRequest(
                Request.Method.GET,
                serviceUrl + "/image/params",
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
                val stringBuilder = StringBuilder(serviceUrl + "/image/params")
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

    fun postPoint(idDevice: String, name: String, point: Point){

        val queue = Volley.newRequestQueue(context)

        val request = object : VolleyMultipartRequest(
                Method.POST,
                serviceUrl + "/image/point/coordinate",
                Response.Listener {
                    Log.d("InteractionService","response is: $it")
                },
                Response.ErrorListener {
                    Log.d("InteractionService","error is: $it")
                }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                return params
            }
            @Throws(AuthFailureError::class)
            override fun getParams(): MutableMap<String, String> {
                var params = HashMap<String, String>()
                params["idDevice"] = idDevice
                params["name"] = name
                params["pointGuid"] = point.Guid.toString()
                params["x"] = point.x.toString()
                params["y"] = point.y.toString()
                return params
            }
        }

        queue.add(request)
    }
    fun postFindParams(idDevice: String, name: String, imageGuid: UUID){

        val queue = Volley.newRequestQueue(context)

        val request = object : VolleyMultipartRequest(
                Method.POST,
                serviceUrl + "/find/params",
                Response.Listener {
                    Log.d("InteractionService","response is: $it")
                },
                Response.ErrorListener {
                    Log.d("InteractionService","error is: $it")
                }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                return params
            }
            @Throws(AuthFailureError::class)
            override fun getParams(): MutableMap<String, String> {
                var params = HashMap<String, String>()
                params["idDevice"] = idDevice
                params["name"] = name
                params["imageGuid"] = imageGuid.toString()
                return params
            }
        }

        queue.add(request)
    }
}