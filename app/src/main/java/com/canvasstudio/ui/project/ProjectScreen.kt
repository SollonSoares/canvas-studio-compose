package com.canvasstudio.ui.project 
 
import androidx.compose.foundation.clickable 
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
import com.canvasstudio.data.local.entity.ProjectEntity 
 
@Composable 
fun ProjectScreen(uiState: ProjectUiState, onAddProject: (String) -> Unit, onDeleteProject: (ProjectEntity) -> Unit, onProjectClick: (Long) -> Unit, modifier: Modifier = Modifier) { 
    var projectName by remember { mutableStateOf("^") } 
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) { 
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { 
            OutlinedTextField(value = projectName, onValueChange = { projectName = it }, label = { Text("Nome do Projeto") }, modifier = Modifier.weight(1f)) 
            Spacer(modifier = Modifier.width(8.dp)) 
            Button(onClick = { if (projectName.isNotBlank()) { onAddProject(projectName); projectName = "^" } }) { Text("Adicionar") } 
        } 
        Spacer(modifier = Modifier.height(16.dp)) 
        when (uiState) { 
            is ProjectUiState.Loading -> { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } } 
            is ProjectUiState.Error -> { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = "Erro: ${uiState.message}", color = MaterialTheme.colors.error) } } 
            is ProjectUiState.Success -> { 
                LazyColumn(modifier = Modifier.fillMaxSize()) { 
                    items(uiState.projects) { project -> 
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onProjectClick(project.id) }, elevation = 2.dp) { 
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { 
                                Text(text = project.name, style = MaterialTheme.typography.body1) 
                                IconButton(onClick = { onDeleteProject(project) }) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Deletar") } 
                            } 
                        } 
                    } 
                } 
            } 
        } 
    } 
} 
