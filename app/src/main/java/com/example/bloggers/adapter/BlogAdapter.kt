package com.example.bloggers.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bloggers.R
import com.example.bloggers.ReadMoreActivity
import com.example.bloggers.databinding.BlogItemBinding
import com.example.bloggers.model.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BlogAdapter(private val items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    // ✅ Keep original copy for search
    private val originalItems: MutableList<BlogItemModel> = ArrayList(items)

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance(
            "https://bloggers-96929-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem = items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int = items.size

    // ✅ Search function
    fun filter(query: String) {
        items.clear()
        if (query.isEmpty()) {
            items.addAll(originalItems)
        } else {
            val lowerCaseQuery = query.lowercase()
            val matched = originalItems.filter {
                it.heading?.lowercase()?.contains(lowerCaseQuery) == true ||
                        it.userName?.lowercase()?.contains(lowerCaseQuery) == true ||
                        it.post?.lowercase()?.contains(lowerCaseQuery) == true
            }
            items.addAll(matched)
        }
        notifyDataSetChanged()
    }

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blogItem: BlogItemModel) {
            val postId = blogItem.postId ?: return
            val context = binding.root.context

            binding.heading.text = blogItem.heading
            binding.username.text = blogItem.userName
            binding.date.text = blogItem.date
            binding.blogInfo.text = blogItem.post
            binding.likeNumber.text = (blogItem.likeCount ?: 0).toString()

            Glide.with(binding.profileImage.context)
                .load(blogItem.imageUrl)
                .into(binding.profileImage)

            binding.root.setOnClickListener {
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItem)
                context.startActivity(intent)
            }

            setupLikeButton(postId, blogItem, binding)
            setupSaveButton(postId, blogItem, binding)
        }

        private fun setupLikeButton(postId: String, blogItem: BlogItemModel, binding: BlogItemBinding) {
            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

            currentUser?.uid?.let { uid ->
                postLikeReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.likeEmpty.setImageResource(
                            if (snapshot.exists()) R.drawable.heart_redfull else R.drawable.heart_black
                        )
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            binding.likeEmpty.setOnClickListener {
                if (currentUser != null) {
                    handleLikeClicked(postId, blogItem, binding)
                } else {
                    Toast.makeText(binding.root.context, "Please log in first", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun handleLikeClicked(postId: String, blogItem: BlogItemModel, binding: BlogItemBinding) {
            val userReference = databaseReference.child("users").child(currentUser!!.uid)
            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

            postLikeReference.child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentLikeCount = blogItem.likeCount ?: 0
                    if (snapshot.exists()) {
                        userReference.child("likes").child(postId).removeValue().addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).removeValue()
                            blogItem.likedBy?.remove(currentUser.uid)
                            updateLikeImage(binding, false)
                            val newCount = currentLikeCount - 1
                            blogItem.likeCount = newCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newCount)
                            binding.likeNumber.text = newCount.toString()
                        }
                    } else {
                        userReference.child("likes").child(postId).setValue(true).addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).setValue(true)
                            blogItem.likedBy?.add(currentUser.uid)
                            updateLikeImage(binding, true)
                            val newCount = currentLikeCount + 1
                            blogItem.likeCount = newCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newCount)
                            binding.likeNumber.text = newCount.toString()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun updateLikeImage(binding: BlogItemBinding, liked: Boolean) {
            binding.likeEmpty.setImageResource(if (liked) R.drawable.heart_redfull else R.drawable.heart_black)
        }

        private fun setupSaveButton(postId: String, blogItem: BlogItemModel, binding: BlogItemBinding) {
            val userReference = databaseReference.child("users").child(currentUser?.uid ?: "")
            val saveReference = userReference.child("saveBlogPosts").child(postId)

            saveReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.saveIconEmpty.setImageResource(
                        if (snapshot.exists()) R.drawable.save_redfull else R.drawable.save_red
                    )
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            binding.saveIconEmpty.setOnClickListener {
                if (currentUser != null) handleSaveClicked(postId, blogItem, binding)
                else Toast.makeText(binding.root.context, "Please log in first", Toast.LENGTH_SHORT).show()
            }
        }

        private fun handleSaveClicked(postId: String, blogItem: BlogItemModel, binding: BlogItemBinding) {
            val userReference = databaseReference.child("users").child(currentUser!!.uid)
            val saveReference = userReference.child("saveBlogPosts").child(postId)

            saveReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val context = binding.root.context
                    if (snapshot.exists()) {
                        saveReference.removeValue().addOnSuccessListener {
                            blogItem.isSaved = false
                            notifyDataSetChanged()
                            Toast.makeText(context, "Post unsaved!!", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to unsave the post", Toast.LENGTH_SHORT).show()
                        }
                        binding.saveIconEmpty.setImageResource(R.drawable.save_red)
                    } else {
                        saveReference.setValue(true).addOnSuccessListener {
                            blogItem.isSaved = true
                            notifyDataSetChanged()
                            Toast.makeText(context, "Blog saved!!", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to save blog", Toast.LENGTH_SHORT).show()
                        }
                        binding.saveIconEmpty.setImageResource(R.drawable.save_redfull)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    fun updateData(savedBlogArticles: MutableList<BlogItemModel>) {
        items.clear()
        items.addAll(savedBlogArticles)
        originalItems.clear()
        originalItems.addAll(savedBlogArticles)
        notifyDataSetChanged()
    }
}
