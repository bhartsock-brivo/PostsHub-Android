package com.brivo.claude.postshub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.brivo.claude.postshub.ui.RootTabs
import com.brivo.claude.postshub.ui.theme.PostsHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PostsHubTheme {
                RootTabs()
            }
        }
    }
}
