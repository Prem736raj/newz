package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.SurakshaDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AppLanguage
import com.example.data.model.LanguageTier
import com.example.data.model.ThemeMode
import com.example.data.remote.IndianCrimeNewsRssClient
import com.example.data.remote.SurakshaTranslationService
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
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

private fun configureAppCheck(context: Context, intent: Intent) {
    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    if (!isDebuggable) {
        return
    }

    val debugToken = intent.getStringExtra("app_check_debug_token")
    if (debugToken != null) {
        context.getSharedPreferences(
            "com.google.firebase.appcheck.debug.store",
            Context.MODE_PRIVATE
        ).edit {
            putString("com.google.firebase.appcheck.debug.ARCHITECTURAL_DEBUG_SECRET", debugToken)
        }
    }

    Firebase.appCheck.installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching { configureAppCheck(this, intent) }
        enableEdgeToEdge()
        setContent {
            SurakshaRootApp()
        }
    }
}

@Composable
fun SurakshaRootApp(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val initialPrefs = remember(prefsRepo) { prefsRepo.readInitialPreferencesBlocking() }
    val userPrefs by prefsRepo.preferencesFlow.collectAsStateWithLifecycle(initialValue = initialPrefs)

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var showSignInScreen by rememberSaveable { mutableStateOf(false) }

    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (userPrefs.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    if (!userPrefs.hasCompletedLanguageOnboarding) {
        SurakshaChronicleTheme(
            darkTheme = useDarkTheme,
            appLanguage = userPrefs.appLanguage,
            fontScale = userPrefs.fontScale
        ) {
            FirstRunLanguageOnboardingScreen(
                selectedLanguage = userPrefs.appLanguage,
                onSelectLanguage = { chosen ->
                    scope.launch {
                        prefsRepo.setAppLanguage(chosen)
                    }
                },
                onConfirm = { chosen ->
                    scope.launch {
                        prefsRepo.completeLanguageOnboarding(chosen)
                    }
                }
            )
        }
        return
    }

    if (showSignInScreen && currentUser == null) {
        SurakshaChronicleTheme(
            darkTheme = useDarkTheme,
            appLanguage = userPrefs.appLanguage,
            fontScale = userPrefs.fontScale
        ) {
            SurakshaAuthScreen(
                onAuthSuccess = { showSignInScreen = false },
                onContinueAsGuest = { showSignInScreen = false }
            )
        }
    } else {
        SurakshaAuthenticatedShell(
            currentUserId = currentUser?.uid ?: "guest_user",
            onRequestSignIn = { showSignInScreen = true }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirstRunLanguageOnboardingScreen(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onConfirm: (AppLanguage) -> Unit
) {
    val tierALanguages = remember { AppLanguage.entries.filter { it.tier == LanguageTier.TIER_A } }
    val tierBLanguages = remember { AppLanguage.entries.filter { it.tier == LanguageTier.TIER_B } }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("first_run_language_onboarding"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.onboarding_lang_badge),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.onboarding_lang_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = stringResource(R.string.onboarding_lang_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.onboarding_tier_a_header),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tierALanguages.forEach { lang ->
                    LanguageSelectionCard(
                        language = lang,
                        selected = selectedLanguage == lang,
                        onClick = { onSelectLanguage(lang) }
                    )
                }
            }

            Text(
                text = stringResource(R.string.onboarding_tier_b_header),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tierBLanguages.forEach { lang ->
                    LanguageSelectionCard(
                        language = lang,
                        selected = selectedLanguage == lang,
                        onClick = { onSelectLanguage(lang) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onConfirm(selectedLanguage) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("onboarding_continue_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_continue_btn, selectedLanguage.nativeName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectionCard(
    language: AppLanguage,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
            .testTag("onboarding_lang_${language.code}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = language.englishName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SurakshaAuthenticatedShell(
    currentUserId: String,
    onRequestSignIn: () -> Unit = {}
) {
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
                val roomDb = SurakshaDatabase.getInstance(app, currentUserId)
                val prefsRepo = UserPreferencesRepository(app)
                val translationDao = roomDb.translationCacheDao()
                SurakshaViewModel(
                    firestoreRepository = SurakshaFirestoreRepository(firestore),
                    offlineDao = roomDb.savedCaseOfflineDao(),
                    rssClient = IndianCrimeNewsRssClient(),
                    currentUserId = currentUserId,
                    userPreferencesRepository = prefsRepo,
                    translationCacheDao = translationDao,
                    translationService = SurakshaTranslationService(translationDao)
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
    val localizedHelplines by viewModel.localizedHelplines.collectAsStateWithLifecycle()
    val localizedLegalGuides by viewModel.localizedLegalGuides.collectAsStateWithLifecycle()
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
    var showGuestSignInRequiredDialog by rememberSaveable { mutableStateOf(false) }

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
        appLanguage = appLanguage,
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
                    if (viewModel.isGuestUser) {
                        showGuestSignInRequiredDialog = true
                    } else {
                        viewModel.toggleSaveForLife(selectedDossier, notes)
                    }
                },
                onToggleFollow = {
                    if (viewModel.isGuestUser) {
                        showGuestSignInRequiredDialog = true
                    } else {
                        viewModel.toggleFollowCase(selectedDossier)
                    }
                },
                onAddFollowUp = { dateLabel, stage, headline, details ->
                    if (viewModel.isGuestUser) {
                        showGuestSignInRequiredDialog = true
                    } else {
                        viewModel.addFollowUpMilestone(
                            dossier = selectedDossier,
                            updateDateLabel = dateLabel,
                            newStage = stage,
                            headline = headline,
                            details = details
                        )
                    }
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
                    titleText = stringResource(R.string.app_name),
                    subtitleText = stringResource(R.string.app_tagline),
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
                                    contentDescription = stringResource(R.string.cd_notifications),
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
                                contentDescription = stringResource(R.string.cd_sensitive_blur),
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
                                contentDescription = stringResource(R.string.cd_settings),
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
                    val homeLabel = stringResource(R.string.nav_home)
                    val trackLabel = stringResource(R.string.nav_track)
                    val savedLabel = stringResource(R.string.nav_saved)
                    val safetyLabel = stringResource(R.string.nav_safety)
                    val mpsLabel = stringResource(R.string.nav_mps)

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
                        onToggleSaveForLife = {
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.toggleSaveForLife(it)
                            }
                        },
                        onToggleFollow = {
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.toggleFollowCase(it)
                            }
                        },
                        onAddFollowUp = { dossier, dateLabel, stage, headline, details ->
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.addFollowUpMilestone(
                                    dossier = dossier,
                                    updateDateLabel = dateLabel,
                                    newStage = stage,
                                    headline = headline,
                                    details = details
                                )
                            }
                        },
                        onArchiveNewIncident = { title, summary, fullReport, st, dist, ct, mla, mp, victim, cat, stage, notes ->
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.archiveNewIncidentForLife(
                                    title, summary, fullReport, st, dist, ct, mla, mp, victim, cat, stage, notes
                                )
                            }
                        }
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
                        onToggleSaveForLife = {
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.toggleSaveForLife(it)
                            }
                        },
                        onToggleFollow = {
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.toggleFollowCase(it)
                            }
                        },
                        onAddFollowUp = { dossier, dateLabel, stage, headline, details ->
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.addFollowUpMilestone(
                                    dossier = dossier,
                                    updateDateLabel = dateLabel,
                                    newStage = stage,
                                    headline = headline,
                                    details = details
                                )
                            }
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
                        onToggleSaveForLife = {
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.toggleSaveForLife(it)
                            }
                        },
                        onToggleFollow = {
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.toggleFollowCase(it)
                            }
                        },
                        onAddFollowUp = { dossier, dateLabel, stage, headline, details ->
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.addFollowUpMilestone(
                                    dossier = dossier,
                                    updateDateLabel = dateLabel,
                                    newStage = stage,
                                    headline = headline,
                                    details = details
                                )
                            }
                        },
                        onArchiveNewIncident = { title, summary, fullReport, st, dist, ct, mla, mp, victim, cat, stage, notes ->
                            if (viewModel.isGuestUser) {
                                showGuestSignInRequiredDialog = true
                            } else {
                                viewModel.archiveNewIncidentForLife(
                                    title, summary, fullReport, st, dist, ct, mla, mp, victim, cat, stage, notes
                                )
                            }
                        },
                        onQuickSelectJurisdiction = { st, dist, ct ->
                            viewModel.applyLocationSelection(st, dist, ct)
                            currentTab = 0
                        }
                    )
                    3 -> DataAndSafetyScreen(
                        states = viewModel.allStates,
                        helplines = localizedHelplines,
                        legalGuides = localizedLegalGuides,
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

        if (showGuestSignInRequiredDialog) {
            AlertDialog(
                onDismissRequest = { showGuestSignInRequiredDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.guest_sign_in_required_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.guest_sign_in_required_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGuestSignInRequiredDialog = false
                            onRequestSignIn()
                        },
                        modifier = Modifier.testTag("guest_prompt_sign_in_button")
                    ) {
                        Text(stringResource(R.string.guest_sign_in_action))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGuestSignInRequiredDialog = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.notifications_dialog_title),
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
                                    text = stringResource(R.string.notifications_followed_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(R.string.notifications_followed_subtitle),
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
                                text = stringResource(R.string.notifications_empty_text),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.notifications_following_count, followedDossiers.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            followedDossiers.take(4).forEach { dossier ->
                                val stageLabel = stringResource(dossier.caseStage.labelRes)
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
                                            text = "$stageLabel • ${dossier.timelineUpdates.lastOrNull()?.dateLabel ?: dossier.incidentDateLabel}",
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
                        Text(stringResource(R.string.action_done))
                    }
                }
            )
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_theme_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(stringResource(mode.labelRes), maxLines = 1) },
                                    modifier = Modifier.testTag("theme_mode_${mode.name.lowercase()}")
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.settings_font_scale_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                0.85f to R.string.settings_font_scale_small,
                                1.0f to R.string.settings_font_scale_standard,
                                1.3f to R.string.settings_font_scale_large,
                                1.6f to R.string.settings_font_scale_max
                            ).forEach { (scaleValue, labelRes) ->
                                FilterChip(
                                    selected = kotlin.math.abs(fontScale - scaleValue) < 0.05f,
                                    onClick = { viewModel.setFontScale(scaleValue) },
                                    label = { Text(stringResource(labelRes), maxLines = 1) },
                                    modifier = Modifier.testTag("font_scale_${scaleValue}")
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.settings_language_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.settings_language_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = appLanguage == lang,
                                    onClick = { viewModel.setAppLanguage(lang) },
                                    label = { Text(lang.nativeName, maxLines = 1) },
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
                                    text = stringResource(R.string.settings_sensitive_blur_title),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = stringResource(R.string.settings_sensitive_blur_subtitle),
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

                        if (viewModel.isGuestUser) {
                            OutlinedButton(
                                onClick = {
                                    showSettingsDialog = false
                                    onRequestSignIn()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("sign_in_from_settings_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Login,
                                    contentDescription = stringResource(R.string.cd_sign_in),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_sign_in_btn),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
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
                                    contentDescription = stringResource(R.string.cd_sign_out),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_sign_out_btn),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text(stringResource(R.string.action_close))
                    }
                }
            )
        }
    }
}
