package com.example.bloggers.register

import android.content.Intent
import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bloggers.MainActivity
import com.example.bloggers.R
import com.example.bloggers.SigninActivity
import com.example.bloggers.databinding.ActivityStartBinding
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private  val binding: ActivityStartBinding by lazy {
        ActivityStartBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

    auth = FirebaseAuth.getInstance()

    binding.loginButton.setOnClickListener {
        val intent = Intent(this, SigninActivity::class.java)
        intent.putExtra("action", "login")
        startActivity(intent)
        finish()
    }
        binding.registerButton.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            intent.putExtra("action", "register")
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser!=null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}