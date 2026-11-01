package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.SurakshaDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import com.example.data.remote.IndianCrimeNewsRssClient
import com.example.data.repository.MpsRepository
import com.example.data.repository.SurakshaFirestoreRepository
import com.example.ui.auth.SurakshaAuthScreen
import com.example.ui.auth.signOutFromSuraksha
import com.example.ui.screens.CaseDossierDetailScreen
import com.example.ui.screens.DataAndSafetyScreen
import com.example.ui.screens.FollowUpTrackerScreen
import com.example.ui.screens.FrontPageCaseFeedScreen
import com.example.ui.screens.HomeSearchableTopAppBar
import com.example.ui.screens.MpsOfIndiaScreen
import com.example.ui.screens.PermanentVaultScreen
import com.example.ui.theme.SurakshaChronicleTheme
import com.example.ui.viewmodel.MpsViewModel
import com.example.ui.viewmodel.SurakshaViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SurakshaRootApp()
        }
    }
}

@Composable
fun SurakshaRootApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        SurakshaChronicleTheme {
            SurakshaAuthScreen(onAuthSuccess = {})
        }
    } else {
        SurakshaAuthenticatedShell(currentUserId = user.uid)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurakshaAuthenticatedShell(currentUserId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: SurakshaViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val firestore = FirebaseFirestore.getInstance(databaseId)
                val roomDb = SurakshaDatabase.getInstance(app)
                SurakshaViewModel(
                    firestoreRepository = SurakshaFirestoreRepository(firestore),
                    offlineDao = roomDb.savedCaseOfflineDao(),
                    rssClient = IndianCrimeNewsRssClient(),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val mpsViewModel: MpsViewModel = viewModel(
        key = "mps_directory",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                MpsViewModel(repository = MpsRepository(app))
            }
        }
    )
    val mpsUiState by mpsViewModel.uiState.collectAsStateWithLifecycle()

    val allDossiers by viewModel.allDossiers.collectAsStateWithLifecycle()
    val filteredDossiers by viewModel.filteredDossiers.collectAsStateWithLifecycle()
    val savedForLifeDossiers by viewModel.savedForLifeDossiers.collectAsStateWithLifecycle()
    val followedDossiers by viewModel.followedDossiers.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val globalSensitiveBlur by viewModel.globalSensitiveBlur.collectAsStateWithLifecycle()
    val revealedCaseIds by viewModel.revealedCaseIds.collectAsStateWithLifecycle()
    val isRefreshingLive by viewModel.isRefreshingLive.collectAsStateWithLifecycle()
    val selectedDossierId by viewModel.selectedDossierId.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val feedSyncNote by viewModel.feedSyncNote.collectAsStateWithLifecycle()
    val visibleFeedLimit by viewModel.visibleFeedLimit.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val correctionLogs by viewModel.correctionLogs.collectAsStateWithLifecycle()

    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var showSearchBar by rememberSaveable { mutableStateOf(true) }
    var showNotificationsDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(bannerMessage) {
        bannerMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBannerMessage()
        }
    }

    val selectedDossier = remember(allDossiers, selectedDossierId) {
        allDossiers.find { it.id == selectedDossierId }
    }

    SurakshaChronicleTheme(
        darkTheme = useDarkTheme,
        fontScale = fontScale
    ) {
        if (selectedDossier != null) {
            val relatedDossiers = remember(allDossiers, selectedDossier.id) {
                allDossiers.filter {
                    it.id != selectedDossier.id &&
                        (it.stateName.equals(selectedDossier.stateName, ignoreCase = true) ||
                            it.crimeCategory.equals(selectedDossier.crimeCategory, ignoreCase = true))
                }
            }
            CaseDossierDetailScreen(
                dossier = selectedDossier,
                onBack = { viewModel.openDossierDetail(null) },
                onToggleSaveForLife = { notes ->
                    viewModel.toggleSaveForLife(selectedDossier, notes)
                },
                onToggleFollow = {
                    viewModel.toggleFollowCase(selectedDossier)
                },
                onAddFollowUp = { dateLabel, stage, headline, details ->
                    viewModel.addFollowUpMilestone(
                        dossier = selectedDossier,
                        updateDateLabel = dateLabel,
                        newStage = stage,
                        headline = headline,
                        details = details
                    )
                },
                onSubmitCorrection = { type, details ->
                    viewModel.submitCorrectionOrPrivacyReport(selectedDossier.id, type, details)
                },
                relatedDossiers = relatedDossiers,
                onOpenRelatedDossier = { related ->
                    viewModel.openDossierDetail(related.id)
                },
                correctionLogs = correctionLogs[selectedDossier.id] ?: emptyList()
            )
            return@SurakshaChronicleTheme
        }

        BackHandler(enabled = currentTab != 0) {
            currentTab = 0
        }

        val availableDistricts = remember(filterState.selectedState) {
            viewModel.getAvailableDistricts(filterState.selectedState)
        }
        val availableCities = remember(filterState.selectedState, filterState.selectedDistrict) {
            viewModel.getAvailableCities(filterState.selectedState, filterState.selectedDistrict)
        }
        val activeRepresentative = remember(
            filterState.selectedState,
            filterState.selectedDistrict,
            filterState.selectedCity
        ) {
            viewModel.getActiveConstituencyRepresentative(
                filterState.selectedState,
                filterState.selectedDistrict,
                filterState.selectedCity
            )
        }

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                HomeSearchableTopAppBar(
                    searchQuery = filterState.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    titleText = if (appLanguage == AppLanguage.HINDI) {
                        "सुरक्षा क्रॉनिकल"
                    } else {
                        "Suraksha Chronicle"
                    },
                    subtitleText = if (appLanguage == AppLanguage.HINDI) {
                        "शीर्षक से आगे। तथ्यों का अनुसरण करें।"
                    } else {
                        "Beyond the headline. Follow the facts."
                    },
                    showSearchInput = (currentTab == 0 && showSearchBar),
                    onSearchButtonClick = {
                        showSearchBar = true
                        if (currentTab != 0) currentTab = 0
                    },
                    extraActions = {
                        IconButton(
                            onClick = { showNotificationsDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("top_bar_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (followedDossiers.isNotEmpty() && notificationsEnabled) {
                                        Badge { Text("${followedDossiers.size}") }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (notificationsEnabled) {
                                        Icons.Default.Notifications
                                    } else {
                                        Icons.Default.NotificationsNone
                                    },
                                    contentDescription = "Case Update Notifications",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.toggleGlobalSensitiveBlur() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("toggle_sensitive_blur_button")
                        ) {
                            Icon(
                                imageVector = if (globalSensitiveBlur) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = "Toggle Sensitive Content Blur",
                                tint = if (globalSensitiveBlur) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("top_bar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "App Settings & Preferences",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    val homeLabel = if (appLanguage == AppLanguage.HINDI) "होम" else "Home"
                    val trackLabel = if (appLanguage == AppLanguage.HINDI) "ट्रैक" else "Track"
                    val savedLabel = if (appLanguage == AppLanguage.HINDI) "सहेजे गए" else "Saved"
                    val safetyLabel = if (appLanguage == AppLanguage.HINDI) "सुरक्षा" else "Safety"
                    val mpsLabel = if (appLanguage == AppLanguage.HINDI) "सांसद" else "MPs"

                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = {
                            Icon(Icons.Default.Newspaper, contentDescription = homeLabel)
                        },
                        label = {
                            Text(
                                text = homeLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_feed")
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (followedDossiers.isNotEmpty()) {
                                        Badge { Text("${followedDossiers.size}") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.History, contentDescription = trackLabel)
                            }
                        },
                        label = {
                            Text(
                                text = trackLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_followup")
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (savedForLifeDossiers.isNotEmpty()) {
                                        Badge { Text("${savedForLifeDossiers.size}") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.BookmarkAdded, contentDescription = savedLabel)
                            }
                        },
                        label = {
                            Text(
                                text = savedLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_vault")
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = {
                            Icon(Icons.Default.Gavel, contentDescription = safetyLabel)
                        },
                        label = {
                            Text(
                                text = safetyLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_safety")
                    )
                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { currentTab = 4 },
                        icon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = mpsLabel)
                        },
                        label = {
                            Text(
                                text = mpsLabel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_mps")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> FrontPageCaseFeedScreen(
                        dossiers = filteredDossiers,
                        savedCount = savedForLifeDossiers.size,
                        states = viewModel.allStates,
                        filterState = filterState,
                        districts = availableDistricts,
                        cities = availableCities,
                        activeRepresentative = activeRepresentative,
                        globalBlur = globalSensitiveBlur,
                        revealedIds = revealedCaseIds,
                        isRefreshingLive = isRefreshingLive,
                        visibleFeedLimit = visibleFeedLimit,
                        feedSyncNote = feedSyncNote,
                        showSearchBar = showSearchBar,
                        showTopAppBar = false,
                        onSelectState = viewModel::selectState,
                        onSelectDistrict = viewModel::selectDistrict,
                        onSelectCity = viewModel::selectCity,
                        onApplyLocation = viewModel::applyLocationSelection,
                        onResetFilters = viewModel::resetAllFilters,
                        onSelectVictimGroup = viewModel::selectVictimGroup,
                        onSelectCrimeCategory = viewModel::selectCrimeCategory,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onRefreshLive = { viewModel.refreshLiveIndianNews() },
                        onLoadMore = viewModel::loadMoreFeedItems,
                        onRevealBlur = viewModel::revealCaseContent,
                        onOpenDossier = { viewModel.openDossierDetail(it.id) },
                        onToggleSaveForLife = { viewModel.toggleSaveForLife(it) },
                        onToggleFollow = { viewModel.toggleFollowCase(it) },
                        onAddFollowUp = { dossier, dateLabel, stage, headline, details ->
                            viewModel.addFollowUpMilestone(
                                dossier = dossier,
                                updateDateLabel = dateLabel,
                                newStage = stage,
                                headline = headline,
                                details = details
                            )
                        },
                        onArchiveNewIncident = viewModel::archiveNewIncidentForLife
                    )
                    1 -> FollowUpTrackerScreen(
                        allDossiers = filteredDossiers,
                        states = viewModel.allStates,
                        filterState = filterState,
                        districts = availableDistricts,
                        cities = availableCities,
                        activeRepresentative = activeRepresentative,
                        globalBlur = globalSensitiveBlur,
                        revealedIds = revealedCaseIds,
                        onSelectState = viewModel::selectState,
                        onSelectDistrict = viewModel::selectDistrict,
                        onSelectCity = viewModel::selectCity,
                        onApplyLocation = viewModel::applyLocationSelection,
                        onRevealBlur = viewModel::revealCaseContent,
                        onOpenDossier = { viewModel.openDossierDetail(it.id) },
                        onToggleSaveForLife = { viewModel.toggleSaveForLife(it) },
                        onToggleFollow = { viewModel.toggleFollowCase(it) },
                        onAddFollowUp = { dossier, dateLabel, stage, headline, details ->
                            viewModel.addFollowUpMilestone(
                                dossier = dossier,
                                updateDateLabel = dateLabel,
                                newStage = stage,
                                headline = headline,
                                details = details
                            )
                        },
                        onQuickSelectJurisdiction = { st, dist, ct ->
                            viewModel.applyLocationSelection(st, dist, ct)
                        }
                    )
                    2 -> PermanentVaultScreen(
                        savedDossiers = savedForLifeDossiers,
                        followedDossiers = followedDossiers,
                        states = viewModel.allStates,
                        globalBlur = globalSensitiveBlur,
                        revealedIds = revealedCaseIds,
                        onRevealBlur = viewModel::revealCaseContent,
                        onOpenDossier = { viewModel.openDossierDetail(it.id) },
                        onToggleSaveForLife = { viewModel.toggleSaveForLife(it) },
                        onToggleFollow = { viewModel.toggleFollowCase(it) },
                        onAddFollowUp = { dossier, dateLabel, stage, headline, details ->
                            viewModel.addFollowUpMilestone(
                                dossier = dossier,
                                updateDateLabel = dateLabel,
                                newStage = stage,
                                headline = headline,
                                details = details
                            )
                        },
                        onArchiveNewIncident = viewModel::archiveNewIncidentForLife,
                        onQuickSelectJurisdiction = { st, dist, ct ->
                            viewModel.applyLocationSelection(st, dist, ct)
                            currentTab = 0
                        }
                    )
                    3 -> DataAndSafetyScreen(
                        states = viewModel.allStates,
                        helplines = viewModel.emergencyHelplines,
                        legalGuides = viewModel.legalRightsGuides,
                        onSelectStateForFeed = { stateName ->
                            viewModel.selectState(stateName)
                            currentTab = 0
                        }
                    )
                    4 -> MpsOfIndiaScreen(
                        uiState = mpsUiState,
                        onSelectHouse = mpsViewModel::selectHouse,
                        onSearchQueryChange = mpsViewModel::updateSearchQuery,
                        onSelectState = mpsViewModel::selectStateFilter,
                        onSelectParty = mpsViewModel::selectPartyFilter,
                        onResetFilters = mpsViewModel::resetFilters,
                        onOpenLokSabhaMp = mpsViewModel::openLokSabhaMpDetail,
                        onOpenRajyaSabhaMp = mpsViewModel::openRajyaSabhaMpDetail,
                        onCloseMpDetail = mpsViewModel::closeMpDetail
                    )
                }
            }
        }

        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                title = {
                    Text(
                        text = "Case Update Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Followed Case Notifications",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Notify when chargesheets, court hearings, or verified updates are logged.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { viewModel.toggleNotificationsEnabled() },
                                modifier = Modifier.testTag("toggle_notifications_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        if (followedDossiers.isEmpty()) {
                            Text(
                                text = "You are not following any cases yet. Tap 'Follow' on any news report or case card to track confirmed milestones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Currently following ${followedDossiers.size} case(s):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            followedDossiers.take(4).forEach { dossier ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = dossier.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${dossier.caseStage.label} • ${dossier.timelineUpdates.lastOrNull()?.dateLabel ?: dossier.incidentDateLabel}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotificationsDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = {
                    Text(
                        text = "Reading & Display Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(mode.label, maxLines = 1) },
                                    modifier = Modifier.testTag("theme_mode_${mode.name.lowercase()}")
                                )
                            }
                        }

                        Text(
                            text = "Text Size",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                1.0f to "1.0x Standard",
                                1.3f to "1.3x Large",
                                2.0f to "2.0x Max"
                            ).forEach { (scaleValue, label) ->
                                FilterChip(
                                    selected = kotlin.math.abs(fontScale - scaleValue) < 0.05f,
                                    onClick = { viewModel.setFontScale(scaleValue) },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag("font_scale_${label.lowercase().replace(" ", "_")}")
                                )
                            }
                        }

                        Text(
                            text = "Interface Language",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = appLanguage == lang,
                                    onClick = { viewModel.setAppLanguage(lang) },
                                    label = { Text(lang.label, maxLines = 1) },
                                    modifier = Modifier.testTag("lang_${lang.name.lowercase()}")
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sensitive Headline Blur",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Require tap-to-read on graphic incident summaries",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = globalSensitiveBlur,
                                onCheckedChange = { viewModel.toggleGlobalSensitiveBlur() }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        OutlinedButton(
                            onClick = {
                                showSettingsDialog = false
                                signOutFromSuraksha(
                                    credentialManager = credentialManager,
                                    onSignOutComplete = {},
                                    scope = scope
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("sign_out_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Out of Archive",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
