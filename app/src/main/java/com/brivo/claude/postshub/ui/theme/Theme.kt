package com.brivo.claude.postshub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF3D5AFE)

@Composable
fun PostsHubTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFF8C9EFF))
    } else {
        lightColorScheme(primary = Accent)
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
