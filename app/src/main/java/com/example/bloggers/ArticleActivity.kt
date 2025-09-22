package com.example.bloggers

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloggers.adapter.ArticleAdapter
import com.example.bloggers.databinding.ActivityArticleBinding
import com.example.bloggers.model.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var blogRef: DatabaseReference
    private lateinit var articleAdapter: ArticleAdapter
    private val blogList = mutableListOf<BlogItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        blogRef = FirebaseDatabase.getInstance(
            "https://bloggers-96929-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).getReference("blogs")

        // Setup RecyclerView
        articleAdapter = ArticleAdapter(this, blogList)
        binding.articleRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ArticleActivity)
            adapter = articleAdapter
        }

        binding.backButton.setOnClickListener { finish() }

        if (currentUserId != null) {
            fetchUserBlogs(currentUserId)
        } else {
            showEmpty()
        }
    }

    private fun fetchUserBlogs(userId: String) {
        blogRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogList.clear()
                for (blogSnap in snapshot.children) {
                    val map = blogSnap.value as? Map<*, *> ?: continue

                    val blogUserId = map["userId"] as? String ?: ""
                    if (blogUserId != userId) continue // only logged-in user's blogs

                    val heading = map["heading"] as? String ?: ""
                    val post = map["post"] as? String ?: ""
                    val date = map["date"] as? String ?: ""
                    val userName = map["userName"] as? String ?: "Anonymous"
                    val postId = blogSnap.key ?: ""
                    val image = map["imageUrl"]

                    val blog = BlogItemModel(
                        postId = postId,
                        heading = heading,
                        post = post,
                        date = date,
                        userName = userName,
                        userId = blogUserId,
                        imageUrl = image.toString()
                    )

                    // Fetch user image from users/{userId} first
                    FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(blogUserId)
                        .child("imageUrl")
                        .get()
                        .addOnSuccessListener { urlSnap ->
                            blog.imageUrl = urlSnap.getValue(String::class.java)
                            val index = blogList.indexOf(blog)
                            if (index != -1) {
                                articleAdapter.notifyItemChanged(index)
                            }
                        }

                    blogList.add(blog)
                    articleAdapter.notifyItemInserted(blogList.size - 1)
                }

                if (blogList.isEmpty()) showEmpty()
                else binding.articleRecyclerView.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ArticleActivity", "Failed to fetch blogs: ${error.message}")
                showEmpty()
            }
        })
    }

    private fun showEmpty() {
        binding.articleRecyclerView.visibility = View.GONE
    }
}
