@file:Suppress("DEPRECATION")

package com.example.appwrite_userauth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appwrite_userauth.configs.AppwriteManager
import com.example.appwrite_userauth.configs.AppwriteManager.account
import com.example.appwrite_userauth.databinding.ActivityRegisterBinding
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppwriteManager.initialize(applicationContext)

        binding.registerBtn.setOnClickListener {
            val name = binding.name.text.toString()
            val email = binding.emailRegister.text.toString()
            val password = binding.passwordRegister.text.toString()
            createAccount(account, name, email, password)
        }

        binding.loginActivity.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createAccount(account: Account, name: String, email: String, password: String) {
        val progressDialog = ProgressDialog(this@RegisterActivity)
        progressDialog.setMessage("Registering Account..")
        progressDialog.show()
        GlobalScope.launch {
            try {
                account.create(userId = "unique()", email, password, name)
                this@RegisterActivity.runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Register Success", Toast.LENGTH_SHORT)
                        .show()
                }
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            } catch (e: AppwriteException) {
                this@RegisterActivity.runOnUiThread {
                    Toast.makeText(this@RegisterActivity, e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            progressDialog.dismiss()
        }
    }
}
