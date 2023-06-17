package com.example.appwrite_userauth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appwrite_userauth.databinding.ActivityMainBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.User
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1") // Project Endpoint
            .setProject("648b140a11900d9078cd")
            .setSelfSigned(status = true) // For self signed certificates, only use for development

        val account = Account(client)
        CoroutineScope(Dispatchers.Main).launch {
            val user = account.get()
            binding.username.text = user.name
            binding.usermail.text = user.email
        }

        sharedPreferences = getSharedPreferences("is_logged", Context.MODE_PRIVATE)
        val userIDs = sharedPreferences.getString("user_id", null)
        val sessionID = sharedPreferences.getString("SESSION_ID", null)
        val ips = sharedPreferences.getString("ip", null)
        val deviceModel = sharedPreferences.getString("device_model", null)
        val deviceBrand = sharedPreferences.getString("device_brand", null)
        val deviceOs = sharedPreferences.getString("device_os", null)
        val deviceOsVer = sharedPreferences.getString("device_os_version", null)
        val userName = sharedPreferences.getString("username", null)

        binding.apply {
            ip.text = ips
            userID.text = userIDs
            username.text = userName
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
                flushSharedPrefs()
                Log.e("Appwrite", response.toString())
                this@MainActivity.runOnUiThread {
                    Toast.makeText(this@MainActivity, "Session flushed", Toast.LENGTH_SHORT).show()
                }
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            } catch (e: AppwriteException) {
                this@MainActivity.runOnUiThread {
                    Toast.makeText(this@MainActivity, e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun flushSharedPrefs() {
        sharedPreferences = getSharedPreferences("is_logged", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .remove("SESSION_ID").remove("ip").remove("device_model")
            .remove("device_brand").remove("device_os").remove("device_os_version")
            .remove("user_id").remove("username")
            .apply()
    }
}
