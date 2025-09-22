package com.example.bloggers.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.database.PropertyName

@Parcelize
data class BlogItemModel(
    val heading: String? = null,
    var imageUrl: String? = null,
    val userName: String? = null,
    val date: String? = null,
    val post: String? = null,

    var likeCount: Int? = 0,
    var postId: String? = "",
    val likedBy: MutableList<String>? = mutableListOf(),
    var isSaved: Boolean? = false,
    val userId: String? = null,

    // Use @JvmField to prevent getter/setter generation for Firebase
    @JvmField
    var stability: Int? = null,

    val likes: Map<String, Boolean>? = null
) : Parcelable
