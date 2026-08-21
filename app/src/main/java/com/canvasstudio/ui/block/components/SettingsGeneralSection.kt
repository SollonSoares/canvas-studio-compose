package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canvasstudio.designsystem.components.CanvasTextField
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SettingsGeneralSection(
    title: String,
    onTitleChange: (String) -> Unit,
    galleryBaseUrl: String,
    onGalleryBaseUrlChange: (String) -> Unit,
    githubToken: String,
    onGithubTokenChange: (String) -> Unit,
    colors: CanvasColors
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextSectionHeader("IDENTIDADE & WORKSPACE", colors)
        CanvasTextField(
            value = title,
            onValueChange = onTitleChange,
            label = "Título do App / Ficha",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        TextSectionHeader("INTEGRAÇÃO COM GITHUB & GALERIA", colors)
        CanvasTextField(
            value = galleryBaseUrl,
            onValueChange = onGalleryBaseUrlChange,
            label = "URL Base da Galeria Web",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        CanvasTextField(
            value = githubToken,
            onValueChange = onGithubTokenChange,
            label = "Personal Access Token (GitHub)",
            placeholder = "ghp_...",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
