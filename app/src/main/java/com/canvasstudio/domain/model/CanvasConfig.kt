package com.canvasstudio.domain.model

data class CanvasConfig(
    val autoOcrEnabled: Boolean = true,
    val showFinancialBadges: Boolean = true,
    val showPartyDetails: Boolean = true,
    val fitOriginalAspectRatio: Boolean = true,
    val enabledModules: Map<String, Boolean> = mapOf(
        "text" to true,
        "image" to true,
        "chart" to true,
        "receipt" to true
    )
)
