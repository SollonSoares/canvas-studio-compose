package com.canvasstudio.ui.block.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canvasstudio.designsystem.components.CanvasButton
import com.canvasstudio.designsystem.components.CanvasButtonVariant
import com.canvasstudio.designsystem.components.CanvasModal
import com.canvasstudio.domain.model.CanvasConfig
import com.canvasstudio.ui.theme.CanvasColors

@Composable
fun SettingsModal(
    title: String,
    onTitleChange: (String) -> Unit,
    authorName: String = "",
    onAuthorNameChange: (String) -> Unit = {},
    themeStyle: String,
    onThemeStyleChange: (String) -> Unit,
    galleryBaseUrl: String,
    onGalleryBaseUrlChange: (String) -> Unit,
    githubToken: String,
    onGithubTokenChange: (String) -> Unit,
    modules: Map<String, Boolean>,
    onToggleModule: (String, Boolean) -> Unit,
    config: CanvasConfig,
    onToggleAutoOcr: (Boolean) -> Unit,
    onToggleFinancialBadges: (Boolean) -> Unit,
    onTogglePartyDetails: (Boolean) -> Unit,
    onToggleFitAspectRatio: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    colors: CanvasColors
) {
    CanvasModal(
        title = "Configurações do Studio",
        onDismiss = onDismiss,
        confirmButton = {
            CanvasButton(
                text = "Concluir",
                onClick = onDismiss,
                variant = CanvasButtonVariant.Primary
            )
        }
    ) {
        SettingsGeneralSection(
            title = title,
            onTitleChange = onTitleChange,
            authorName = authorName,
            onAuthorNameChange = onAuthorNameChange,
            galleryBaseUrl = galleryBaseUrl,
            onGalleryBaseUrlChange = onGalleryBaseUrlChange,
            githubToken = githubToken,
            onGithubTokenChange = onGithubTokenChange,
            colors = colors
        )

        Spacer(Modifier.height(12.dp))
        TextSectionHeader("ESTILO VISUAL DO DESIGN SYSTEM", colors)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasButton(
                text = "🍏 Cupertino",
                onClick = { onThemeStyleChange("cupertino") },
                variant = if (themeStyle == "cupertino") CanvasButtonVariant.Primary else CanvasButtonVariant.Outlined,
                modifier = Modifier.weight(1f)
            )
            CanvasButton(
                text = "🎛️ Teenage",
                onClick = { onThemeStyleChange("teenage") },
                variant = if (themeStyle == "teenage") CanvasButtonVariant.Primary else CanvasButtonVariant.Outlined,
                modifier = Modifier.weight(1f)
            )
        }

        SettingsTogglesSection(
            modules = modules,
            onToggleModule = onToggleModule,
            config = config,
            onToggleAutoOcr = onToggleAutoOcr,
            onToggleFinancialBadges = onToggleFinancialBadges,
            onTogglePartyDetails = onTogglePartyDetails,
            onToggleFitAspectRatio = onToggleFitAspectRatio,
            colors = colors
        )
    }
}
