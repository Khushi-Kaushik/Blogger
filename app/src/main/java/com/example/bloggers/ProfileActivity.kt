package com.example.bloggers

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bloggers.databinding.ActivityProfileBinding
import com.example.bloggers.register.StartActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {
    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance(
            "https://bloggers-96929-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference.child("users")

        val userId = auth.currentUser?.uid
        if (userId != null) loadUserProfileData(userId)

        // ✅ Add new blog button
        binding.addNewBlogButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        // ✅ Your Stories button
        binding.articlesButton.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val intent = Intent(this, ArticleActivity::class.java)
            intent.putExtra("USER_ID", uid) // pass current user id
            startActivity(intent)
        }

        // ✅ Logout button
        binding.logOutButton.setOnClickListener {
            // Clear saved session
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            auth.signOut()
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

    }

    private fun loadUserProfileData(userId: String) {
        val userReference = databaseReference.child(userId)

        // Profile image
        userReference.child("profileImageUrl").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImage = snapshot.getValue(String::class.java)
                if (profileImage != null) {
                    Glide.with(this@ProfileActivity).load(profileImage).into(binding.userProfile)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Name
        userReference.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.getValue(String::class.java)
                if (name != null) binding.profileName.text = name
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
