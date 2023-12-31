@file:Suppress("DEPRECATION")

package com.example.appwrite_userauth

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appwrite_userauth.configs.AppwriteManager
import com.example.appwrite_userauth.configs.AppwriteManager.client
import com.example.appwrite_userauth.databinding.ActivityLoginBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppwriteManager.initialize(applicationContext)

        binding.loginBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isEmpty() && password.isEmpty())
                loginProcess(client, email, password)
            else {
                val account = Account(client)
                GlobalScope.launch {
                    // https://dev.to/appwrite/auth0-authentication-with-appwrite-7hk
                    account.createOAuth2Session(
                        activity = this@LoginActivity,
                        provider = "auth0"
                    )
                }
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
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

                gotoMainWithPrefs(
                    ip,
                    deviceModel,
                    deviceBrand,
                    deviceOS,
                    deviceOSversion,
                    sessionId,
                    userId,
                    username
                )
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
