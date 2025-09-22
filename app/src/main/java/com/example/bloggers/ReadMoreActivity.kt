package com.example.bloggers

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloggers.databinding.ActivityReadMoreBinding
import com.example.bloggers.model.BlogItemModel

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityReadMoreBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            finish()
        }

        val blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")

        if(blogs!=null){
            binding.titleText.text = blogs.heading
            binding.userName.text = blogs.userName
            binding.date1.text = blogs.date
            binding.blogDescriptionTextView.text = blogs.post

            val userImageUrl = blogs.imageUrl
            Glide.with(this)
                .load(userImageUrl)
                .apply(RequestOptions.centerCropTransform())
                .into(binding.imageProfile)
        }else{
            Toast.makeText(this, "Failed to load Blog", Toast.LENGTH_SHORT).show()
        }
    }
}