package com.example.bloggers

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.bloggers.adapter.BlogAdapter
import com.example.bloggers.databinding.ActivityMainBinding
import com.example.bloggers.model.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth
    private lateinit var blogAdapter: BlogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance(
            "https://bloggers-96929-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference.child("blogs")

        blogAdapter = BlogAdapter(blogItems)
        binding.recyclerBlog.layoutManager = LinearLayoutManager(this)
        binding.recyclerBlog.adapter = blogAdapter

        auth.currentUser?.uid?.let { loadUserProfileImage(it) }

        loadBlogs()

        binding.addArticleButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }
        binding.saveEmptyIcon.setOnClickListener {
            startActivity(Intent(this, SavedArticlesActivity::class.java))
        }
        binding.profileImage1.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadBlogs() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for (child in snapshot.children) {
                    val postId = child.key ?: ""
                    val heading = child.child("heading").getValue(String::class.java)
                    val imageUrl = child.child("imageUrl").getValue(String::class.java)
                    val userName = child.child("userName").getValue(String::class.java)
                    val date = child.child("date").getValue(String::class.java)
                    val post = child.child("post").getValue(String::class.java)
                    val likeCount = child.child("likeCount").getValue(Int::class.java) ?: 0
                    val likedBy = child.child("likes").children.mapNotNull { it.key }.toMutableList()

                    val blogItem = BlogItemModel(
                        heading = heading,
                        imageUrl = imageUrl,
                        userName = userName,
                        date = date,
                        post = post,
                        likeCount = likeCount,
                        postId = postId,
                        likedBy = likedBy
                    )
                    blogItems.add(blogItem)
                }
                blogItems.reverse()
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Blog loading failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference = FirebaseDatabase.getInstance(
            "https://bloggers-96929-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference.child("users").child(userId).child("profileImageUrl")

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                profileImageUrl?.let {
                    Glide.with(this@MainActivity)
                        .load(it)
                        .into(binding.profileImage1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loading profile image", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
