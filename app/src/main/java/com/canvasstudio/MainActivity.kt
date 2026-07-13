package com.canvasstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canvasstudio.ui.project.ProjectScreen
import com.canvasstudio.ui.project.ProjectViewModel
import com.canvasstudio.ui.block.BlockScreen
import com.canvasstudio.ui.block.BlockViewModel
import com.canvasstudio.data.local.entity.BlockEntity

class MainActivity : ComponentActivity() {

    private val projectViewModel: ProjectViewModel by viewModels { ProjectViewModel.Factory }
    private val blockViewModel: BlockViewModel by viewModels { BlockViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    var currentProjectId by remember { mutableStateOf<Long?>(null) }

                    if (currentProjectId == null) {
                        val projectUiState by projectViewModel.uiState.collectAsStateWithLifecycle()
                        ProjectScreen(
                            uiState = projectUiState,
                            onAddProject = { projectViewModel.insertProject(it) },
                            onDeleteProject = { projectViewModel.deleteProject(it) },
                            onProjectClick = { projectId ->
                                blockViewModel.setProject(projectId)
                                currentProjectId = projectId
                            }
                        )
                    } else {
                        val blockUiState by blockViewModel.uiState.collectAsStateWithLifecycle()
                        BlockScreen(
                            uiState = blockUiState,
                            onUpdateBlock = { blockViewModel.updateBlock(it) },
                            onDeleteBlock = { blockViewModel.deleteBlock(it) },
                            onAddBlock = {
                                blockViewModel.insertBlock(
                                    BlockEntity(
                                        id = 0,
                                        projectId = currentProjectId!!,
                                        title = "Novo Bloco",
                                        type = "Código",
                                        posX = 100.toFloat(),
                                        posY = 100.toFloat(),
                                        width = 150,
                                        height = 100,
                                        contentJson = "{}"
                                    )
                                )
                            },
                            onBack = { currentProjectId = null }
                        )
                    }
                }
            }
        }
    }
}