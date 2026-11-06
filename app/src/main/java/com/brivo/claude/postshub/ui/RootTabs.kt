package com.brivo.claude.postshub.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.brivo.claude.postshub.store.PostStore
import kotlinx.serialization.Serializable

@Serializable object PostsTab
@Serializable object FavoritesTab

@Serializable object PostListRoute
@Serializable data class PostDetailRoute(val postId: Int)

@Serializable object FavoritesRoute
@Serializable data class FavoriteDetailRoute(val postId: Int)

private data class Tab<T : Any>(val route: T, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(PostsTab, "Posts", Icons.AutoMirrored.Filled.List),
    Tab(FavoritesTab, "Favorites", Icons.Filled.Favorite),
)

@Composable
fun RootTabs() {
    val store: PostStore = viewModel()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PostsTab,
            modifier = Modifier.padding(innerPadding),
        ) {
            navigation<PostsTab>(startDestination = PostListRoute) {
                composable<PostListRoute> {
                    PostListScreen(
                        store = store,
                        onOpenPost = { post -> navController.navigate(PostDetailRoute(post.id)) },
                    )
                }
                composable<PostDetailRoute> { entry ->
                    PostDetailScreen(
                        postId = entry.toRoute<PostDetailRoute>().postId,
                        store = store,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            navigation<FavoritesTab>(startDestination = FavoritesRoute) {
                composable<FavoritesRoute> {
                    FavoritesScreen(
                        store = store,
                        onOpenPost = { post -> navController.navigate(FavoriteDetailRoute(post.id)) },
                    )
                }
                composable<FavoriteDetailRoute> { entry ->
                    PostDetailScreen(
                        postId = entry.toRoute<FavoriteDetailRoute>().postId,
                        store = store,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
