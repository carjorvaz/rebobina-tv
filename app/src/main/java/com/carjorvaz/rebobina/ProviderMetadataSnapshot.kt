package com.carjorvaz.rebobina

import android.content.Context
import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

class ProviderMetadataSnapshotSource(
    private val assetName: String = "sample-provider-metadata.json",
) : CatchupCatalogSource {
    override val sourceName: String = "provider-snapshot:$assetName"

    override fun load(context: Context): CatchupCatalog {
        val json = context.assets.open(assetName).bufferedReader().use {
            it.readText()
        }
        return ProviderMetadataSnapshotParser.parse(json)
    }
}

object ProviderMetadataSnapshotParser {
    private val DEFAULT_PROVIDER_ROUTE = ProviderRouteComposer.flashback()

    fun parse(json: String): CatchupCatalog {
        val root = JSONObject(json)
        val dates = root.getJSONArray("dates").jsonObjects()
        val details = root.optJSONArray("eventDetails")
            ?.jsonObjects()
            ?.associateBy { eventKey(it.optString("common_id"), it.optString("id")) }
            .orEmpty()

        val channelsById = linkedMapOf<String, CatchupChannel>()
        val days = dates.map { date ->
            date.optJSONArray("channels")?.jsonObjects().orEmpty().forEach { channel ->
                val channelId = channel.getString("common_id")
                channelsById.putIfAbsent(
                    channelId,
                    CatchupChannel(
                        id = channelId,
                        name = channel.optString("name", channelId),
                        number = channel.optInt("number", channelsById.size + 1),
                        badge = channel.optString("badge").ifBlank {
                            compactBadge(channel.optString("name", channelId))
                        },
                    ),
                )
            }
            CatchupDay(
                id = date.getString("date"),
                label = date.optString("label", date.getString("date")),
                subtitle = date.optString("subtitle"),
            )
        }

        val programmes = dates.flatMap { date ->
            val dayId = date.getString("date")
            date.optJSONArray("channels")?.jsonObjects().orEmpty().flatMap { channel ->
                val fallbackChannelId = channel.getString("common_id")
                channel.optJSONArray("events")?.jsonObjects().orEmpty().map { event ->
                    val channelId = event.optString("common_id", fallbackChannelId)
                    val detail = details[eventKey(channelId, event.getString("id"))]
                    event.toProgramme(dayId, channelId, detail)
                }
            }
        }

        return CatchupCatalog(
            days = days,
            channels = channelsById.values.sortedBy { it.number },
            programmes = linkEpisodeNeighbours(programmes),
        )
    }

    private fun JSONObject.toProgramme(
        dayId: String,
        channelId: String,
        detail: JSONObject?,
    ): CatchupProgramme {
        val eventId = getString("id")
        val seasonLabel = detail?.optString("seasonLabel").orEmpty()
            .ifBlank { optString("seasonLabel") }
        val episodeLabel = detail?.optString("episodeLabel").orEmpty()
            .ifBlank { optString("episodeLabel") }
        val kind = normalizeKind(detail?.optString("category").orEmpty().ifBlank {
            optString("category")
        })
        return CatchupProgramme(
            id = stableProgrammeId(channelId, eventId),
            dayId = dayId,
            channelId = channelId,
            start = displayTime(optString("start_time")),
            end = displayTime(optString("end_time")),
            title = detail?.optString("title").orEmpty().ifBlank { getString("title") },
            subtitle = firstLabel().ifBlank { displayKind(kind) },
            kind = kind,
            seriesTitle = detail?.optString("seriesTitle").orEmpty().ifBlank {
                optString("seriesTitle")
            },
            season = numberFromLabel(seasonLabel),
            episode = numberFromLabel(episodeLabel),
            description = detail?.optString("description").orEmpty().ifBlank {
                optString("description")
            },
            progressPercent = detail?.watchProgress() ?: watchProgress(),
            previousProgrammeId = "",
            nextProgrammeId = "",
            providerRoute = optString("providerRoute", DEFAULT_PROVIDER_ROUTE),
            candidateProviderRoute = candidateProviderRoute(detail),
            accent = accentForKind(kind),
        )
    }

