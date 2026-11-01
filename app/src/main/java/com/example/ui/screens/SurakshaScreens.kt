package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CaseDossier
import com.example.data.model.CaseStage
import com.example.data.model.CityJurisdiction
import com.example.data.model.DistrictJurisdiction
import com.example.data.model.EmergencyHelpline
import com.example.data.model.LegalRightGuide
import com.example.data.model.StateJurisdiction
import com.example.ui.components.CascadingLocationDropdownField
import com.example.ui.components.CaseDossierCard
import com.example.ui.components.CompactLocationSelectorPill
import com.example.ui.components.CompactRepresentativeCard
import com.example.ui.components.EditorialNewsCard
import com.example.ui.components.LocationFilterBottomSheet
import com.example.ui.viewmodel.SurakshaFilterState

private enum class HomeNewsCategory(val label: String) {
    LATEST("Latest"),
    FOLLOWING("Following"),
    CASE_UPDATES("Case Updates"),
    STATES("States"),
    SAFETY("Safety"),
    LEGAL_UPDATES("Legal Updates")
}

/**
 * Material 3 Searchable TopAppBar for the Home Screen and Application Shell.
 * Combines a Material 3 TopAppBar with a responsive keyword search input to filter the news feed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchableTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    titleText: String = "Suraksha Chronicle",
    subtitleText: String = "Beyond the headline. Follow the facts.",
    showSearchInput: Boolean = true,
    onSearchButtonClick: () -> Unit = {},
    extraActions: @Composable RowScope.() -> Unit = {}
) {
    val searchFocusRequester = remember { FocusRequester() }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onSearchButtonClick()
                            runCatching { searchFocusRequester.requestFocus() }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("top_bar_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search news by keyword",
                            tint = if (searchQuery.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    extraActions()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_top_app_bar")
            )

            AnimatedVisibility(visible = showSearchInput) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search headlines, locations, courts, or sources...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search articles"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.testTag("clear_search_query_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .focusRequester(searchFocusRequester)
                        .testTag("case_search_input")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * Redesigned Home Screen:
 * - Searchable Material 3 TopAppBar for filtering news by keyword
 * - Compact location filter pill ("All India ▾") opening a cascading bottom sheet
 * - Horizontally scrollable editorial category chips (Latest, Following, Case Updates, States, Safety, Legal Updates)
 * - Immediate news headlines with Featured Story + compact EditorialNewsCards
 * - No giant inline dropdowns or floating action buttons covering article content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrontPageCaseFeedScreen(
    dossiers: List<CaseDossier>,
    savedCount: Int,
    states: List<StateJurisdiction>,
    filterState: SurakshaFilterState,
    districts: List<DistrictJurisdiction>,
    cities: List<CityJurisdiction>,
    activeRepresentative: CityJurisdiction?,
    globalBlur: Boolean,
    revealedIds: Set<String>,
    isRefreshingLive: Boolean,
    onSelectState: (String) -> Unit,
    onSelectDistrict: (String) -> Unit,
    onSelectCity: (String) -> Unit,
    onSelectVictimGroup: (String) -> Unit,
    onSelectCrimeCategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefreshLive: () -> Unit,
    onRevealBlur: (String) -> Unit,
    onOpenDossier: (CaseDossier) -> Unit,
    onToggleSaveForLife: (CaseDossier) -> Unit,
    onAddFollowUp: (CaseDossier, String, CaseStage, String, String) -> Unit,
    onArchiveNewIncident: (
        title: String,
        summary: String,
        fullReport: String,
        stateName: String,
        districtName: String,
        cityName: String,
        mlaName: String,
        mpName: String,
        victimCategory: String,
        crimeCategory: String,
        caseStage: CaseStage,
        personalNotes: String
    ) -> Unit,
    onToggleFollow: (CaseDossier) -> Unit = {},
    onApplyLocation: (String, String, String) -> Unit = { st, dist, ct ->
        onSelectState(st)
        onSelectDistrict(dist)
        onSelectCity(ct)
    },
    onResetFilters: () -> Unit = { onSelectState("All India") },
    visibleFeedLimit: Int = 10,
    onLoadMore: () -> Unit = {},
    feedSyncNote: String? = null,
    showSearchBar: Boolean = true,
    showTopAppBar: Boolean = true
) {
    var selectedCategory by remember { mutableStateOf(HomeNewsCategory.LATEST) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var localSearchQuery by remember(filterState.searchQuery) { mutableStateOf(filterState.searchQuery) }

    val activeKeyword = localSearchQuery.trim()

    val keywordFilteredDossiers = remember(dossiers, activeKeyword) {
        if (activeKeyword.isBlank()) {
            dossiers
        } else {
            dossiers.filter { dossier ->
                dossier.title.contains(activeKeyword, ignoreCase = true) ||
                    dossier.summary.contains(activeKeyword, ignoreCase = true) ||
                    dossier.fullReport.contains(activeKeyword, ignoreCase = true) ||
                    dossier.stateName.contains(activeKeyword, ignoreCase = true) ||
                    dossier.districtName.contains(activeKeyword, ignoreCase = true) ||
                    dossier.cityName.contains(activeKeyword, ignoreCase = true) ||
                    dossier.crimeCategory.contains(activeKeyword, ignoreCase = true) ||
                    dossier.victimCategory.contains(activeKeyword, ignoreCase = true) ||
                    dossier.mlaName.contains(activeKeyword, ignoreCase = true) ||
                    dossier.mpName.contains(activeKeyword, ignoreCase = true) ||
                    dossier.sourceName.contains(activeKeyword, ignoreCase = true)
            }
        }
    }

    val displayedArticles = remember(keywordFilteredDossiers, selectedCategory) {
        when (selectedCategory) {
            HomeNewsCategory.LATEST -> keywordFilteredDossiers
            HomeNewsCategory.FOLLOWING -> keywordFilteredDossiers.filter { it.isFollowed || it.isSavedForLife }
            HomeNewsCategory.CASE_UPDATES -> keywordFilteredDossiers.filter { it.timelineUpdates.size > 1 }
            HomeNewsCategory.STATES -> keywordFilteredDossiers.sortedBy { it.stateName }
            HomeNewsCategory.SAFETY -> keywordFilteredDossiers.filter {
                it.crimeCategory.contains("School", ignoreCase = true) ||
                    it.crimeCategory.contains("Campus", ignoreCase = true) ||
                    it.crimeCategory.contains("Safety", ignoreCase = true) ||
                    it.crimeCategory.contains("Cyber", ignoreCase = true)
            }.ifEmpty { keywordFilteredDossiers }
            HomeNewsCategory.LEGAL_UPDATES -> keywordFilteredDossiers.filter {
                it.caseStage == CaseStage.CHARGESHEET_FILED ||
                    it.caseStage == CaseStage.TRIAL_ONGOING ||
                    it.caseStage == CaseStage.VERDICT_DELIVERED ||
                    it.crimeCategory.contains("POCSO", ignoreCase = true)
            }.ifEmpty { keywordFilteredDossiers }
        }
    }

    val paginatedArticles = remember(displayedArticles, visibleFeedLimit) {
        displayedArticles.take(visibleFeedLimit)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (showTopAppBar) {
            HomeSearchableTopAppBar(
                searchQuery = localSearchQuery,
                onSearchQueryChange = { newQuery ->
                    localSearchQuery = newQuery
                    onSearchQueryChange(newQuery)
                },
                showSearchInput = showSearchBar
            )
        }

        PullToRefreshBox(
            isRefreshing = isRefreshingLive,
            onRefresh = onRefreshLive,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("front_page_feed_list"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Compact Location Filter Bar + Horizontally Scrollable Category Chips
                item {
                    Column(
                        modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactLocationSelectorPill(
                        selectedState = filterState.selectedState,
                        selectedDistrict = filterState.selectedDistrict,
                        selectedCity = filterState.selectedCity,
                        onClick = { showLocationSheet = true },
                        onReset = { onResetFilters() }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isRefreshingLive) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        TextButton(
                            onClick = onRefreshLive,
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .testTag("refresh_live_rss_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh news feed",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refresh", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontally scrollable News Categories row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeNewsCategory.entries.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("home_category_chip_${category.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Compact Constituency Representative card ONLY when a specific city/district is selected
        if (activeRepresentative != null && filterState.selectedState != "All India") {
            item {
                CompactRepresentativeCard(
                    cityRep = activeRepresentative,
                    stateName = filterState.selectedState,
                    districtName = filterState.selectedDistrict,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Subtle Sync / Offline status note if present
        if (feedSyncNote != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = feedSyncNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRefreshLive) {
                            Text("Retry", maxLines = 1)
                        }
                    }
                }
            }
        }

            // Loading Skeletons when refreshing live feed
            if (isRefreshingLive) {
                item {
                    EditorialLoadingSkeletonCard()
                }
            }

            // Empty State
            if (paginatedArticles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No verified reports match this filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Reset the location or category filters to view all verified national reports.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                selectedCategory = HomeNewsCategory.LATEST
                                onResetFilters()
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Reset All Filters", maxLines = 1)
                        }
                    }
                }
            }
        } else {
            // Main News Feed: Featured first story + compact EditorialNewsCards
            itemsIndexed(paginatedArticles, key = { _, item -> item.id }) { index, dossier ->
                val isBlurred = globalBlur && !revealedIds.contains(dossier.id)
                EditorialNewsCard(
                    dossier = dossier,
                    isBlurred = isBlurred,
                    onRevealBlur = { onRevealBlur(dossier.id) },
                    onOpenDetail = { onOpenDossier(dossier) },
                    onToggleSave = { onToggleSaveForLife(dossier) },
                    onToggleFollow = { onToggleFollow(dossier) },
                    isFeatured = index == 0 && selectedCategory == HomeNewsCategory.LATEST
                )
            }

            // Pagination ("Load More")
            if (displayedArticles.size > paginatedArticles.size) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedButton(
                            onClick = onLoadMore,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("load_more_articles_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Load More Reports (${displayedArticles.size - paginatedArticles.size} remaining)",
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
            }
        }
    }

    if (showLocationSheet) {
        LocationFilterBottomSheet(
            states = states,
            initialState = filterState.selectedState,
            initialDistrict = filterState.selectedDistrict,
            initialCity = filterState.selectedCity,
            onDismiss = { showLocationSheet = false },
            onApply = { st, dist, ct ->
                onApplyLocation(st, dist, ct)
                showLocationSheet = false
            },
            onReset = {
                onResetFilters()
                showLocationSheet = false
            }
        )
    }
}

private enum class TrackerFilterTab(val label: String) {
    ALL("All Cases"),
    FOLLOWING("Following"),
    RECENTLY_UPDATED("Recently Updated"),
    AWAITING_UPDATES("Awaiting Updates"),
    CLOSED_RESOLVED("Judgment / Resolved")
}

/**
 * Redesigned "Never Forgotten" Case Tracker Screen (Track Tab):
 * Dedicated screen for following cases across months or years with verified chronological timelines.
 */
