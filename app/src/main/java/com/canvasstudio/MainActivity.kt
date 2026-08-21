package com.canvasstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.canvasstudio.data.local.AppDatabase
import com.canvasstudio.ui.block.BlockScreen
import com.canvasstudio.ui.block.BlockViewModel
import com.canvasstudio.data.local.preferences.UserPreferencesManager
import com.canvasstudio.data.repository.BlockRepository
import com.canvasstudio.ui.theme.CanvasStudioTheme
import kotlinx.coroutines.delay

import android.content.Intent
import android.net.Uri

class MainActivity : ComponentActivity() {
    private val blockViewModel: BlockViewModel by viewModels { 
        ViewModelFactory((application as CanvasApplication).container.blockRepository, (application as CanvasApplication).container.userPreferencesManager) 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)

        setContent {
            val isIncomingShare = intent?.action in listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE, Intent.ACTION_VIEW)
            var showSplash by remember { mutableStateOf(!isIncomingShare) }
            val isDarkMode by blockViewModel.isDarkMode.collectAsStateWithLifecycle()

            CanvasStudioTheme(darkTheme = isDarkMode) {
                Surface(color = MaterialTheme.colors.background) {
                    if (showSplash) {
                        SplashScreen(onFinished = { showSplash = false })
                    } else {
                        val uiState by blockViewModel.uiState.collectAsStateWithLifecycle()
                        BlockScreen(
                            uiState = uiState,
                            viewModel = blockViewModel,
                            onBack = { finish() }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun extractUris(intent: Intent?): List<Uri> {
        if (intent == null) return emptyList()
        val uris = mutableListOf<Uri>()

        // 1. ClipData (Formato primário moderno do Android para compartilhamento)
        intent.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i)?.uri?.let { uris.add(it) }
            }
        }

        // 2. EXTRA_STREAM (Single)
        if (uris.isEmpty()) {
            val streamUri = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                }
            } catch (e: Exception) {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            }
            streamUri?.let { uris.add(it) }
        }

        // 3. EXTRA_STREAM (Multiple)
        if (uris.isEmpty()) {
            try {
                val list = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                }
                list?.forEach { if (it != null) uris.add(it) }
            } catch (e: Exception) {}
        }

        // 4. Intent Data
        if (uris.isEmpty()) {
            intent.data?.let { uris.add(it) }
        }

        return uris.distinct()
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE && action != Intent.ACTION_VIEW) {
            return
        }

        android.util.Log.d("CanvasStudio", "handleIncomingIntent: action=$action, type=${intent.type}")
        val type = intent.type ?: ""
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        val uris = extractUris(intent)

        android.util.Log.d("CanvasStudio", "handleIncomingIntent: found ${uris.size} uris: $uris")

        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                blockViewModel.importSharedUri(uri, type, applicationContext)
            }
        } else if (!text.isNullOrBlank()) {
            blockViewModel.importTextShared(text, subject)
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = Color(0xFF0A84FF),
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Canvas Studio",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Modular & Sync",
                color = Color(0xFF0A84FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

class ViewModelFactory(
    private val blockRepository: BlockRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BlockViewModel(blockRepository, preferencesManager) as T
    }
}
