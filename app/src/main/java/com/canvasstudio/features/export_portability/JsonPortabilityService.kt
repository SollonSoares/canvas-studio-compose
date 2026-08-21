package com.canvasstudio.features.export_portability

import com.canvasstudio.data.local.entity.BlockEntity

/**
 * Fachada pública unificada de portabilidade JSON compatível com a Web.
 */
object JsonPortabilityService {

    fun exportToJson(brandTitle: String, blocks: List<BlockEntity>): String {
        return JsonBlockExporter.exportToJson(brandTitle, blocks)
    }

    fun parseBlocksFromJson(jsonString: String, projectId: Long): Pair<String?, List<BlockEntity>> {
        return JsonBlockParser.parseBlocksFromJson(jsonString, projectId)
    }
}
