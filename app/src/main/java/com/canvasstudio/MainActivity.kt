package com.canvasstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.canvasstudio.data.local.AppDatabase
import com.canvasstudio.ui.block.BlockScreen
import com.canvasstudio.ui.block.BlockUiState
import com.canvasstudio.ui.block.BlockViewModel

class MainActivity : ComponentActivity() {
    // Instancia o banco diretamente aqui para evitar erros de AppContainer
    private val database by lazy { Room.databaseBuilder(applicationContext, AppDatabase::class.java, "canvas_db").build() }
    private val blockViewModel: BlockViewModel by viewModels { ViewModelFactory(database) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
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

class ViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BlockViewModel(database.blockDao()) as T
    }
}