/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.blake7.weartube.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.blake7.weartube.data.model.YouTubeVideo
import com.blake7.weartube.presentation.screens.HomeScreen
import com.blake7.weartube.presentation.screens.VideoPlayerScreen
import com.blake7.weartube.presentation.theme.WearTubeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearTubeApp()
        }
    }
}

// Simple video cache to avoid JSON serialization issues
object VideoCache {
    private val videos = mutableMapOf<String, YouTubeVideo>()

    fun store(video: YouTubeVideo): String {
        val id = video.videoId
        videos[id] = video
        return id
    }

    fun get(id: String): YouTubeVideo? = videos[id]
}

@Composable
fun WearTubeApp() {
    WearTubeTheme {
        val navController = rememberSwipeDismissableNavController()

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            composable(
                route = "home"
            ) {
                HomeScreen(
                    onVideoClick = { video ->
                        val videoId = VideoCache.store(video)
                        navController.navigate("video_player/$videoId")
                    }
                )
            }

            composable(
                route = "video_player/{videoId}"
            ) { backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
                val video = VideoCache.get(videoId)

                if (video != null) {
                    VideoPlayerScreen(
                        video = video,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    // Handle error case - navigate back to home
                    navController.popBackStack()
                }
            }
        }
    }
}