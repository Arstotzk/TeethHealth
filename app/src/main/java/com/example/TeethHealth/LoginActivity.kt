package com.example.TeethHealth

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    var serviceAddress: EditText? = null
    var userName: EditText? = null
    var isLogIn: Boolean = false

    public override fun onResume() {
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        serviceAddress = findViewById<View>(R.id.serviceAddress) as EditText?
        userName = findViewById(R.id.userName) as EditText?
    }

    fun onClickToRegistration(view: View?) {
        isLogIn = false
        val service = InteractionService(serviceAddress?.text.toString(), applicationContext)
        val idDevice = Settings.Secure.getString(applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID)
        service.getUser(idDevice, userName?.text.toString(), object : UserCallBack {
            override fun onSuccess(isUserExist: Boolean) {
                var isAlreadyExist = isUserExist
                if (!isAlreadyExist)
                    service.postNewUser(idDevice, userName?.text.toString())
                else
                    Toast.makeText(applicationContext, "Такой пользователь уже существует", Toast.LENGTH_LONG).show()
            }
        })
    }
    fun onClickToLogIn(view: View?) {
        val service = InteractionService(serviceAddress?.text.toString(), applicationContext)
        val idDevice = Settings.Secure.getString(applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID)
        service.getUser(idDevice, userName?.text.toString(), object : UserCallBack {
            override fun onSuccess(isUserExist: Boolean) {
                isLogIn = isUserExist
                if(isLogIn)
                {
                    Toast.makeText(applicationContext, "Удалось войти", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("isLogIn", isLogIn)
                    intent.putExtra("userName", userName?.text.toString())
                    intent.putExtra("serviceAddress", serviceAddress?.text.toString())
                    intent.putExtra("idDevice", Settings.Secure.getString(applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID))
                    startActivity(intent)
                }
                else
                    Toast.makeText(applicationContext, "Не удалось войти", Toast.LENGTH_LONG).show()
            }
        })
    }
    fun onClickToLogInWithoutService(view: View?)   {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        isLogIn = false
        intent.putExtra("isLogIn", isLogIn)
        startActivity(intent)
    }
}