package com.carjorvaz.rebobina

import android.content.Context
import org.json.JSONObject

data class CatchupCatalog(
    val days: List<CatchupDay>,
    val channels: List<CatchupChannel>,
    val programmes: List<CatchupProgramme>,
) {
    fun programmesFor(dayId: String, channelId: String): List<CatchupProgramme> =
        programmes
            .filter { it.dayId == dayId && it.channelId == channelId }
            .sortedWith(compareBy<CatchupProgramme> { it.start }.thenBy { it.title })

    fun programme(id: String?): CatchupProgramme? =
        programmes.firstOrNull { it.id == id }

    fun channel(id: String): CatchupChannel? =
        channels.firstOrNull { it.id == id }

    fun movieProgrammes(): List<CatchupProgramme> =
        programmes
            .filter { it.kind == "movie" }
            .sortedWith(
                compareBy<CatchupProgramme> { it.title.lowercase() }
                    .thenByDescending { it.dayId }
                    .thenBy { it.start },
            )

    fun seriesGroups(): List<CatchupSeriesGroup> =
        programmes
            .filter { it.isSeries }
            .groupBy { it.seriesGroupId }
            .mapNotNull { (id, episodes) ->
                val sortedEpisodes = episodes.sortedForSeriesBrowsing()
                val title = sortedEpisodes.firstOrNull()?.seriesDisplayTitle.orEmpty()
                if (title.isBlank()) {
                    null
                } else {
                    CatchupSeriesGroup(
                        id = id,
                        title = title,
                        episodes = sortedEpisodes,
                    )
                }
            }
            .sortedWith(compareBy<CatchupSeriesGroup> { it.title.lowercase() })

    fun episodesForSeries(seriesGroupId: String): List<CatchupProgramme> =
        seriesGroups().firstOrNull { it.id == seriesGroupId }?.episodes.orEmpty()
}

data class CatchupDay(
    val id: String,
    val label: String,
    val subtitle: String,
)

data class CatchupChannel(
    val id: String,
    val name: String,
    val number: Int,
    val badge: String,
)

data class CatchupProgramme(
    val id: String,
    val dayId: String,
    val channelId: String,
    val start: String,
    val end: String,
    val title: String,
    val subtitle: String,
    val kind: String,
    val seriesTitle: String,
    val season: Int?,
    val episode: Int?,
    val description: String,
    val progressPercent: Int,
    val previousProgrammeId: String,
    val nextProgrammeId: String,
    val providerRoute: String,
    val candidateProviderRoute: String,
    val accent: Int,
) {
    val isSeries: Boolean
        get() = kind == "series" || season != null || episode != null || seriesTitle.isNotBlank()

    val seriesDisplayTitle: String
        get() = seriesTitle.ifBlank { title }

    val seriesGroupId: String
        get() = seriesDisplayTitle.trim().lowercase()

    val episodeLabel: String
        get() = listOfNotNull(
            season?.let { "T$it" },
            episode?.let { "E$it" },
        ).joinToString(" ")
}

data class CatchupSeriesGroup(
    val id: String,
    val title: String,
    val episodes: List<CatchupProgramme>,
)

interface CatchupCatalogSource {
    val sourceName: String
    fun load(context: Context): CatchupCatalog
}

object CatchupCatalogLoader {
    fun load(context: Context): CatchupCatalog =
        AssetCatchupCatalogSource().load(context)
}

class AssetCatchupCatalogSource(
    private val assetName: String = "sample-catchup.json",
) : CatchupCatalogSource {
    override val sourceName: String = "asset:$assetName"

    override fun load(context: Context): CatchupCatalog {
        val json = context.assets.open(assetName).bufferedReader().use {
            it.readText()
        }
        return CatchupCatalogJsonParser.parse(json)
    }
}

object CatchupCatalogJsonParser {
    fun parse(json: String): CatchupCatalog {
        val root = JSONObject(json)
        val days = root.getJSONArray("days").objects().map {
            CatchupDay(
                id = it.getString("id"),
                label = it.getString("label"),
                subtitle = it.getString("subtitle"),
            )
        }
        val channels = root.getJSONArray("channels").objects().map {
            CatchupChannel(
                id = it.getString("id"),
                name = it.getString("name"),
                number = it.getInt("number"),
                badge = it.getString("badge"),
            )
        }.sortedBy { it.number }
        val programmes = root.getJSONArray("programmes").objects().map {
            CatchupProgramme(
                id = it.getString("id"),
                dayId = it.getString("dayId"),
                channelId = it.getString("channelId"),
                start = it.getString("start"),
                end = it.getString("end"),
                title = it.getString("title"),
                subtitle = it.optString("subtitle"),
                kind = it.optString("kind"),
                seriesTitle = it.optString("seriesTitle"),
                season = it.optNullableInt("season"),
                episode = it.optNullableInt("episode"),
                description = it.optString("description"),
                progressPercent = it.optInt("progressPercent"),
                previousProgrammeId = it.optString("previousProgrammeId"),
                nextProgrammeId = it.optString("nextProgrammeId"),
                providerRoute = it.optString("providerRoute", ProviderRouteComposer.flashback()),
                candidateProviderRoute = it.optString("candidateProviderRoute"),
                accent = parseColor(it.optString("accent")),
            )
        }
        return CatchupCatalog(days, channels, programmes)
    }
}

private fun parseColor(value: String): Int =
    runCatching { android.graphics.Color.parseColor(value) }
        .getOrDefault(android.graphics.Color.rgb(229, 184, 77))

private fun org.json.JSONArray.objects(): List<JSONObject> =
    (0 until length()).map { getJSONObject(it) }

private fun JSONObject.optNullableInt(name: String): Int? =
    if (has(name) && !isNull(name)) optInt(name) else null

private fun List<CatchupProgramme>.sortedForSeriesBrowsing(): List<CatchupProgramme> =
    sortedWith(
        compareBy<CatchupProgramme> { it.season ?: Int.MAX_VALUE }
            .thenBy { it.episode ?: Int.MAX_VALUE }
            .thenByDescending { it.dayId }
            .thenBy { it.start },
    )
