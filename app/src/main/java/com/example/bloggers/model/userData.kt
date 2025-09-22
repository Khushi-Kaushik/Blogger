package com.example.bloggers.model

data class userData(
    val name : String,
    val email : String,
    val profileImageUrl: String = ""
) {
    constructor() : this("" , "" ,"")
}
