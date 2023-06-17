@file:Suppress("DEPRECATION")

package com.example.appwrite_userauth

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appwrite_userauth.databinding.ActivityLoginBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val client = Client(applicationContext)
            .setEndpoint("https://cloud.appwrite.io/v1") // Project Endpoint
            .setProject("648b140a11900d9078cd")
            .setSelfSigned(status = true) // For self signed certificates, only use for development

        binding.loginBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            loginProcess(client, email, password)
        }

        binding.registerActivity.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loginProcess(client: Client, email: String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Trying to login...")
        progressDialog.show()

        GlobalScope.launch {
            try {
                val account = Account(client)
                val response = account.createEmailSession(email, password) // json
                val ip = response.ip
                val deviceModel = response.deviceModel
                val deviceBrand = response.deviceBrand
                val deviceOS = response.osName
                val deviceOSversion = response.osVersion
                val sessionId = response.id
                val userId = response.userId
                val username = response.clientName

                gotoMainWithPrefs(ip, deviceModel, deviceBrand, deviceOS, deviceOSversion, sessionId, userId, username)
            } catch (e: AppwriteException) {
                this@LoginActivity.runOnUiThread {
                    Toast.makeText(this@LoginActivity, e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            progressDialog.dismiss()
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun gotoMainWithPrefs(
        ip: String,
        deviceModel: String,
        deviceBrand: String,
        deviceOS: String,
        deviceOSver: String,
        sessionId: String,
        userId: String,
        username: String,
    ) {
        val editor = getSharedPreferences("is_logged", Context.MODE_PRIVATE).edit()
        editor.apply {
            putString("SESSION_ID", sessionId)
            putString("ip", ip)
            putString("device_model", deviceModel)
            putString("device_brand", deviceBrand)
            putString("device_os", deviceOS)
            putString("device_os_version", deviceOSver)
            putString("user_id", userId)
            putString("username", username)
        }.apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
