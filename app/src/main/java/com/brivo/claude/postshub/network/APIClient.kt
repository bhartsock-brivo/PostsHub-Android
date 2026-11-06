package com.brivo.claude.postshub.network

import com.brivo.claude.postshub.logging.Log
import com.brivo.claude.postshub.model.Post
import com.brivo.claude.postshub.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

object APIClient {
    private val baseUrl = "https://dummyjson.com".toHttpUrl()
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class PostsResponse(val posts: List<Post>)

    @Serializable
    private data class UsersResponse(val users: List<User>)

    suspend fun fetchPosts(): List<Post> = withContext(Dispatchers.IO) {
        val url = baseUrl.newBuilder()
            .addPathSegment("posts")
            .addQueryParameter("limit", "100")
            .build()
        val data = client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            response.body!!.string()
        }
        json.decodeFromString<PostsResponse>(data).posts
    }

    suspend fun fetchUsers(): List<User> = withContext(Dispatchers.IO) {
        val url = baseUrl.newBuilder()
            .addPathSegment("users")
            .addQueryParameter("limit", "100")
            .build()
        Log.api.event("GET $url")
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            val bytes = response.body?.bytes() ?: ByteArray(0)
            Log.api.event("← users status=${response.code} bytes=${bytes.size}")
            val data = bytes.decodeToString()
            try {
                val users = json.decodeFromString<UsersResponse>(data).users
                Log.api.event("decoded users count=${users.size}")
                users
            } catch (e: SerializationException) {
                Log.dumpDecodingError(e, data, Log.api)
                throw e
            }
        }
    }

    suspend fun searchPosts(query: String): List<Post> {
        val url = baseUrl.newBuilder()
            .addPathSegments("posts/search")
            .addQueryParameter("q", query)
            .build()
        Log.api.event("GET $url (q='$query')")
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            val bytes = response.body?.bytes() ?: ByteArray(0)
            Log.api.event("← search status=${response.code} bytes=${bytes.size}")
            val data = bytes.decodeToString()
            try {
                val posts = json.decodeFromString<PostsResponse>(data).posts
                Log.api.event("decoded search posts count=${posts.size} for q='$query'")
                return posts
            } catch (e: SerializationException) {
                Log.dumpDecodingError(e, data, Log.api)
                throw e
            }
        }
    }
}
