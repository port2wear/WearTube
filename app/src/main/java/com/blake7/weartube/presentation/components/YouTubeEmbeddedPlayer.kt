package com.blake7.weartube.presentation.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.webkit.WebChromeClient
import androidx.core.net.toUri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebSettings
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.compose.material.*

@Composable
fun YouTubeEmbeddedPlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = false,
    showControls: Boolean = true,
    customEmbedUrl: String? = null,
    onReady: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var hasWebViewError by remember { mutableStateOf(false) }
    var webViewSupported by remember { mutableStateOf(true) }

    // Check if WebView is supported on this device
    LaunchedEffect(Unit) {
        try {
            // Try to create a WebView to test if it's supported
            val testWebView = WebView(context)
            testWebView.destroy()
            webViewSupported = true
            Log.d("YouTubePlayer", "WebView is supported on this device")
        } catch (e: Exception) {
            webViewSupported = false
            hasWebViewError = true
            Log.e("YouTubePlayer", "WebView not supported on this device", e)
            onError("WebView not supported on this device")
        }
    }

    if (!webViewSupported || hasWebViewError) {
        // Fallback UI when WebView is not supported
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        openYouTubeExternal(context, videoId)
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = "▶️",
                    fontSize = 48.sp,
                    color = androidx.compose.ui.graphics.Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Open in YouTube",
                    style = MaterialTheme.typography.caption1,
                    color = androidx.compose.ui.graphics.Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to watch video",
                    style = MaterialTheme.typography.caption2,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                Log.d("YouTubePlayer", "Creating WebView for video: $videoId")
                val container = FrameLayout(ctx)
                
                try {
                    // Check WebView availability more thoroughly
                    if (!isWebViewAvailable(ctx)) {
                        throw Exception("WebView package not available")
                    }
                    
                    val webView = WebView(ctx).apply {
                        // Enhanced WebView settings
                        @SuppressLint("SetJavaScriptEnabled") // Required for YouTube player
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = false
                            allowContentAccess = false
                            
                            // Media settings
                            mediaPlaybackRequiresUserGesture = false
                            
                            // Security and compatibility
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                            
                            // Viewport and rendering
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false
                            
                            // User agent (helps with compatibility)
                            userAgentString = settings.userAgentString + " WearTubeApp/1.0"
                        }

                        // Enable WebView debugging in debug builds
                        WebView.setWebContentsDebuggingEnabled(true)

                        // Set background
                        setBackgroundColor(Color.TRANSPARENT)
                        
                        // Set layer type based on device capabilities
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                Log.d("YouTubePlayer", "Page started loading: $url")
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                Log.d("YouTubePlayer", "Page finished loading: $url")
                                onReady()
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                val errorMsg = error?.description?.toString() ?: "Unknown WebView error"
                                Log.e("YouTubePlayer", "WebView error: $errorMsg for ${request?.url}")
                                
                                // Only report main frame errors
                                if (request?.isForMainFrame == true) {
                                    hasWebViewError = true
                                    onError(errorMsg)
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString()
                                Log.d("YouTubePlayer", "Navigation attempt: $url")
                                
                                // Allow YouTube embed URLs, block external navigation
                                return when {
                                    url?.contains("youtube.com/embed") == true -> false
                                    url?.contains("youtube.com") == true -> false
                                    url?.contains("googlevideo.com") == true -> false
                                    else -> {
                                        Log.w("YouTubePlayer", "Blocking navigation to: $url")
                                        true
                                    }
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                Log.d("YouTubePlayer", "Loading progress: $newProgress%")
                            }
                            
                            override fun onConsoleMessage(message: android.webkit.ConsoleMessage?): Boolean {
                                Log.d("YouTubePlayer", "Console: ${message?.message()}")
                                return true
                            }
                        }
                    }
                    
                    container.addView(webView, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ))
                    
                    Log.d("YouTubePlayer", "WebView created successfully")
                    
                } catch (t: Throwable) {
                    Log.e("YouTubePlayer", "Failed to create WebView", t)
                    hasWebViewError = true
                    onError("WebView initialization failed: ${t.message}")
                    
                    // Add error message view
                    @SuppressLint("SetTextI18n") // Error message doesn't need translation
                    val errorText = TextView(ctx).apply {
                        text = "Video playback not supported on this device"
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        textSize = 12f
                    }
                    container.addView(errorText, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ))
                }
                
                container
            },
            update = { view ->
                if (!hasWebViewError) {
                    try {
                        val child = view.getChildAt(0)
                        if (child is WebView) {
                            val embedUrl = customEmbedUrl ?: buildYouTubeEmbedUrl(videoId, autoplay, showControls)
                            Log.d("YouTubePlayer", "Loading YouTube IFrame API")
                            
                            // Load the HTML with YouTube IFrame API
                            if (embedUrl.startsWith("data:text/html")) {
                                // Load base64 encoded HTML
                                child.loadUrl(embedUrl)
                            } else {
                                // Load custom URL directly
                                child.loadUrl(embedUrl)
                            }
                        }
                    } catch (t: Throwable) {
                        Log.e("YouTubePlayer", "Failed to load URL", t)
                        hasWebViewError = true
                        onError("Failed to load video: ${t.message}")
                    }
                }
            }
        )
    }
}

// Helper function to check WebView availability
private fun isWebViewAvailable(context: Context): Boolean {
    return try {
        val packageManager = context.packageManager
        val webViewPackage = WebView.getCurrentWebViewPackage()
        webViewPackage != null && packageManager.getApplicationInfo(webViewPackage.packageName, 0).enabled
    } catch (e: Exception) {
        Log.e("YouTubePlayer", "WebView availability check failed", e)
        false
    }
}

// Helper function to open YouTube externally
@SuppressLint("QueryPermissionsNeeded") // Standard YouTube intents
private fun openYouTubeExternal(context: Context, videoId: String) {
    try {
        // Try YouTube app first
        val appIntent = Intent(Intent.ACTION_VIEW, "vnd.youtube:$videoId".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(appIntent)
    } catch (_: Exception) {
        try {
            // Fallback to browser
            val webIntent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$videoId".toUri()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Log.e("YouTubePlayer", "Failed to open YouTube", e2)
        }
    }
}

private fun buildYouTubeEmbedUrl(
    videoId: String,
    autoplay: Boolean = false,
    showControls: Boolean = true,
    modest: Boolean = true,
    rel: Boolean = false
): String {
    // Build HTML page with YouTube IFrame API
    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; }
                body { 
                    background: #000; 
                    overflow: hidden;
                }
                #player {
                    width: 100%;
                    height: 100vh;
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
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        videoId: '$videoId',
                        playerVars: {
                            'autoplay': ${if (autoplay) 1 else 0},
                            'controls': ${if (showControls) 1 else 0},
                            'modestbranding': ${if (modest) 1 else 0},
                            'rel': ${if (rel) 1 else 0},
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
                    console.log('Player ready');
                    // Auto play if enabled
                    ${if (autoplay) "event.target.playVideo();" else ""}
                }

                function onPlayerStateChange(event) {
                    console.log('Player state:', event.data);
                }

                function onPlayerError(event) {
                    console.error('Player error:', event.data);
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    Log.d("YouTubePlayer", "Built YouTube IFrame API HTML for video: $videoId")
    return "data:text/html;base64,${android.util.Base64.encodeToString(html.toByteArray(), android.util.Base64.NO_PADDING)}"
}
