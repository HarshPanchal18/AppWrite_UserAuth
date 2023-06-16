package com.example.appwrite_userauth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.appwrite_userauth.databinding.ActivityMainBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val client = Client(applicationContext)
            .setEndpoint("https://cloud.appwrite.io/v1") // Project Endpoint
            .setProject("648b140a11900d9078cd")
            .setSelfSigned(status = true) // For self signed certificates, only use for development

        val account = Account(client)

        sharedPreferences = getSharedPreferences("is_logged", Context.MODE_PRIVATE)
        val userIDs = sharedPreferences.getString("user_id", null)
        val sessionID = sharedPreferences.getString("SESSION_ID", null)
        val ips = sharedPreferences.getString("ip", null)
        val deviceModel = sharedPreferences.getString("device_model", null)
        val deviceBrand = sharedPreferences.getString("device_brand", null)
        val deviceOs = sharedPreferences.getString("device_os", null)
        val deviceOsVer = sharedPreferences.getString("device_os_ver", null)

        binding.apply {
            ip.text = ips
            userID.text = userIDs
            deviceMod.text = deviceModel
            deviceBra.text = deviceBrand
            OS.text = deviceOs
            OsVer.text = deviceOsVer
        }

        binding.endSession.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("End Session")
                .setMessage("Are you sure you want to end your session?")
                .setCancelable(true)
                .setPositiveButton("Yes") { _, _ ->
                    flushSession(account, sessionID!!)
                }.setNegativeButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.cancel()
                }.show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun flushSession(account: Account, sessionID: String) {
        GlobalScope.launch {
            try {
                val response = account.deleteSession(sessionID)
                flushSharedPref()
                Log.e("Appwrite", response.toString())
                this@MainActivity.runOnUiThread {
                    Toast.makeText(this@MainActivity, "Session flushed", Toast.LENGTH_SHORT).show()
                }
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            } catch (e: AppwriteException) {
                this@MainActivity.runOnUiThread(Runnable {
                    Toast.makeText(this@MainActivity, e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                })
            }
        }
    }

    private fun flushSharedPref() {
        sharedPreferences = getSharedPreferences("is_logged", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("SESSION_ID").apply()
        sharedPreferences.edit().remove("ip").apply()
        sharedPreferences.edit().remove("device_model").apply()
        sharedPreferences.edit().remove("device_brand").apply()
        sharedPreferences.edit().remove("device_os").apply()
        sharedPreferences.edit().remove("device_os_ver").apply()
        sharedPreferences.edit().remove("user_id").apply()
    }

    private fun isBooleanAccepted() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
