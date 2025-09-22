package com.example.bloggers.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloggers.AddArticleActivity
import com.example.bloggers.R
import com.example.bloggers.ReadMoreActivity
import com.example.bloggers.model.BlogItemModel
import com.google.firebase.database.FirebaseDatabase

class ArticleAdapter(
    private val context: Context,
    private var blogList: MutableList<BlogItemModel>
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val heading: TextView = itemView.findViewById(R.id.heading)
        val blogInfo: TextView = itemView.findViewById(R.id.blog_info)
        val date: TextView = itemView.findViewById(R.id.date)
        val username: TextView = itemView.findViewById(R.id.username)
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val moreButton: Button = itemView.findViewById(R.id.more_button)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.article_item, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val blog = blogList[position]

        holder.heading.text = blog.heading?.ifEmpty { "No Title" }
        holder.blogInfo.text = blog.post?.ifEmpty { "No Content" }
        holder.date.text = blog.date?.ifEmpty { "Unknown Date" }
        holder.username.text = blog.userName?.ifEmpty { "Anonymous" }

        // Load image safely
        val image = blog.imageUrl
        print(image)
        if (!image.isNullOrEmpty()) {
            Glide.with(context)
                .load(image)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.profile1)
        }

        // More button
        holder.moreButton.setOnClickListener {
            val intent = Intent(context, ReadMoreActivity::class.java)
            intent.putExtra("blogItem", blog)
            context.startActivity(intent)
        }

        // Edit button
        holder.editButton.setOnClickListener {
            val intent = Intent(context, AddArticleActivity::class.java)
            intent.putExtra("editMode", true)
            intent.putExtra("blogItem", blog)
            context.startActivity(intent)
        }

        // Delete button
        holder.deleteButton.setOnClickListener {
            val db = FirebaseDatabase.getInstance(
                "https://bloggers-96929-default-rtdb.asia-southeast1.firebasedatabase.app/"
            ).getReference("blogs")

            val postId = blog.postId
            if (!postId.isNullOrEmpty()) {
                db.child(postId).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        blogList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, blogList.size)
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int = blogList.size

    fun updateData(newList: List<BlogItemModel>) {
        blogList.clear()
        blogList.addAll(newList)
        notifyDataSetChanged()
    }
}
