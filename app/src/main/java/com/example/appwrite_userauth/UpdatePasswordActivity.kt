package com.example.appwrite_userauth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.appwrite_userauth.configs.AppwriteManager
import com.example.appwrite_userauth.configs.AppwriteManager.account
import com.example.appwrite_userauth.databinding.ActivityUpdatePasswordBinding
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpdatePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdatePasswordBinding
    private lateinit var sharedPreferences: SharedPreferences

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppwriteManager.initialize(this)
        sharedPreferences = getSharedPreferences("is_logged", Context.MODE_PRIVATE)
        val sessionID = sharedPreferences.getString("SESSION_ID", null)

        binding.updatePasswordBtn.setOnClickListener {
            GlobalScope.launch {
                try {
                    account.updatePassword(
                        binding.password.text.toString().trim(),
                        binding.oldPassword.text.toString().trim()
                    )
                    flushSession(account, sessionID!!)
                } catch (e: AppwriteException) {
                    Log.d("UpdatePasswordException", e.message.toString())
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun flushSession(account: Account, sessionID: String) {
        GlobalScope.launch {
            try {
                val response = account.deleteSession(sessionID)
                flushSharedPrefs()
                Log.e("Appwrite", response.toString())
                this@UpdatePasswordActivity.runOnUiThread {
                    Toast.makeText(
                        this@UpdatePasswordActivity,
                        "Session flushed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                startActivity(Intent(this@UpdatePasswordActivity, LoginActivity::class.java))
                finish()
            } catch (e: AppwriteException) {
                this@UpdatePasswordActivity.runOnUiThread {
                    Toast.makeText(
                        this@UpdatePasswordActivity,
                        e.message.toString(),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun flushSharedPrefs() {
        sharedPreferences.edit()
            .remove("SESSION_ID").remove("ip").remove("device_model")
            .remove("device_brand").remove("device_os").remove("device_os_version")
            .remove("user_id").remove("username")
            .apply()
    }
}
