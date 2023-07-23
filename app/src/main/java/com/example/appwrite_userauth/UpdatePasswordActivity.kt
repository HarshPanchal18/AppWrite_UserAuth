package com.example.appwrite_userauth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appwrite_userauth.configs.AppwriteManager
import com.example.appwrite_userauth.configs.AppwriteManager.account
import com.example.appwrite_userauth.databinding.ActivityUpdatePasswordBinding
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class UpdatePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdatePasswordBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sessionID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppwriteManager.initialize(this)
        sharedPreferences = getSharedPreferences("is_logged", Context.MODE_PRIVATE)
        sessionID = sharedPreferences.getString("SESSION_ID", null)!!

        val oldPassword = binding.oldPassword.text.toString().trim()
        binding.updatePasswordBtn.setOnClickListener {
            if (oldPassword.isNotEmpty())
                rememberOldPassword()
            else
                forgotOldPassword()
        }
    }

    private fun rememberOldPassword() {
        GlobalScope.launch {
            try {
                account.updatePassword(
                    binding.password.text.toString().trim(),
                    binding.oldPassword.text.toString().trim()
                )
                flushSession(account, sessionID)
            } catch (e: AppwriteException) {
                Log.d("UpdatePasswordException", e.message.toString())
            }
        }
    }

    private fun forgotOldPassword() {
        val intent: Intent = intent
        val action: String? = intent.action
        val data: Uri? = intent.data
        GlobalScope.launch {
            try {
                account.createRecovery(
                    email = account.get().email,
                    url = "https://example.com"
                )
            } catch (e: AppwriteException) {
                Log.d("UpdatePasswordException", e.message.toString())
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
