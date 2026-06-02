package com.carjorvaz.rebobina

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private lateinit var catalog: CatchupCatalog
    private lateinit var statusText: TextView
    private lateinit var targetText: TextView
    private lateinit var channelRailTitle: TextView
    private lateinit var dayList: LinearLayout
    private lateinit var channelList: LinearLayout
    private lateinit var programmeList: LinearLayout
    private lateinit var detailStack: LinearLayout

    private val dayRows = mutableMapOf<String, View>()
    private val channelRows = mutableMapOf<String, View>()
    private val programmeRows = mutableMapOf<String, View>()

    private var watchAction: View? = null
    private var browseMode = BrowseMode.Schedule
    private var selectedDayId = ""
    private var selectedChannelId = ""
    private var selectedSeriesGroupId = ""
    private var selectedProgrammeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catalog = CatchupCatalogLoader.load(this)
        selectedDayId = catalog.days.firstOrNull()?.id.orEmpty()
        selectedChannelId = firstChannelForDay(selectedDayId) ?: catalog.channels.firstOrNull()?.id.orEmpty()
        selectedProgrammeId = firstProgrammeForSelection()?.id
        buildUi()
        renderAll()
        updateTargetStatus()
        dayRows[selectedFirstRailId()]?.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        updateTargetStatus()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event)
        }
        return when (event.keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (handleBackNavigation()) {
                    true
                } else {
                    super.dispatchKeyEvent(event)
                }
            }
            KeyEvent.KEYCODE_MENU -> {
                openProviderRoute("u7d")
                true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                selectedProgramme()?.previousProgrammeId?.takeIf { it.isNotBlank() }?.let {
                    jumpToProgramme(it)
                    true
                } ?: super.dispatchKeyEvent(event)
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                selectedProgramme()?.nextProgrammeId?.takeIf { it.isNotBlank() }?.let {
                    jumpToProgramme(it)
                    true
                } ?: super.dispatchKeyEvent(event)
            }
            else -> super.dispatchKeyEvent(event)
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            clipToPadding = false
            setBackgroundColor(color("background"))
            setPadding(dp(36), dp(24), dp(36), dp(30))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        heading.addView(
            TextView(this).apply {
                text = getString(R.string.screen_title)
                textSize = 36f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(color("text_primary"))
                includeFontPadding = false
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(42)),
        )
        heading.addView(
            TextView(this).apply {
                text = getString(R.string.prototype_subtitle)
                textSize = 15f
                setTextColor(color("text_secondary"))
                includeFontPadding = false
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(24)),
        )
        header.addView(heading, LinearLayout.LayoutParams(0, dp(68), 1f))

        targetText = pillText(getString(R.string.target_unknown))
        header.addView(
            targetText,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40)),
        )
        root.addView(header)

        statusText = TextView(this).apply {
            text = getString(R.string.status_fixture_mode)
            textSize = 15f
            setTextColor(color("text_muted"))
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            includeFontPadding = false
        }
        root.addView(
            statusText,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(34)),
        )

        val columns = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false
            clipToPadding = false
            setBaselineAligned(false)
            setPadding(0, dp(14), 0, 0)
        }
        columns.addView(buildRail(title = getString(R.string.browse), assign = { dayList = it }), columnParams(dp(155)))
        columns.addView(
            buildRail(
                title = getString(R.string.channels),
                assign = { channelList = it },
                assignTitle = { channelRailTitle = it },
            ),
            columnParams(dp(185)),
        )
        columns.addView(buildRail(title = getString(R.string.programmes), assign = { programmeList = it }), columnParams(dp(300)))
        columns.addView(buildDetailPanel(), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).withLeftMargin(dp(16)))
        root.addView(columns, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)
    }

    private fun columnParams(width: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT).withLeftMargin(dp(16))

    private fun buildRail(
        title: String,
        assign: (LinearLayout) -> Unit,
        assignTitle: (TextView) -> Unit = {},
    ): LinearLayout {
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            clipToPadding = false
        }
        assign(list)
        val titleView = panelTitle(title)
        assignTitle(titleView)
        return panel().apply {
            addView(titleView)
            addView(
                ScrollView(this@MainActivity).apply {
                    isFillViewport = true
                    clipChildren = false
                    clipToPadding = false
                    overScrollMode = View.OVER_SCROLL_NEVER
                    addView(list)
                },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f),
            )
        }
    }

    private fun buildDetailPanel(): LinearLayout {
        detailStack = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            clipToPadding = false
        }
        return panel().apply {
            addView(panelTitle(getString(R.string.details)))
            addView(
                ScrollView(this@MainActivity).apply {
                    isFillViewport = true
                    clipChildren = false
                    clipToPadding = false
                    overScrollMode = View.OVER_SCROLL_NEVER
                    addView(detailStack)
                },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f),
            )
        }
    }

    private fun renderAll(focusTarget: FocusTarget? = null) {
        renderDays()
        renderChannels()
        renderProgrammes()
        renderDetails()
        wireFocusNavigation()
        when (focusTarget) {
            is FocusTarget.Day -> dayRows[focusTarget.id]?.requestFocus()
            is FocusTarget.Channel -> channelRows[focusTarget.id]?.requestFocus()
            is FocusTarget.Programme -> programmeRows[focusTarget.id]?.requestFocus()
            null -> Unit
        }
    }

    private fun renderDays() {
        dayRows.clear()
        dayList.removeAllViews()

        dayList.addView(sectionLabel(getString(R.string.discovery)), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(34)))
        addDiscoveryRow(
            id = BROWSE_MOVIES_ID,
            selected = browseMode == BrowseMode.Movies,
            title = getString(R.string.movies),
            subtitle = getString(R.string.movie_discovery_subtitle),
            meta = programmeCountText(catalog.movieProgrammes().size),
            action = ::selectMovies,
        )
        addDiscoveryRow(
            id = BROWSE_SERIES_ID,
            selected = browseMode == BrowseMode.Series,
            title = getString(R.string.series),
            subtitle = getString(R.string.series_discovery_subtitle),
            meta = episodeCountText(catalog.programmes.count { it.isSeries }),
            action = ::selectSeries,
        )

        dayList.addView(sectionLabel(getString(R.string.days)), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(38)).withTopMargin(dp(12)))
        catalog.days.forEach { day ->
            val selected = browseMode == BrowseMode.Schedule && day.id == selectedDayId
            val row = focusRow(
                selected = selected,
                contentDescription = "${day.label}, ${day.subtitle}",
            ) {
                selectDay(day.id)
            }.apply {
                addView(
                    stackedText(
                        title = day.label,
                        subtitle = day.subtitle,
                        meta = programmeCountText(programmeCountForDay(day.id)),
                    ),
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f),
                )
            }
            dayRows[day.id] = row
            dayList.addView(row, rowParams(dp(76)))
        }

        dayList.addView(sectionLabel(getString(R.string.official_shortcuts)), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(38)).withTopMargin(dp(12)))
        addShortcutRow(SHORTCUT_FLASHBACK_ID, getString(R.string.open_flashback)) { openProviderRoute("u7d") }
        addShortcutRow(SHORTCUT_GUIDE_ID, getString(R.string.open_guide)) { openProviderRoute("epg") }
        addShortcutRow(SHORTCUT_SEARCH_ID, getString(R.string.open_search)) { openProviderRoute("search") }
    }

    private fun addDiscoveryRow(
        id: String,
        selected: Boolean,
        title: String,
        subtitle: String,
        meta: String,
        action: () -> Unit,
    ) {
        val row = focusRow(
            selected = selected,
            contentDescription = "$title, $subtitle",
            action = action,
        ).apply {
            addView(
                stackedText(
                    title = title,
                    subtitle = subtitle,
                    meta = meta,
                ),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f),
            )
        }
        dayRows[id] = row
        dayList.addView(row, rowParams(dp(76)))
    }

    private fun addShortcutRow(id: String, text: String, action: () -> Unit) {
        val button = shortcutButton(text, action)
        dayRows[id] = button
        dayList.addView(button, buttonParams())
    }

    private fun renderChannels() {
        channelRows.clear()
        channelList.removeAllViews()
        channelRailTitle.text = when (browseMode) {
            BrowseMode.Schedule -> getString(R.string.channels)
            BrowseMode.Movies -> getString(R.string.movies)
            BrowseMode.Series -> getString(R.string.series)
        }

        when (browseMode) {
            BrowseMode.Schedule -> renderChannelRows()
            BrowseMode.Movies -> renderMovieGroupRows()
            BrowseMode.Series -> renderSeriesGroupRows()
        }
    }

    private fun renderChannelRows() {
        catalog.channels.forEach { channel ->
            val programmes = catalog.programmesFor(selectedDayId, channel.id)
            val selected = channel.id == selectedChannelId
            val row = focusRow(
                selected = selected,
                contentDescription = "${channel.number}, ${channel.name}",
            ) {
                selectChannel(channel.id)
            }.apply {
                val badge = TextView(this@MainActivity).apply {
                    text = channel.badge
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    setTextColor(color("text_primary"))
                    background = rounded("badge", channelColor(channel.id))
                }
                addView(badge, LinearLayout.LayoutParams(dp(48), dp(48)).withRightMargin(dp(12)))
                addView(
                    stackedText(
                        title = channel.name,
                        subtitle = getString(R.string.channel_number, channel.number),
                        meta = programmeCountText(programmes.size),
                    ),
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f),
                )
            }
            channelRows[channel.id] = row
            channelList.addView(row, rowParams(dp(76)))
        }
    }

    private fun renderMovieGroupRows() {
        val movies = catalog.movieProgrammes()
        val row = focusRow(
            selected = true,
            contentDescription = getString(R.string.all_films),
        ) {
            selectMovieGroup()
        }.apply {
            addView(
                stackedText(
                    title = getString(R.string.all_films),
                    subtitle = getString(R.string.all_films_subtitle),
                    meta = programmeCountText(movies.size),
                ),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f),
            )
        }
        channelRows[MOVIES_GROUP_ID] = row
        channelList.addView(row, rowParams(dp(76)))
    }

    private fun renderSeriesGroupRows() {
        val groups = catalog.seriesGroups()
        if (groups.isEmpty()) {
            selectedSeriesGroupId = ""
            channelList.addView(emptyText(getString(R.string.no_series)))
            return
        }
        if (selectedSeriesGroupId !in groups.map { it.id }) {
            selectedSeriesGroupId = groups.first().id
        }
        groups.forEach { group ->
            val selected = group.id == selectedSeriesGroupId
            val row = focusRow(
                selected = selected,
                accent = group.episodes.firstOrNull()?.accent,
                contentDescription = group.title,
            ) {
                selectSeriesGroup(group.id)
            }.apply {
                addView(accentStrip(group.episodes.firstOrNull()?.accent ?: color("accent")), LinearLayout.LayoutParams(dp(5), ViewGroup.LayoutParams.MATCH_PARENT).withRightMargin(dp(12)))
                addView(
                    stackedText(
                        title = group.title,
                        subtitle = getString(R.string.series_group_subtitle),
                        meta = episodeCountText(group.episodes.size),
                    ),
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f),
                )
            }
            channelRows[group.id] = row
            channelList.addView(row, rowParams(dp(76)))
        }
    }

    private fun renderProgrammes() {
        programmeRows.clear()
        programmeList.removeAllViews()

        val programmes = activeProgrammes()
        if (programmes.isEmpty()) {
            selectedProgrammeId = null
            programmeList.addView(emptyText(emptyProgrammeMessage()))
            return
        }

        if (selectedProgrammeId !in programmes.map { it.id }) {
            selectedProgrammeId = programmes.first().id
        }

        programmes.forEach { programme ->
            val selected = programme.id == selectedProgrammeId
            val row = focusRow(
                selected = selected,
                accent = programme.accent,
                contentDescription = "${programme.start}, ${programme.title}",
            ) {
                selectProgramme(programme.id)
            }.apply {
                addView(accentStrip(programme.accent), LinearLayout.LayoutParams(dp(5), ViewGroup.LayoutParams.MATCH_PARENT).withRightMargin(dp(12)))
                addView(
                    stackedText(
                        title = programme.title,
                        subtitle = programmeSubtitle(programme),
                        meta = programmeMeta(programme),
                    ),
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f),
                )
            }
            programmeRows[programme.id] = row
            programmeList.addView(row, rowParams(dp(94)))
        }
    }

    private fun renderDetails() {
        watchAction = null
        detailStack.removeAllViews()
        val programme = selectedProgramme()
        if (programme == null) {
            detailStack.addView(emptyText(getString(R.string.select_programme)))
            return
        }

        val channel = catalog.channel(programme.channelId)
        val day = catalog.days.firstOrNull { it.id == programme.dayId }
        val meta = listOfNotNull(
            channel?.name,
            day?.label,
            "${programme.start}-${programme.end}",
            programme.episodeLabel.takeIf { it.isNotBlank() },
        ).joinToString(" · ")

        detailStack.addView(
            TextView(this).apply {
                text = programme.title
                textSize = 30f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(color("text_primary"))
                includeFontPadding = false
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
        )
        detailStack.addView(bodyText(meta, color("accent")), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(34)))
        detailStack.addView(bodyText(programme.description, color("text_secondary")), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).withTopMargin(dp(8)))

        if (programme.progressPercent > 0) {
            detailStack.addView(
                bodyText(getString(R.string.progress_percent, programme.progressPercent), color("text_muted")),
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(32)).withTopMargin(dp(18)),
            )
            detailStack.addView(progressBar(programme.progressPercent, programme.accent), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)))
        }

        if (programme.isSeries) {
            detailStack.addView(sectionLabel(getString(R.string.more_episodes)), LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(34)).withTopMargin(dp(18)))
            val episodeRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                clipChildren = false
                clipToPadding = false
            }
            episodeRow.addView(
                actionButton(getString(R.string.previous_episode)) {
                    programme.previousProgrammeId.takeIf { it.isNotBlank() }?.let(::jumpToProgramme)
                }.apply {
                    isEnabled = programme.previousProgrammeId.isNotBlank()
                    alpha = if (isEnabled) 1f else 0.45f
                },
                LinearLayout.LayoutParams(0, dp(54), 1f),
            )
            episodeRow.addView(
                actionButton(getString(R.string.next_episode)) {
                    programme.nextProgrammeId.takeIf { it.isNotBlank() }?.let(::jumpToProgramme)
                }.apply {
                    isEnabled = programme.nextProgrammeId.isNotBlank()
                    alpha = if (isEnabled) 1f else 0.45f
                },
                LinearLayout.LayoutParams(0, dp(54), 1f).withLeftMargin(dp(10)),
            )
            detailStack.addView(episodeRow)
        }

        val watchButton = actionButton(getString(R.string.watch)) {
            openUri(Uri.parse(programme.providerRoute))
        }
        watchAction = watchButton
        detailStack.addView(
            watchButton,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)).withTopMargin(dp(24)),
        )
        detailStack.addView(
            actionButton(getString(R.string.open_flashback)) {
                openProviderRoute("u7d")
            },
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).withTopMargin(dp(10)),
        )
    }

    private fun wireFocusNavigation() {
        val selectedBrowse = dayRows[selectedFirstRailId()]
        val selectedGroup = selectedSecondRailId()?.let { channelRows[it] }
        val selectedProgramme = selectedProgrammeId?.let { programmeRows[it] }
        val rightOfBrowse = selectedGroup ?: selectedProgramme
        val leftOfProgramme = selectedGroup ?: selectedBrowse
        val watch = watchAction

        dayRows.values.forEach { row ->
            row.nextFocusRightId = rightOfBrowse?.id ?: View.NO_ID
            row.setHorizontalFocusTargets(left = null, right = rightOfBrowse)
        }
        channelRows.values.forEach { row ->
            row.nextFocusLeftId = selectedBrowse?.id ?: View.NO_ID
            row.nextFocusRightId = selectedProgramme?.id ?: View.NO_ID
            row.setHorizontalFocusTargets(left = selectedBrowse, right = selectedProgramme)
        }
        programmeRows.values.forEach { row ->
            row.nextFocusLeftId = leftOfProgramme?.id ?: View.NO_ID
            row.nextFocusRightId = watch?.id ?: View.NO_ID
            row.setHorizontalFocusTargets(left = leftOfProgramme, right = watch)
        }
        watch?.nextFocusLeftId = selectedProgramme?.id ?: View.NO_ID
        watch?.setHorizontalFocusTargets(left = selectedProgramme, right = null)
    }

    private fun handleBackNavigation(): Boolean {
        val current = currentFocus
        val selectedBrowse = dayRows[selectedFirstRailId()]
        val selectedGroup = selectedSecondRailId()?.let { channelRows[it] }
        val selectedProgramme = selectedProgrammeId?.let { programmeRows[it] }

        return when {
            current.isInDetailStack() ->
                selectedProgramme.requestFocusIfPresent() ||
                    selectedGroup.requestFocusIfPresent() ||
                    selectedBrowse.requestFocusIfPresent()
            current != null && programmeRows.values.contains(current) ->
                selectedGroup.requestFocusIfPresent() || selectedBrowse.requestFocusIfPresent()
            current != null && channelRows.values.contains(current) ->
                selectedBrowse.requestFocusIfPresent()
            current != null && dayRows.values.contains(current) && browseMode != BrowseMode.Schedule -> {
                browseMode = BrowseMode.Schedule
                selectedProgrammeId = firstProgrammeForSelection()?.id
                renderAll(FocusTarget.Day(selectedDayId))
                true
            }
            current == null && browseMode != BrowseMode.Schedule -> {
                browseMode = BrowseMode.Schedule
                selectedProgrammeId = firstProgrammeForSelection()?.id
                renderAll(FocusTarget.Day(selectedDayId))
                true
            }
            else -> false
        }
    }

    private fun View?.requestFocusIfPresent(): Boolean =
        this?.requestFocus() == true

    private fun View?.isInDetailStack(): Boolean {
        var node = this
        while (node != null) {
            if (node == detailStack) {
                return true
            }
            node = node.parent as? View
        }
        return false
    }

    private fun View.setHorizontalFocusTargets(left: View?, right: View?) {
        setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) {
                return@setOnKeyListener false
            }
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> left?.requestFocus() ?: false
                KeyEvent.KEYCODE_DPAD_RIGHT -> right?.requestFocus() ?: false
                else -> false
            }
        }
    }

    private fun selectDay(dayId: String) {
        browseMode = BrowseMode.Schedule
        selectedDayId = dayId
        if (catalog.programmesFor(selectedDayId, selectedChannelId).isEmpty()) {
            selectedChannelId = firstChannelForDay(selectedDayId) ?: selectedChannelId
        }
        selectedProgrammeId = firstProgrammeForSelection()?.id
        renderAll(FocusTarget.Day(dayId))
    }

    private fun selectMovies() {
        browseMode = BrowseMode.Movies
        selectedProgrammeId = firstProgrammeForSelection()?.id
        renderAll(FocusTarget.Day(BROWSE_MOVIES_ID))
    }

    private fun selectSeries() {
        browseMode = BrowseMode.Series
        val groups = catalog.seriesGroups()
        val preferredGroupId = selectedProgramme()?.takeIf { it.isSeries }?.seriesGroupId
        selectedSeriesGroupId = groups.firstOrNull { it.id == preferredGroupId }?.id
            ?: groups.firstOrNull()?.id.orEmpty()
        selectedProgrammeId = firstProgrammeForSelection()?.id
        renderAll(FocusTarget.Day(BROWSE_SERIES_ID))
    }

    private fun selectChannel(channelId: String) {
        selectedChannelId = channelId
        selectedProgrammeId = firstProgrammeForSelection()?.id
        renderAll(FocusTarget.Channel(channelId))
    }

    private fun selectMovieGroup() {
        selectedProgrammeId = firstProgrammeForSelection()?.id
        renderAll(FocusTarget.Channel(MOVIES_GROUP_ID))
    }

    private fun selectSeriesGroup(seriesGroupId: String) {
        selectedSeriesGroupId = seriesGroupId
        selectedProgrammeId = firstProgrammeForSelection()?.id
        renderAll(FocusTarget.Channel(seriesGroupId))
    }

    private fun selectProgramme(programmeId: String) {
        selectedProgrammeId = programmeId
        renderAll(FocusTarget.Programme(programmeId))
    }

    private fun jumpToProgramme(programmeId: String) {
        val programme = catalog.programme(programmeId) ?: return
        if (browseMode == BrowseMode.Series && programme.isSeries) {
            selectedSeriesGroupId = programme.seriesGroupId
        } else {
            browseMode = BrowseMode.Schedule
            selectedDayId = programme.dayId
            selectedChannelId = programme.channelId
        }
        selectedProgrammeId = programme.id
        renderAll(FocusTarget.Programme(programme.id))
    }

    private fun firstChannelForDay(dayId: String): String? =
        catalog.channels.firstOrNull { catalog.programmesFor(dayId, it.id).isNotEmpty() }?.id

    private fun firstProgrammeForSelection(): CatchupProgramme? =
        activeProgrammes().firstOrNull()

    private fun activeProgrammes(): List<CatchupProgramme> =
        when (browseMode) {
            BrowseMode.Schedule -> catalog.programmesFor(selectedDayId, selectedChannelId)
            BrowseMode.Movies -> catalog.movieProgrammes()
            BrowseMode.Series -> catalog.episodesForSeries(selectedSeriesGroupId)
        }

    private fun emptyProgrammeMessage(): String =
        when (browseMode) {
            BrowseMode.Schedule -> getString(R.string.no_programmes)
            BrowseMode.Movies -> getString(R.string.no_movies)
            BrowseMode.Series -> getString(R.string.no_series)
        }

    private fun selectedProgramme(): CatchupProgramme? =
        catalog.programme(selectedProgrammeId)

    private fun selectedFirstRailId(): String =
        when (browseMode) {
            BrowseMode.Schedule -> selectedDayId
            BrowseMode.Movies -> BROWSE_MOVIES_ID
            BrowseMode.Series -> BROWSE_SERIES_ID
        }

    private fun selectedSecondRailId(): String? =
        when (browseMode) {
            BrowseMode.Schedule -> selectedChannelId.takeIf { it.isNotBlank() }
            BrowseMode.Movies -> MOVIES_GROUP_ID
            BrowseMode.Series -> selectedSeriesGroupId.takeIf { it.isNotBlank() }
        }

    private fun programmeSubtitle(programme: CatchupProgramme): String {
        val kindLabel = programme.subtitle.ifBlank { displayKind(programme.kind) }
        val base = "${programme.start}-${programme.end} · $kindLabel"
        if (browseMode == BrowseMode.Schedule) {
            return base
        }
        val channel = catalog.channel(programme.channelId)?.name
        val day = catalog.days.firstOrNull { it.id == programme.dayId }?.label
        return listOfNotNull(
            channel,
            day,
            base,
        ).joinToString(" · ")
    }

    private fun programmeCountForDay(dayId: String): Int =
        catalog.programmes.count { it.dayId == dayId }

    private fun programmeCountText(count: Int): String =
        if (count == 1) {
            getString(R.string.programme_count_one)
        } else {
            getString(R.string.programme_count_many, count)
        }

    private fun episodeCountText(count: Int): String =
        if (count == 1) {
            getString(R.string.episode_count_one)
        } else {
            getString(R.string.episode_count_many, count)
        }

    private fun programmeMeta(programme: CatchupProgramme): String =
        listOfNotNull(
            programme.episodeLabel.takeIf { it.isNotBlank() },
            programme.progressPercent.takeIf { it > 0 }?.let {
                getString(R.string.progress_percent, it)
            },
        ).joinToString(" · ").ifBlank {
            displayKind(programme.kind)
        }

    private fun displayKind(kind: String): String =
        when (kind) {
            "movie" -> "Filme"
            "sport" -> "Desporto"
            "series" -> "Série"
            "news" -> "Informação"
            else -> kind.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

    private fun openProviderRoute(vararg segments: String) {
        val path = segments.joinToString("/") { Uri.encode(it.trim()) }
        openUri(Uri.parse("$TARGET_SCHEME://$path"))
    }

    private fun openUri(uri: Uri) {
        if (!isAllowedProviderUri(uri)) {
            setStatus(getString(R.string.launch_rejected))
            Log.w(LOG_TAG, "handoff_result status=rejected_uri scheme=${uri.scheme} host=${uri.host}")
            return
        }
        Log.i(LOG_TAG, "handoff_start uri=$uri package=$TARGET_PACKAGE")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            setPackage(TARGET_PACKAGE)
        }
        try {
            startActivity(intent)
            setStatus(getString(R.string.launched_route, uri.toString()))
            Log.i(LOG_TAG, "handoff_result status=requested uri=$uri")
        } catch (_: ActivityNotFoundException) {
            setStatus(getString(R.string.launch_failed))
            Log.w(LOG_TAG, "handoff_result status=activity_not_found uri=$uri")
        } catch (_: SecurityException) {
            setStatus(getString(R.string.launch_failed))
            Log.w(LOG_TAG, "handoff_result status=security_exception uri=$uri")
        }
    }

    private fun isAllowedProviderUri(uri: Uri): Boolean =
        uri.isHierarchical &&
            uri.scheme == TARGET_SCHEME &&
            uri.host in ALLOWED_ROUTE_ROOTS &&
            uri.userInfo == null

    private fun updateTargetStatus() {
        val available = runCatching {
            packageManager.getPackageInfo(TARGET_PACKAGE, 0)
        }.isSuccess
        targetText.text = if (available) {
            getString(R.string.target_available)
        } else {
            getString(R.string.target_missing)
        }
        targetText.background = rounded(if (available) "target_ok" else "target_missing")
    }

    private fun setStatus(text: String) {
        statusText.text = text
    }

    private fun panel(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            clipToPadding = false
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded("panel")
        }

    private fun panelTitle(text: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(color("text_primary"))
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            setPadding(0, 0, 0, dp(12))
        }

    private fun sectionLabel(text: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(color("text_muted"))
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            includeFontPadding = false
        }

    private fun stackedText(title: String, subtitle: String, meta: String): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                maxLines = 1
                setTextColor(color("text_primary"))
                includeFontPadding = false
            })
            addView(TextView(this@MainActivity).apply {
                text = subtitle
                textSize = 13f
                maxLines = 1
                setTextColor(color("text_secondary"))
                includeFontPadding = false
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(21)))
            addView(TextView(this@MainActivity).apply {
                text = meta
                textSize = 12f
                maxLines = 1
                setTextColor(color("text_muted"))
                includeFontPadding = false
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(18)))
        }

    private fun bodyText(text: String, textColor: Int): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 16f
            setLineSpacing(2f, 1f)
            setTextColor(textColor)
            includeFontPadding = false
        }

    private fun emptyText(text: String): TextView =
        bodyText(text, color("text_muted")).apply {
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(36), dp(20), dp(36))
        }

    private fun focusRow(
        selected: Boolean,
        accent: Int? = null,
        contentDescription: String,
        action: () -> Unit,
    ): LinearLayout =
        LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isFocusable = true
            isClickable = true
            this.contentDescription = contentDescription
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = rowBackground(selected = selected, focused = false, accent = accent)
            setOnClickListener { action() }
            applyFocusTreatment(false)
            setOnFocusChangeListener { view, hasFocus ->
                view.background = rowBackground(selected = selected, focused = hasFocus, accent = accent)
                view.applyFocusTreatment(hasFocus)
            }
        }

    private fun actionButton(text: String, action: () -> Unit): Button =
        Button(this).apply {
            id = View.generateViewId()
            this.text = text
            isAllCaps = false
            textSize = 15f
            maxLines = 1
            includeFontPadding = false
            setTextColor(color("text_primary"))
            background = rounded("button")
            setOnClickListener { action() }
            applyFocusTreatment(false)
            setOnFocusChangeListener { view, hasFocus ->
                view.background = rounded(if (hasFocus) "button_focused" else "button")
                view.applyFocusTreatment(hasFocus)
            }
        }

    private fun shortcutButton(text: String, action: () -> Unit): Button =
        actionButton(text, action).apply {
            textSize = 14f
        }

    private fun pillText(text: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(color("text_primary"))
            setPadding(dp(16), 0, dp(16), 0)
            background = rounded("target_missing")
        }

    private fun accentStrip(accent: Int): View =
        View(this).apply {
            background = rounded("strip", accent)
        }

    private fun progressBar(percent: Int, accent: Int): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = rounded("progress_track")
            val clamped = percent.coerceIn(0, 100)
            addView(View(this@MainActivity).apply {
                background = rounded("progress_fill", accent)
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, clamped.toFloat()))
            addView(View(this@MainActivity), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (100 - clamped).toFloat()))
        }

    private fun rowBackground(selected: Boolean, focused: Boolean, accent: Int?): GradientDrawable =
        rounded(
            when {
                focused && selected -> "row_selected_focused"
                focused -> "row_focused"
                selected -> "row_selected"
                else -> "row"
            },
            accent,
        )

    private fun View.applyFocusTreatment(hasFocus: Boolean) {
        val scale = if (hasFocus) 1.025f else 1f
        val z = if (hasFocus) dp(8).toFloat() else 0f
        animate().cancel()
        animate()
            .scaleX(scale)
            .scaleY(scale)
            .translationZ(z)
            .setDuration(90L)
            .start()
    }

    private fun rounded(name: String, accent: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            when (name) {
                "panel" -> {
                    setColor(color("panel"))
                    setStroke(dp(1), color("line"))
                }
                "row" -> {
                    setColor(color("row"))
                    setStroke(dp(1), color("line_soft"))
                }
                "row_selected" -> {
                    setColor(color("row_selected"))
                    setStroke(dp(2), accent ?: color("accent"))
                }
                "row_focused" -> {
                    setColor(color("row_focused"))
                    setStroke(dp(4), color("focus"))
                }
                "row_selected_focused" -> {
                    setColor(color("row_selected_focused"))
                    setStroke(dp(4), color("focus"))
                }
                "button" -> {
                    setColor(color("button"))
                    setStroke(dp(1), color("line"))
                }
                "button_focused" -> {
                    setColor(color("button_focused"))
                    setStroke(dp(4), color("focus"))
                }
                "badge" -> {
                    shape = GradientDrawable.OVAL
                    setColor(accent ?: color("accent"))
                }
                "strip", "progress_fill" -> {
                    setColor(accent ?: color("accent"))
                }
                "progress_track" -> {
                    setColor(color("progress_track"))
                }
                "target_ok" -> {
                    setColor(Color.rgb(34, 78, 58))
                    setStroke(dp(1), Color.rgb(93, 192, 129))
                }
                "target_missing" -> {
                    setColor(Color.rgb(92, 48, 43))
                    setStroke(dp(1), Color.rgb(229, 125, 88))
                }
                else -> setColor(color("panel"))
            }
        }

    private fun channelColor(channelId: String): Int =
        when (channelId) {
            "rtp1" -> Color.rgb(53, 132, 228)
            "rtp2" -> Color.rgb(78, 172, 184)
            "sic" -> Color.rgb(229, 145, 63)
            "tvi" -> Color.rgb(156, 106, 204)
            "sicn" -> Color.rgb(56, 159, 98)
            else -> color("accent")
        }

    private fun color(name: String): Int =
        when (name) {
            "background" -> Color.rgb(13, 17, 16)
            "panel" -> Color.rgb(27, 32, 31)
            "row" -> Color.rgb(34, 40, 39)
            "row_selected" -> Color.rgb(55, 53, 36)
            "row_selected_focused" -> Color.rgb(74, 70, 42)
            "row_focused" -> Color.rgb(50, 75, 75)
            "button" -> Color.rgb(42, 48, 47)
            "button_focused" -> Color.rgb(61, 80, 78)
            "line" -> Color.rgb(71, 82, 77)
            "line_soft" -> Color.rgb(50, 60, 56)
            "progress_track" -> Color.rgb(65, 71, 67)
            "text_primary" -> Color.rgb(248, 245, 234)
            "text_secondary" -> Color.rgb(201, 207, 199)
            "text_muted" -> Color.rgb(150, 160, 153)
            "accent" -> Color.rgb(232, 186, 82)
            "focus" -> Color.rgb(140, 238, 232)
            else -> Color.rgb(232, 186, 82)
        }

    private fun rowParams(height: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height).withBottomMargin(dp(10))

    private fun buttonParams(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)).withBottomMargin(dp(10))

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    private enum class BrowseMode {
        Schedule,
        Movies,
        Series,
    }

    private sealed class FocusTarget {
        data class Day(val id: String) : FocusTarget()
        data class Channel(val id: String) : FocusTarget()
        data class Programme(val id: String) : FocusTarget()
    }

    companion object {
        private const val TARGET_PACKAGE = "ro.digionline.tv"
        private const val TARGET_SCHEME = "digitv"
        private const val LOG_TAG = "RebobinaHandoff"
        private const val BROWSE_MOVIES_ID = "browse:movies"
        private const val BROWSE_SERIES_ID = "browse:series"
        private const val MOVIES_GROUP_ID = "group:movies:all"
        private const val SHORTCUT_FLASHBACK_ID = "shortcut:flashback"
        private const val SHORTCUT_GUIDE_ID = "shortcut:guide"
        private const val SHORTCUT_SEARCH_ID = "shortcut:search"
        private val ALLOWED_ROUTE_ROOTS = setOf(
            "catchup",
            "catchupstream",
            "content",
            "epg",
            "livestream",
            "search",
            "u7d",
        )
    }
}

private fun LinearLayout.LayoutParams.withLeftMargin(margin: Int): LinearLayout.LayoutParams =
    apply { leftMargin = margin }

private fun LinearLayout.LayoutParams.withRightMargin(margin: Int): LinearLayout.LayoutParams =
    apply { rightMargin = margin }

private fun LinearLayout.LayoutParams.withTopMargin(margin: Int): LinearLayout.LayoutParams =
    apply { topMargin = margin }

private fun LinearLayout.LayoutParams.withBottomMargin(margin: Int): LinearLayout.LayoutParams =
    apply { bottomMargin = margin }
