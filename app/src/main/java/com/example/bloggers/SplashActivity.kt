package com.example.bloggers

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bloggers.register.StartActivity

class   SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Delay for 2 seconds (splash screen)
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserLoginStatus()
        }, 2000)
    }

    private fun checkUserLoginStatus() {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // User already logged in → go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // User not logged in → go to StartActivity (login/register)
            startActivity(Intent(this, StartActivity::class.java))
        }
        finish()
    }
}
