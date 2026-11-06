package com.brivo.claude.postshub.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int,
    @SerialName("user_id") val userId: Int,
    val title: String,
    val body: String,
)
