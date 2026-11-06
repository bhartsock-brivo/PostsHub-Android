package com.brivo.claude.postshub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brivo.claude.postshub.R
import com.brivo.claude.postshub.logging.Log
import com.brivo.claude.postshub.model.Post
import com.brivo.claude.postshub.store.PostStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(store: PostStore, onOpenPost: (Post) -> Unit) {
    val posts by store.posts.collectAsStateWithLifecycle()
    val isLoading by store.isLoading.collectAsStateWithLifecycle()
    val errorMessage by store.errorMessage.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var selectedUserId by remember { mutableStateOf<Int?>(null) }
    var showingFilter by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filteredPosts = selectedUserId?.let { uid -> posts.filter { it.userId == uid } } ?: posts

    LaunchedEffect(Unit) {
        Log.list.event("LaunchedEffect fired (storePosts=${store.posts.value.size} storeUsers=${store.users.value.size})")
        store.refresh()
        store.loadUsers()
    }

    DisposableEffect(Unit) {
        Log.list.event("onAppear filtered=${filteredPosts.size} store=${store.posts.value.size} selectedUserId=${selectedUserId ?: "nil"}")
        onDispose {
            Log.list.event("onDisappear store=${store.posts.value.size}")
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Posts") },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .combinedClickable(
                                role = Role.Button,
                                onClickLabel = "Filter by user",
                                onLongClickLabel = "Dump diagnostics",
                                onClick = { showingFilter = true },
                                onLongClick = { store.diagnosticsDump() },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_filter_list),
                            contentDescription = "Filter by user",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { newValue ->
                    query = newValue
                    Log.list.event("query changed -> '$newValue'")
                    store.searchInline(newValue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search posts") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    Log.list.event("pull-to-refresh")
                    scope.launch {
                        isRefreshing = true
                        store.refresh()
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.scale(1.4f))
                        Text("Loading posts…")
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    itemsIndexed(filteredPosts) { index, post ->
                        val dismissState = rememberSwipeToDismissBoxState()
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = { DeleteBackground() },
                            onDismiss = {
                                Log.list.event(
                                    "onDelete index=$index removingId=${store.posts.value[index].id} " +
                                        "store=${store.posts.value.size} selectedUserId=${selectedUserId ?: "nil"}"
                                )
                                store.posts.value = store.posts.value.toMutableList().also { it.removeAt(index) }
                                scope.launch { dismissState.reset() }
                            },
                            modifier = Modifier.animateItem(),
                        ) {
                            PostRow(post = post, store = store, onClick = { onOpenPost(post) })
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showingFilter) {
        UserFilterSheet(
            store = store,
            selectedUserId = selectedUserId,
            onSelect = { selectedUserId = it },
            onDismiss = { showingFilter = false },
        )
    }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { store.errorMessage.value = null }) { Text("OK") }
            },
        )
    }
}

@Composable
private fun DeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onError,
        )
    }
}
