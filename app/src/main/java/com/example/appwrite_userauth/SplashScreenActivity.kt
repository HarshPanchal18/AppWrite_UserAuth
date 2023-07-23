package com.example.appwrite_userauth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.appwrite_userauth.databinding.ActivitySplashScreenBinding

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val handler = Handler()
        handler.postDelayed({
            /*val check = getSharedPreferences("is_logged", Context.MODE_PRIVATE)
                .getString("SESSION_ID", null)
            if (check is String && check.isNotEmpty())
                startActivity(Intent(this, MainActivity::class.java))
            else*/
                startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)
    }
}
