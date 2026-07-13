package com.canvasstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canvasstudio.data.local.entity.ProjectEntity
import com.canvasstudio.ui.project.ProjectUiState
import com.canvasstudio.ui.project.ProjectViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ProjectViewModel by viewModels { ProjectViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    ProjectScreen(
                        uiState = uiState,
                        onAddProject = { name -> viewModel.insertProject(name) },
                        onDeleteProject = { project -> viewModel.deleteProject(project) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectScreen(
    uiState: ProjectUiState,
    onAddProject: (String) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var projectName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text("Nome do Projeto") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (projectName.isNotBlank()) {
                        onAddProject(projectName)
                        projectName = ""
                    }
                }
            ) {
                Text("Adicionar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is ProjectUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProjectUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.projects) { project ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = project.name, style = MaterialTheme.typography.body1)
                                IconButton(onClick = { onDeleteProject(project) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Deletar Projeto"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is ProjectUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Erro: ${uiState.message}", color = MaterialTheme.colors.error)
                }
            }
        }
    }
}