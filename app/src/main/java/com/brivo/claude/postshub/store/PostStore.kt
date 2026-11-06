package com.brivo.claude.postshub.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brivo.claude.postshub.logging.Log
import com.brivo.claude.postshub.model.Post
import com.brivo.claude.postshub.model.User
import com.brivo.claude.postshub.network.APIClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PostStore : ViewModel() {
    val posts = MutableStateFlow<List<Post>>(emptyList())
    val users = MutableStateFlow<List<User>>(emptyList())
    val favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    private var refreshTimer: Job? = null
    private var searchJob: Job? = null

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshTimer = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(30_000)
                refresh()
            }
        }
    }

    suspend fun refresh() {
        Log.store.event("refresh start")
        isLoading.value = true
        try {
            val fetched = APIClient.fetchPosts()
            posts.value = fetched
            isLoading.value = false
            Log.store.event("refresh ok: posts=${fetched.size}")
        } catch (e: Exception) {
            errorMessage.value = "Failed to load posts: ${e::class.simpleName}"
            isLoading.value = false
            Log.store.event("refresh failed: ${e::class.simpleName}")
        }
    }

    suspend fun loadUsers() {
        Log.store.event("loadUsers start")
        try {
            val fetched = APIClient.fetchUsers()
            users.value = fetched
            Log.store.event("loadUsers ok: users=${fetched.size}")
        } catch (e: Exception) {
            errorMessage.value = "Failed to load users: ${e::class.simpleName}"
            Log.store.event("loadUsers failed: ${e::class.simpleName}")
        }
    }

    fun searchInline(query: String) {
        Log.store.event("searchInline q='$query'")
        searchJob?.cancel()
        if (query.isEmpty()) {
            Log.store.event("searchInline empty -> refresh")
            viewModelScope.launch { refresh() }
            return
        }
        searchJob = viewModelScope.launch {
            val results = runCatching { APIClient.searchPosts(query) }.getOrDefault(emptyList())
            val processed = results.sortedBy { it.id }
            Log.store.event("searchInline assign q='$query' results=${processed.size}")
            posts.value = processed
        }
    }

    fun toggleFavorite(postId: Int) {
        if (postId in favoriteIds.value) {
            favoriteIds.value -= postId
        } else {
            favoriteIds.value += postId
        }
        Log.store.event("toggleFavorite id=$postId nowFav=${postId in favoriteIds.value} total=${favoriteIds.value.size}")
    }

    fun updatePost(post: Post) {
        val idx = posts.value.indexOfFirst { it.id == post.id }
        if (idx == -1) {
            Log.store.event("updatePost id=${post.id} NOT FOUND in posts (count=${posts.value.size})")
            return
        }
        posts.value = posts.value.toMutableList().also { it[idx] = post }
        Log.store.event("updatePost id=${post.id} idx=$idx titleLen=${post.title.length} bodyLen=${post.body.length}")
    }

    fun diagnosticsDump() {
        Log.store.event("── DIAGNOSTICS ──")
        Log.store.event(
            "posts.count=${posts.value.size} users.count=${users.value.size} " +
                "favoriteIds=${favoriteIds.value.sorted()} isLoading=${isLoading.value} " +
                "error='${errorMessage.value ?: "nil"}'"
        )
    }
}
