package com.example.bloggers

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bloggers.databinding.ActivityAddArticleBinding
import com.example.bloggers.model.BlogItemModel
import com.example.bloggers.model.userData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AddArticleActivity : AppCompatActivity() {

    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val auth = FirebaseAuth.getInstance()
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance(
        "https://bloggers-96929-default-rtdb.asia-southeast1.firebasedatabase.app/"
    ).getReference("blogs")

    private val userReference: DatabaseReference = FirebaseDatabase.getInstance(
        "https://bloggers-96929-default-rtdb.asia-southeast1.firebasedatabase.app/"
    ).getReference("users")

    private var editMode = false
    private var blogToEdit: BlogItemModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Check if this is edit mode
        editMode = intent.getBooleanExtra("editMode", false)
        blogToEdit = intent.getParcelableExtra("blogItem")

        if (editMode && blogToEdit != null) {
            binding.blogTitle.editText?.setText(blogToEdit?.heading)
            binding.blogDescription.editText?.setText(blogToEdit?.post)
            binding.addBlogButton.text = "Update Blog"
        } else {
            binding.addBlogButton.text = "Add Blog"
        }

        // Back button
        binding.btnBack.setOnClickListener { finish() }

        // Add or Update blog button
        binding.addBlogButton.setOnClickListener { handleAddOrUpdateBlog() }
    }

    private fun handleAddOrUpdateBlog() {
        val title = binding.blogTitle.editText?.text.toString().trim()
        val description = binding.blogDescription.editText?.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val user: FirebaseUser? = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid
        val username = user.displayName ?: "Anonymous"
        val userImage = user.photoUrl?.toString() ?: ""

        // Fetch user info from DB to ensure consistency
        userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userDataFromDB = snapshot.getValue(userData::class.java)
                val userNameFromDB = userDataFromDB?.name ?: username
                val userImageFromDB = userDataFromDB?.profileImageUrl ?: userImage
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                if (editMode && blogToEdit != null) {
                    // UPDATE existing blog
                    updateBlog(title, description, currentDate)
                } else {
                    // ADD new blog
                    addNewBlog(title, description, currentDate, userId, userNameFromDB, userImageFromDB)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddArticleActivity, "Failed to fetch user info", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateBlog(title: String, description: String, date: String) {
        blogToEdit?.let { blog ->
            val updatedBlog = blog.copy(
                heading = title,
                post = description,
                date = date
            )
            blog.postId?.let { postId ->
                databaseReference.child(postId).setValue(updatedBlog)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Blog updated successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to update blog", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun addNewBlog(
        title: String,
        description: String,
        date: String,
        userId: String,
        username: String,
        userImage: String
    ) {
        val newPostId = databaseReference.push().key ?: run {
            Toast.makeText(this, "Failed to generate post ID", Toast.LENGTH_SHORT).show()
            return
        }

        val newBlog = BlogItemModel(
            heading = title,
            post = description,
            date = date,
            userId = userId,
            userName = username,
            imageUrl = userImage,
            postId = newPostId
        )

        databaseReference.child(newPostId).setValue(newBlog)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Blog added successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add blog", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
