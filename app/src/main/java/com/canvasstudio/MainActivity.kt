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

class MainActivity : ComponentActivity() {
    private val blockViewModel: BlockViewModel by viewModels { 
        ViewModelFactory((application as CanvasApplication).container.blockRepository, (application as CanvasApplication).container.userPreferencesManager) 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var showSplash by remember { mutableStateOf(true) }
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