    private fun JSONObject.candidateProviderRoute(detail: JSONObject?): String {
        val fields = detail?.catchupStreamRouteFields() ?: catchupStreamRouteFields()
        return fields
            ?.takeIf { it.isComplete }
            ?.let(ProviderRouteComposer::catchupStream)
            .orEmpty()
    }

    private fun JSONObject.catchupStreamRouteFields(): CatchupStreamRouteFields? =
        optJSONObject("routeFields")
            ?.optJSONObject("catchupstream")
            ?.let {
                CatchupStreamRouteFields(
                    epgItem = it.optString("epgItem"),
                    id = it.optString("id"),
                    name = it.optString("name"),
                    logo = it.optString("logo"),
                )
            }

    private fun linkEpisodeNeighbours(programmes: List<CatchupProgramme>): List<CatchupProgramme> {
        val previousById = mutableMapOf<String, String>()
        val nextById = mutableMapOf<String, String>()
        programmes
            .filter { it.seriesTitle.isNotBlank() && it.season != null && it.episode != null }
            .groupBy { "${it.seriesTitle.lowercase()}|${it.season}" }
            .values
            .forEach { series ->
                val ordered = series.sortedWith(
                    compareBy<CatchupProgramme> { it.season ?: 0 }
                        .thenBy { it.episode ?: 0 }
                        .thenBy { it.dayId }
                        .thenBy { it.start },
                )
                ordered.forEachIndexed { index, programme ->
                    ordered.getOrNull(index - 1)?.let { previousById[programme.id] = it.id }
                    ordered.getOrNull(index + 1)?.let { nextById[programme.id] = it.id }
                }
            }
        return programmes.map {
            it.copy(
                previousProgrammeId = previousById[it.id].orEmpty(),
                nextProgrammeId = nextById[it.id].orEmpty(),
            )
        }
    }

    private fun JSONObject.watchProgress(): Int =
        optJSONObject("watchInfo")?.optInt("progress") ?: optInt("progress")

    private fun JSONObject.firstLabel(): String =
        optJSONArray("labels")?.takeIf { it.length() > 0 }?.optString(0).orEmpty()

    private fun displayTime(value: String): String =
        when {
            value.length >= 16 && value[10] == 'T' -> value.substring(11, 16)
            value.length >= 5 -> value.substring(0, 5)
            else -> ""
        }

    private fun normalizeKind(value: String): String =
        when (value.lowercase()) {
            "movie", "filme" -> "movie"
            "sport", "desporto" -> "sport"
            "series", "serie" -> "series"
            "news", "informacao" -> "news"
            else -> value.ifBlank { "programme" }
        }

    private fun displayKind(kind: String): String =
        when (kind) {
            "movie" -> "Filme"
            "sport" -> "Desporto"
            "series" -> "Serie"
            "news" -> "Informacao"
            else -> "Programa"
        }

    private fun numberFromLabel(label: String): Int? =
        Regex("""\d+""").find(label)?.value?.toIntOrNull()

    private fun stableProgrammeId(channelId: String, eventId: String): String =
        "provider-${slug(channelId)}-${slug(eventId)}"

    private fun eventKey(channelId: String, eventId: String): String =
        "${channelId.trim()}|${eventId.trim()}"

    private fun slug(value: String): String =
        value.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "unknown" }

    private fun compactBadge(name: String): String =
        name.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString("") { it.first().uppercase() }
            .take(3)
            .ifBlank { "TV" }

    private fun accentForKind(kind: String): Int =
        when (kind) {
            "movie" -> Color.rgb(94, 124, 226)
            "sport" -> Color.rgb(45, 156, 219)
            "series" -> Color.rgb(242, 153, 74)
            "news" -> Color.rgb(39, 174, 96)
            else -> Color.rgb(229, 184, 77)
        }
}

private fun JSONArray.jsonObjects(): List<JSONObject> =
    (0 until length()).map { getJSONObject(it) }
