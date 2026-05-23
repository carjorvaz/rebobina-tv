package com.carjorvaz.rebobina

import android.net.Uri

data class CatchupStreamRouteFields(
    val epgItem: String,
    val id: String,
    val name: String,
    val logo: String,
) {
    val isComplete: Boolean
        get() = epgItem.isNotBlank() &&
            id.isNotBlank() &&
            name.isNotBlank() &&
            logo.isNotBlank()
}

object ProviderRouteComposer {
    const val SCHEME: String = "digitv"

    fun flashback(): String =
        buildRoute("u7d")

    fun liveChannel(channelId: String): String =
        buildRoute("livestream", channelId)

    fun catchupStream(fields: CatchupStreamRouteFields): String =
        buildRoute(
            "catchupstream",
            fields.epgItem,
            fields.id,
            fields.name,
            fields.logo,
        )

    private fun buildRoute(root: String, vararg segments: String): String {
        val builder = Uri.Builder()
            .scheme(SCHEME)
            .authority(root.trim())
        segments.forEach { builder.appendPath(it.trim()) }
        return builder.build().toString()
    }
}
