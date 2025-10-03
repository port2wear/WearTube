package com.blake7.weartube.presentation.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebSettings
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * YouTube IFrame Player using the official YouTube IFrame API
 * Reference: https://developers.google.com/youtube/iframe_api_reference
 */
@Composable
fun YouTubeIFramePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = false,
    showControls: Boolean = true,
    onPlayerReady: () -> Unit = {},
    onStateChange: (PlayerState) -> Unit = {},
    onError: (PlayerError) -> Unit = {}
) {
    var playerController by remember { mutableStateOf<YouTubePlayerController?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val container = FrameLayout(ctx)
            
            try {
                val webView = WebView(ctx).apply {
                    @SuppressLint("SetJavaScriptEnabled") // Required for YouTube IFrame API
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = false
                        allowContentAccess = false
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = WebSettings.LOAD_DEFAULT
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportZoom(false)
                    }

                    setBackgroundColor(Color.TRANSPARENT)
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                    // Enable debugging
                    WebView.setWebContentsDebuggingEnabled(true)

                    // Create JavaScript interface
                    val jsInterface = YouTubePlayerJSInterface(
                        onReady = {
                            Log.d("YouTubeIFramePlayer", "Player ready")
                            onPlayerReady()
                        },
                        onStateChange = { state ->
                            Log.d("YouTubeIFramePlayer", "State changed: $state")
                            onStateChange(state)
                        },
                        onError = { error ->
                            Log.e("YouTubeIFramePlayer", "Player error: $error")
                            onError(error)
                        }
                    )
                    addJavascriptInterface(jsInterface, "Android")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d("YouTubeIFramePlayer", "Page loaded")
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                Log.e("YouTubeIFramePlayer", "WebView error: ${error?.description}")
                                onError(PlayerError.UNKNOWN)
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(message: android.webkit.ConsoleMessage?): Boolean {
                            Log.d("YouTubeIFramePlayer", "Console: ${message?.message()}")
                            return true
                        }
                    }

                    // Create controller for this player
                    playerController = YouTubePlayerController(this)
                    
                    // Suppress unused warning by adding a comment
                    @Suppress("UNUSED_VALUE")
                    playerController
                }

                container.addView(webView, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ))

                Log.d("YouTubeIFramePlayer", "WebView created for video: $videoId")
            } catch (t: Throwable) {
                Log.e("YouTubeIFramePlayer", "Failed to create WebView", t)
                onError(PlayerError.HTML5_ERROR)
            }

            container
        },
        update = { view ->
            val child = view.getChildAt(0)
            if (child is WebView) {
                val html = buildYouTubeIFrameHTML(videoId, autoplay, showControls)
                child.loadDataWithBaseURL(
                    "https://www.youtube.com",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
    )
}

/**
 * JavaScript interface for communication between WebView and native code
 */
@Suppress("unused") // Methods are called from JavaScript
class YouTubePlayerJSInterface(
    private val onReady: () -> Unit,
    private val onStateChange: (PlayerState) -> Unit,
    private val onError: (PlayerError) -> Unit
) {
    @JavascriptInterface
    fun onPlayerReady() {
        onReady()
    }

    @JavascriptInterface
    fun onPlayerStateChange(state: Int) {
        onStateChange(PlayerState.fromInt(state))
    }

    @JavascriptInterface
    fun onPlayerError(errorCode: Int) {
        onError(PlayerError.fromInt(errorCode))
    }
}

/**
 * Controller for YouTube player operations
 */
@Suppress("unused") // Public API for controlling player
class YouTubePlayerController(private val webView: WebView) {
    fun play() {
        webView.evaluateJavascript("player.playVideo();", null)
    }

    fun pause() {
        webView.evaluateJavascript("player.pauseVideo();", null)
    }

    fun stop() {
        webView.evaluateJavascript("player.stopVideo();", null)
    }

    fun seekTo(seconds: Float) {
        webView.evaluateJavascript("player.seekTo($seconds, true);", null)
    }

    fun mute() {
        webView.evaluateJavascript("player.mute();", null)
    }

    fun unMute() {
        webView.evaluateJavascript("player.unMute();", null)
    }

    fun setVolume(volume: Int) {
        webView.evaluateJavascript("player.setVolume($volume);", null)
    }
}

/**
 * Player states from YouTube IFrame API
 * Reference: https://developers.google.com/youtube/iframe_api_reference#onStateChange
 */
enum class PlayerState(val value: Int) {
    UNSTARTED(-1),
    ENDED(0),
    PLAYING(1),
    PAUSED(2),
    BUFFERING(3),
    VIDEO_CUED(5);

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: UNSTARTED
    }
}

/**
 * Player errors from YouTube IFrame API
 * Reference: https://developers.google.com/youtube/iframe_api_reference#onError
 */
enum class PlayerError(val value: Int) {
    INVALID_PARAMETER(2),
    HTML5_ERROR(5),
    VIDEO_NOT_FOUND(100),
    NOT_ALLOWED_EMBEDDED(101),
    NOT_ALLOWED_EMBEDDED_ALT(150),
    UNKNOWN(-1);

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: UNKNOWN
    }
}

/**
 * Build HTML with YouTube IFrame API
 * Reference: https://developers.google.com/youtube/iframe_api_reference
 */
private fun buildYouTubeIFrameHTML(
    videoId: String,
    autoplay: Boolean,
    showControls: Boolean
): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        * { 
            margin: 0; 
            padding: 0; 
            box-sizing: border-box;
        }
        html, body { 
            width: 100%;
            height: 100%;
            background: #000; 
            overflow: hidden;
        }
        #player {
            width: 100%;
            height: 100%;
        }
    </style>
</head>
<body>
    <div id="player"></div>
    <script>
        // Load YouTube IFrame API
        var tag = document.createElement('script');
        tag.src = "https://www.youtube.com/iframe_api";
        var firstScriptTag = document.getElementsByTagName('script')[0];
        firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

        var player;
        
        // This function is called automatically when IFrame API is ready
        function onYouTubeIframeAPIReady() {
            player = new YT.Player('player', {
                height: '100%',
                width: '100%',
                videoId: '$videoId',
                playerVars: {
                    'autoplay': ${if (autoplay) 1 else 0},
                    'controls': ${if (showControls) 1 else 0},
                    'modestbranding': 1,
                    'rel': 0,
                    'playsinline': 1,
                    'fs': 1,
                    'cc_load_policy': 1,
                    'iv_load_policy': 3,
                    'enablejsapi': 1,
                    'origin': window.location.origin
                },
                events: {
                    'onReady': onPlayerReady,
                    'onStateChange': onPlayerStateChange,
                    'onError': onPlayerError
                }
            });
        }

        function onPlayerReady(event) {
            console.log('YouTube player ready');
            if (typeof Android !== 'undefined') {
                Android.onPlayerReady();
            }
        }

        function onPlayerStateChange(event) {
            console.log('Player state changed to: ' + event.data);
            if (typeof Android !== 'undefined') {
                Android.onPlayerStateChange(event.data);
            }
        }

        function onPlayerError(event) {
            console.error('Player error: ' + event.data);
            if (typeof Android !== 'undefined') {
                Android.onPlayerError(event.data);
            }
        }
    </script>
</body>
</html>
    """.trimIndent()
}
