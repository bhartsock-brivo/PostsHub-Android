package com.brivo.claude.postshub.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.brivo.claude.postshub.logging.Log
import com.brivo.claude.postshub.model.Post
import com.brivo.claude.postshub.store.PostStore
import kotlinx.coroutines.delay
import java.net.URI

@Composable
fun PostDetailScreen(postId: Int, store: PostStore, onBack: () -> Unit) {
    val posts by store.posts.collectAsStateWithLifecycle()
    val post = posts.firstOrNull { it.id == postId }
    if (post == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Post #$postId is no longer available")
        }
        return
    }
    PostDetailContent(post = post, store = store, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDetailContent(post: Post, store: PostStore, onBack: () -> Unit) {
    val users by store.users.collectAsStateWithLifecycle()
    val favoriteIds by store.favoriteIds.collectAsStateWithLifecycle()

    var draft by remember { mutableStateOf(post) }
    var showSavedToast by remember { mutableStateOf(false) }

    val avatarUrl = remember(post.userId) {
        val raw = "https://avatars.example.com/user ${post.userId}.png"
        Log.detail.event("init post=${post.id} userId=${post.userId} avatarRaw='$raw'")
        URI(raw)
    }

    DisposableEffect(Unit) {
        Log.detail.event("onAppear id=${post.id} usersLoaded=${store.users.value.size}")
        onDispose {
            Log.detail.event("onDisappear id=${post.id} draftDirty=${draft != post}")
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Post #${post.id}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SectionHeader("Author")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = avatarUrl.toString(),
                        contentDescription = null,
                        placeholder = ColorPainter(Color.Gray.copy(alpha = 0.3f)),
                        error = ColorPainter(Color.Gray.copy(alpha = 0.3f)),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    val user = users.firstOrNull { it.id == post.userId }
                    if (user != null) {
                        Column {
                            Text(user.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Text("User #${post.userId}")
                    }
                }

                SectionHeader("Title")
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = { draft = draft.copy(title = it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Title") },
                )

                SectionHeader("Body")
                OutlinedTextField(
                    value = draft.body,
                    onValueChange = { draft = draft.copy(body = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp),
                )

                Spacer(modifier = Modifier.padding(top = 8.dp))

                val isFavorite = post.id in favoriteIds
                OutlinedButton(
                    onClick = {
                        val wasFav = post.id in store.favoriteIds.value
                        if (post.id in store.favoriteIds.value) {
                            store.favoriteIds.value -= post.id
                        } else {
                            store.favoriteIds.value += post.id
                        }
                        Log.detail.event("favorite tap id=${post.id} wasFav=$wasFav nowFav=${post.id in store.favoriteIds.value}")
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites")
                }

                Button(
                    onClick = {
                        Log.detail.event(
                            "Save tapped id=${post.id} titleChanged=${draft.title != post.title} " +
                                "bodyChanged=${draft.body != post.body} titleLen=${draft.title.length} bodyLen=${draft.body.length}"
                        )
                        showSavedToast = true
                    },
                    enabled = draft != post,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save Changes")
                }
            }

            if (showSavedToast) {
                AnimatedVisibility(
                    visible = showSavedToast,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp,
                    ) {
                        Text("Saved", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
                LaunchedEffect(Unit) {
                    delay(1_500)
                    showSavedToast = false
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}