@Composable
fun FollowUpTrackerScreen(
    allDossiers: List<CaseDossier>,
    states: List<StateJurisdiction>,
    filterState: SurakshaFilterState,
    districts: List<DistrictJurisdiction>,
    cities: List<CityJurisdiction>,
    activeRepresentative: CityJurisdiction?,
    globalBlur: Boolean,
    revealedIds: Set<String>,
    onSelectState: (String) -> Unit,
    onSelectDistrict: (String) -> Unit,
    onSelectCity: (String) -> Unit,
    onRevealBlur: (String) -> Unit,
    onOpenDossier: (CaseDossier) -> Unit,
    onToggleSaveForLife: (CaseDossier) -> Unit,
    onAddFollowUp: (CaseDossier, String, CaseStage, String, String) -> Unit,
    onQuickSelectJurisdiction: (String, String, String) -> Unit,
    onToggleFollow: (CaseDossier) -> Unit = {},
    onApplyLocation: (String, String, String) -> Unit = { st, dist, ct ->
        onSelectState(st)
        onSelectDistrict(dist)
        onSelectCity(ct)
    },
    onResetFilters: () -> Unit = { onSelectState("All India") }
) {
    var selectedTrackerTab by remember { mutableStateOf(TrackerFilterTab.ALL) }
    var selectedCategoryFilter by remember { mutableStateOf("All Categories") }
    var activeTimelineUpdateDossier by remember { mutableStateOf<CaseDossier?>(null) }
    var showLocationSheet by remember { mutableStateOf(false) }

    val availableCategories = remember(allDossiers) {
        listOf("All Categories") + allDossiers.map { it.crimeCategory }.distinct()
    }

    val filteredCases = remember(allDossiers, selectedTrackerTab, selectedCategoryFilter) {
        val statusFiltered = when (selectedTrackerTab) {
            TrackerFilterTab.ALL -> allDossiers
            TrackerFilterTab.FOLLOWING -> allDossiers.filter { it.isFollowed || it.isSavedForLife }
            TrackerFilterTab.RECENTLY_UPDATED -> allDossiers.filter { it.timelineUpdates.size >= 2 }
            TrackerFilterTab.AWAITING_UPDATES -> allDossiers.filter {
                it.caseStage == CaseStage.FIR_FILED ||
                    it.caseStage == CaseStage.JUSTICE_PENDING ||
                    it.daysSinceIncident >= 30
            }
            TrackerFilterTab.CLOSED_RESOLVED -> allDossiers.filter {
                it.caseStage == CaseStage.VERDICT_DELIVERED
            }
        }
        if (selectedCategoryFilter == "All Categories") {
            statusFiltered
        } else {
            statusFiltered.filter { it.crimeCategory.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("followup_tracker_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header + Compact Location Selector + Filter Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Never Forgotten • Case Tracker",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Follow investigations, chargesheets, and court proceedings beyond the initial news cycle.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactLocationSelectorPill(
                        selectedState = filterState.selectedState,
                        selectedDistrict = filterState.selectedDistrict,
                        selectedCity = filterState.selectedCity,
                        onClick = { showLocationSheet = true },
                        onReset = onResetFilters
                    )

                    Text(
                        text = "${filteredCases.size} Tracked Cases",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrackerFilterTab.entries.forEach { tab ->
                        FilterChip(
                            selected = selectedTrackerTab == tab,
                            onClick = { selectedTrackerTab = tab },
                            label = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("tracker_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }
        }

        if (filteredCases.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No cases in this view",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Switch back to 'All Cases' or follow cases from the Home feed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { selectedTrackerTab = TrackerFilterTab.ALL },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Show All Tracked Cases", maxLines = 1)
                        }
                    }
                }
            }
        } else {
            items(filteredCases, key = { it.id }) { dossier ->
                val isBlurred = globalBlur && !revealedIds.contains(dossier.id)
                CaseDossierCard(
                    dossier = dossier,
                    isBlurred = isBlurred,
                    onRevealBlur = { onRevealBlur(dossier.id) },
                    onOpenDetail = { onOpenDossier(dossier) },
                    onToggleSaveForLife = { onToggleSaveForLife(dossier) },
                    onQuickSelectJurisdiction = onQuickSelectJurisdiction,
                    initiallyExpandedTimeline = true,
                    onAddTimelineUpdateClick = { activeTimelineUpdateDossier = dossier },
                    onToggleFollow = { onToggleFollow(dossier) }
                )
            }
        }
    }

    if (showLocationSheet) {
        LocationFilterBottomSheet(
            states = states,
            initialState = filterState.selectedState,
            initialDistrict = filterState.selectedDistrict,
            initialCity = filterState.selectedCity,
            onDismiss = { showLocationSheet = false },
            onApply = { st, dist, ct ->
                onApplyLocation(st, dist, ct)
                showLocationSheet = false
            },
            onReset = {
                onResetFilters()
                showLocationSheet = false
            }
        )
    }

    activeTimelineUpdateDossier?.let { targetDossier ->
        AddFollowUpDialog(
            dossier = targetDossier,
            onDismiss = { activeTimelineUpdateDossier = null },
            onSubmit = { milestoneLabel, stage, headline, details ->
                onAddFollowUp(targetDossier, milestoneLabel, stage, headline, details)
                activeTimelineUpdateDossier = null
            }
        )
    }
}

private enum class SavedSectionTab(val label: String) {
    SAVED_ARTICLES("Saved Articles"),
    FOLLOWED_CASES("Followed Cases"),
    ARCHIVED_DOSSIERS("Submitted Dossiers")
}

/**
 * Redesigned "Saved" Screen:
 * Calm, organized archive with Saved Articles, Followed Cases, and Submitted Dossiers,
 * plus search, sort, data export, and honest offline/sync status explanation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PermanentVaultScreen(
    savedDossiers: List<CaseDossier>,
    followedDossiers: List<CaseDossier> = emptyList(),
    states: List<StateJurisdiction>,
    globalBlur: Boolean,
    revealedIds: Set<String>,
    onRevealBlur: (String) -> Unit,
    onOpenDossier: (CaseDossier) -> Unit,
    onToggleSaveForLife: (CaseDossier) -> Unit,
    onAddFollowUp: (CaseDossier, String, CaseStage, String, String) -> Unit,
    onArchiveNewIncident: (
        title: String,
        summary: String,
        fullReport: String,
        stateName: String,
        districtName: String,
        cityName: String,
        mlaName: String,
        mpName: String,
        victimCategory: String,
        crimeCategory: String,
        caseStage: CaseStage,
        personalNotes: String
    ) -> Unit,
    onQuickSelectJurisdiction: (String, String, String) -> Unit,
    onToggleFollow: (CaseDossier) -> Unit = {}
) {
    val context = LocalContext.current
    var showCreateModal by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf(SavedSectionTab.SAVED_ARTICLES) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStateFilter by remember { mutableStateOf("All States") }
    var sortByLatestUpdated by remember { mutableStateOf(false) }
    var activeTimelineUpdateDossier by remember { mutableStateOf<CaseDossier?>(null) }

    val baseItems = remember(savedDossiers, followedDossiers, selectedSection) {
        when (selectedSection) {
            SavedSectionTab.SAVED_ARTICLES -> savedDossiers
            SavedSectionTab.FOLLOWED_CASES -> followedDossiers.ifEmpty { savedDossiers }
            SavedSectionTab.ARCHIVED_DOSSIERS -> savedDossiers.filter {
                it.id.startsWith("case_user_") || it.personalNotes.isNotBlank()
            }.ifEmpty { savedDossiers }
        }
    }

    val availableSavedStates = remember(baseItems) {
        listOf("All States") + baseItems.map { it.stateName }.distinct()
    }

    val displayedItems = remember(baseItems, searchQuery, selectedStateFilter, sortByLatestUpdated) {
        val stateFiltered = if (selectedStateFilter == "All States") {
            baseItems
        } else {
            baseItems.filter { it.stateName.equals(selectedStateFilter, ignoreCase = true) }
        }
        val filtered = if (searchQuery.isBlank()) {
            stateFiltered
        } else {
            stateFiltered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.stateName.contains(searchQuery, ignoreCase = true) ||
                    it.cityName.contains(searchQuery, ignoreCase = true) ||
                    it.crimeCategory.contains(searchQuery, ignoreCase = true) ||
                    it.personalNotes.contains(searchQuery, ignoreCase = true)
            }
        }
        if (sortByLatestUpdated) {
            filtered.sortedByDescending { it.timelineUpdates.size }
        } else {
            filtered
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("permanent_vault_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Saved & Followed Archive",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cached on device for offline reading and synced to your account when online.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val exportText = buildString {
                                    appendLine("SURAKSHA CHRONICLE • SAVED ARCHIVE EXPORT")
                                    appendLine("Total Saved Items: ${savedDossiers.size}")
                                    appendLine()
                                    savedDossiers.forEachIndexed { index, item ->
                                        appendLine("${index + 1}. ${item.title}")
                                        appendLine("   Location: ${item.cityName}, ${item.stateName}")
                                        appendLine("   Status: ${item.caseStage.label}")
                                        if (item.personalNotes.isNotBlank()) {
                                            appendLine("   Private Note: ${item.personalNotes}")
                                        }
                                        appendLine()
                                    }
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Suraksha Chronicle Saved Archive Export")
                                    putExtra(Intent.EXTRA_TEXT, exportText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Saved Archive"))
                            },
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("export_saved_archive_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export saved archive",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export", maxLines = 1)
                        }

                        Button(
                            onClick = { showCreateModal = true },
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("archive_new_incident_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Case", maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Section Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SavedSectionTab.entries.forEach { tab ->
                        FilterChip(
                            selected = selectedSection == tab,
                            onClick = { selectedSection = tab },
                            label = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("saved_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search & Sort Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search saved items or private notes...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("saved_vault_search_input")
                    )

                    FilterChip(
                        selected = sortByLatestUpdated,
                        onClick = { sortByLatestUpdated = !sortByLatestUpdated },
                        label = {
                            Text(
                                text = if (sortByLatestUpdated) "Most Updates" else "Latest Saved",
                                maxLines = 1
                            )
                        }
                    )
                }

                if (availableSavedStates.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSavedStates.forEach { st ->
                            FilterChip(
                                selected = selectedStateFilter == st,
                                onClick = { selectedStateFilter = st },
                                label = {
                                    Text(
                                        text = st,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (displayedItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdded,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No saved items in this section",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bookmark articles from the Home feed to read them offline or follow their case timelines.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedItems, key = { it.id }) { dossier ->
                val isBlurred = globalBlur && !revealedIds.contains(dossier.id)
                CaseDossierCard(
                    dossier = dossier,
                    isBlurred = isBlurred,
                    onRevealBlur = { onRevealBlur(dossier.id) },
                    onOpenDetail = { onOpenDossier(dossier) },
                    onToggleSaveForLife = { onToggleSaveForLife(dossier) },
                    onQuickSelectJurisdiction = onQuickSelectJurisdiction,
                    initiallyExpandedTimeline = false,
                    onAddTimelineUpdateClick = { activeTimelineUpdateDossier = dossier },
                    onToggleFollow = { onToggleFollow(dossier) }
                )
            }
        }
    }

    if (showCreateModal) {
        ArchiveNewIncidentModal(
            states = states,
            onDismiss = { showCreateModal = false },
            onSubmit = { title, summary, fullReport, st, dist, ct, mla, mp, victim, cat, stage, notes ->
                onArchiveNewIncident(title, summary, fullReport, st, dist, ct, mla, mp, victim, cat, stage, notes)
                showCreateModal = false
            }
        )
    }

    activeTimelineUpdateDossier?.let { targetDossier ->
        AddFollowUpDialog(
            dossier = targetDossier,
            onDismiss = { activeTimelineUpdateDossier = null },
            onSubmit = { milestoneLabel, stage, headline, details ->
                onAddFollowUp(targetDossier, milestoneLabel, stage, headline, details)
                activeTimelineUpdateDossier = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArchiveNewIncidentModal(
    states: List<StateJurisdiction>,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        summary: String,
        fullReport: String,
        stateName: String,
        districtName: String,
        cityName: String,
        mlaName: String,
        mpName: String,
        victimCategory: String,
        crimeCategory: String,
        caseStage: CaseStage,
        personalNotes: String
    ) -> Unit
) {
    var selectedStateObj by remember { mutableStateOf(states.first()) }
    var selectedDistrictObj by remember(selectedStateObj) { mutableStateOf(selectedStateObj.districts.first()) }
    var selectedCityObj by remember(selectedDistrictObj) { mutableStateOf(selectedDistrictObj.cities.first()) }

    var mlaInput by remember(selectedCityObj) {
        mutableStateOf(selectedCityObj.mlaName)
    }
    var mpInput by remember(selectedCityObj) {
        mutableStateOf(selectedCityObj.mpName)
    }

    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var fullReport by remember { mutableStateOf("") }
    var victimCategory by remember { mutableStateOf("Teenage Student (13-19)") }
    var crimeCategory by remember { mutableStateOf("School/Campus Safety") }
    var caseStage by remember { mutableStateOf(CaseStage.FIR_FILED) }
    var personalNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Archive Case Dossier",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Privacy & Moderation Policy: Do not enter names, school roll numbers, or identifying details of minors or survivors. User-archived dossiers are saved to your personal vault and labeled 'Awaiting Verification'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CascadingLocationDropdownField(
                    label = "State / UT",
                    selectedValue = selectedStateObj.stateName,
                    options = states.map { it.stateName },
                    enabled = true,
                    placeholderText = "Select State",
                    testTagName = "modal_dropdown_state",
                    onOptionSelected = { chosenStateName ->
                        val foundState = states.find { it.stateName == chosenStateName } ?: states.first()
                        selectedStateObj = foundState
                        selectedDistrictObj = foundState.districts.first()
                        selectedCityObj = foundState.districts.first().cities.first()
                    }
                )

                CascadingLocationDropdownField(
                    label = "District",
                    selectedValue = selectedDistrictObj.districtName,
                    options = selectedStateObj.districts.map { it.districtName },
                    enabled = true,
                    placeholderText = "Select District",
                    testTagName = "modal_dropdown_district",
                    onOptionSelected = { chosenDistrictName ->
                        val foundDist = selectedStateObj.districts.find { it.districtName == chosenDistrictName }
                            ?: selectedStateObj.districts.first()
                        selectedDistrictObj = foundDist
                        selectedCityObj = foundDist.cities.first()
                    }
                )

                CascadingLocationDropdownField(
                    label = "City / Locality",
                    selectedValue = selectedCityObj.cityName,
                    options = selectedDistrictObj.cities.map { it.cityName },
                    enabled = true,
                    placeholderText = "Select City",
                    testTagName = "modal_dropdown_city",
                    onOptionSelected = { chosenCityName ->
                        val foundCity = selectedDistrictObj.cities.find { it.cityName == chosenCityName }
                            ?: selectedDistrictObj.cities.first()
                        selectedCityObj = foundCity
                    }
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Neutral Case Title (Required)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_incident_title_input")
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Factual Summary & Source Reference (Required)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_incident_summary_input"),
                    minLines = 2
                )
                OutlinedTextField(
                    value = fullReport,
                    onValueChange = { fullReport = it },
                    label = { Text("Additional Confirmed Details (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = personalNotes,
                    onValueChange = { personalNotes = it },
                    label = { Text("Private Note (Visible only to you)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        title,
                        summary,
                        fullReport.ifBlank { summary },
                        selectedStateObj.stateName,
                        selectedDistrictObj.districtName,
                        selectedCityObj.cityName,
                        mlaInput,
                        mpInput,
                        victimCategory,
                        crimeCategory,
                        caseStage,
                        personalNotes
                    )
                },
                enabled = title.trim().length >= 5 && summary.trim().length >= 8,
                modifier = Modifier.testTag("submit_new_incident_button")
            ) {
                Text("Save to Archive")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class SafetySectionTab(val label: String) {
    EMERGENCY_SUPPORT("Emergency Support"),
    KNOW_YOUR_RIGHTS("Know Your Rights"),
    SAFETY_AND_CIVIC("Safety & Representatives")
}

/**
 * Redesigned "Safety & Law" Screen:
 * Organized into 3 clear, trustworthy sections:
 * 1. Emergency Support (Verified helplines with official source & when-to-use description)
 * 2. Know Your Rights (Relevant legal protections, reporting workflows, official government links)
 * 3. Safety Information & Constituency Reference Lookup
 */
@Composable
fun DataAndSafetyScreen(
    states: List<StateJurisdiction>,
    helplines: List<EmergencyHelpline>,
    legalGuides: List<LegalRightGuide>,
    onSelectStateForFeed: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(SafetySectionTab.EMERGENCY_SUPPORT) }
    var selectedLookupState by remember { mutableStateOf(states.first()) }
    var selectedLookupDistrict by remember(selectedLookupState) { mutableStateOf(selectedLookupState.districts.first()) }
    var selectedLookupCity by remember(selectedLookupDistrict) { mutableStateOf(selectedLookupDistrict.cities.first()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("data_and_safety_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Safety, Helplines & Legal Rights",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Verified emergency support numbers, statutory reporting guides, and public-safety resources.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SafetySectionTab.entries.forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("safety_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            SafetySectionTab.EMERGENCY_SUPPORT -> {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "All helplines below are official toll-free or statutory government numbers operated by MHA, MWCD, and the National Commission for Women (NCW).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(helplines, key = { it.id }) { helpline ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = helpline.number,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = helpline.availability,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = helpline.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = helpline.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Official Source: Govt. of India (${helpline.targetGroup})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${helpline.number}")
                                    }
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("dial_helpline_${helpline.number}"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call ${helpline.title}",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call", maxLines = 1)
                            }
                        }
                    }
                }
            }

            SafetySectionTab.KNOW_YOUR_RIGHTS -> {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Public Legal Information Notice",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "The summaries below provide general awareness of statutory protections under the Bharatiya Nagarik Suraksha Sanhita (BNSS), Bharatiya Nyaya Sanhita (BNS), and POCSO Act. They do not constitute personalized legal counsel. For free legal aid, contact NALSA Helpline 15100.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(legalGuides, key = { it.id }) { guide ->
                    ExpandableLegalGuideCard(guide = guide)
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Official Government Reporting Portals",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OfficialPortalRow(
                                title = "National Commission for Women (NCW)",
                                url = "https://ncw.nic.in/",
                                description = "Online complaint registration & 24x7 women safety cell"
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            OfficialPortalRow(
                                title = "National Cyber Crime Reporting Portal",
                                url = "https://cybercrime.gov.in/",
                                description = "Report online harassment, morphing, blackmail, or stalking (Helpline 1930)"
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            OfficialPortalRow(
                                title = "NCPCR POCSO e-Box (Child Protection)",
                                url = "https://ncpcr.gov.in/",
                                description = "Confidential reporting of offences against children and school safety lapses"
                            )
                        }
                    }
                }
            }

            SafetySectionTab.SAFETY_AND_CIVIC -> {
                // Practical School, Campus & Online Safety Guides
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "School, Campus & Online Safety Standards",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            SafetyBulletItem(
                                title = "School & Classroom Access Control",
                                body = "Under NCPCR School Safety Guidelines, schools must verify all visitors at perimeter gates, prohibit unauthorized weapons or tools on campus, and maintain functional CCTV in corridors."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SafetyBulletItem(
                                title = "University & Workplace Internal Committees (POSH)",
                                body = "Every higher-education institution and workplace with 10+ employees is legally required to maintain an Internal Complaints Committee (ICC) and display contact details prominently."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SafetyBulletItem(
                                title = "Digital & Cyber Evidence Preservation",
                                body = "In cases of online threats or stalking, preserve screenshots with visible URLs/timestamps and report immediately via 1930 or cybercrime.gov.in before blocking accounts."
                            )
                        }
                    }
                }

                // Dedicated Constituency MP/MLA Reference Lookup
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Constituency Representative Directory",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Look up verified MLA & MP records by State, District, and City",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            CascadingLocationDropdownField(
                                label = "State / UT",
                                selectedValue = selectedLookupState.stateName,
                                options = states.map { it.stateName },
                                enabled = true,
                                placeholderText = "Select State",
                                testTagName = "safety_lookup_state",
                                onOptionSelected = { stName ->
                                    val found = states.find { it.stateName == stName } ?: states.first()
                                    selectedLookupState = found
                                    selectedLookupDistrict = found.districts.first()
                                    selectedLookupCity = found.districts.first().cities.first()
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CascadingLocationDropdownField(
                                    label = "District",
                                    selectedValue = selectedLookupDistrict.districtName,
                                    options = selectedLookupState.districts.map { it.districtName },
                                    enabled = true,
                                    placeholderText = "Select District",
                                    testTagName = "safety_lookup_district",
                                    onOptionSelected = { distName ->
                                        val found = selectedLookupState.districts.find { it.districtName == distName }
                                            ?: selectedLookupState.districts.first()
                                        selectedLookupDistrict = found
                                        selectedLookupCity = found.cities.first()
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                CascadingLocationDropdownField(
                                    label = "City / Constituency",
                                    selectedValue = selectedLookupCity.cityName,
                                    options = selectedLookupDistrict.cities.map { it.cityName },
                                    enabled = true,
                                    placeholderText = "Select City",
                                    testTagName = "safety_lookup_city",
                                    onOptionSelected = { cityName ->
                                        val found = selectedLookupDistrict.cities.find { it.cityName == cityName }
                                            ?: selectedLookupDistrict.cities.first()
                                        selectedLookupCity = found
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            CompactRepresentativeCard(
                                cityRep = selectedLookupCity,
                                stateName = selectedLookupState.stateName,
                                districtName = selectedLookupDistrict.districtName
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { onSelectStateForFeed(selectedLookupState.stateName) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("View Reports in ${selectedLookupState.stateName}", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableLegalGuideCard(guide: LegalRightGuide) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = guide.codeBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse guide" else "Expand guide",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = guide.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = guide.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    guide.actionChecklist.forEach { step ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Statutory Reference: ${guide.statutoryCitation}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OfficialPortalRow(
    title: String,
    url: String,
    description: String
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open $title",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SafetyBulletItem(
    title: String,
    body: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EditorialLoadingSkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .testTag("editorial_loading_skeleton"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(22.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            )
        }
    }
}

