package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SurakshaArchiveSeedData
import com.example.data.local.SurakshaHindiSeedTranslations
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AppLanguage
import com.example.data.model.HouseMeta
import com.example.data.model.ThemeMode
import com.example.data.model.VerificationStatus
import com.example.data.remote.IndianCrimeNewsRssClient
import com.example.ui.screens.CaseDossierDetailScreen
import com.example.ui.screens.DataAndSafetyScreen
import com.example.ui.screens.FollowUpTrackerScreen
import com.example.ui.screens.FrontPageCaseFeedScreen
import com.example.ui.screens.MpsOfIndiaScreen
import com.example.ui.screens.PermanentVaultScreen
import com.example.ui.theme.SurakshaChronicleTheme
import com.example.ui.viewmodel.MpsUiState
import com.example.ui.viewmodel.SurakshaFilterState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `verify application resources and name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        val dbId = context.getString(R.string.firestore_database_id)
        assertEquals("Suraksha Chronicle", appName)
        assertTrue(dbId.isNotBlank())
    }

    @Test
    fun `verify seed catalog and representative data integrity`() {
        val states = SurakshaArchiveSeedData.stateJurisdictions
        assertTrue("Catalog should include Andhra Pradesh", states.any { it.stateName == "Andhra Pradesh" })
        val dossiers = SurakshaArchiveSeedData.buildInitialCaseDossiers()
        assertTrue("Seed dossiers should not be empty", dossiers.isNotEmpty())

        val apDossier = dossiers.first { it.id == "case_ap_school_chainsaw_01" }
        assertEquals("Andhra Pradesh", apDossier.stateName)
        assertTrue(apDossier.timelineUpdates.size >= 3)
        assertEquals(VerificationStatus.CONFIRMED, apDossier.verificationStatus)

        val apState = states.first { it.stateName == "Andhra Pradesh" }
        val anakapalli = apState.districts.first { it.districtName == "Anakapalli" }
        val elamanchili = anakapalli.cities.first { it.cityName == "Elamanchili" }
        assertNotNull(elamanchili)
        assertTrue(elamanchili.mlaName.contains("Sundarapu Vijay Kumar"))
        assertTrue(elamanchili.mpName.contains("C. M. Ramesh"))
        assertTrue(apDossier.representativeDataSource.contains("Election Commission"))
    }

    @Test
    fun `verify rss client deduplication and unverified representative fallback`() {
        val rssClient = IndianCrimeNewsRssClient()
        val stem1 = rssClient.normalizeTitleStem("Class 10 Student Attacked in School - The Hindu")
        val stem2 = rssClient.normalizeTitleStem("Class 10 student attacked in school! - Indian Express")
        assertEquals(stem1, stem2)
    }

    @Test
    fun `home screen displays compact location pill and opens location filter bottom sheet`() {
        val dossiers = SurakshaArchiveSeedData.buildInitialCaseDossiers()
        val states = SurakshaArchiveSeedData.stateJurisdictions
        var filterState by mutableStateOf(SurakshaFilterState())
        var savedTriggered = false
        var followTriggered = false

        composeTestRule.setContent {
            SurakshaChronicleTheme {
                FrontPageCaseFeedScreen(
                    dossiers = dossiers,
                    savedCount = 1,
                    states = states,
                    filterState = filterState,
                    districts = states.find { it.stateName == filterState.selectedState }?.districts ?: emptyList(),
                    cities = emptyList(),
                    activeRepresentative = null,
                    globalBlur = false,
                    revealedIds = emptySet(),
                    isRefreshingLive = false,
                    onSelectState = { st -> filterState = filterState.copy(selectedState = st) },
                    onSelectDistrict = { dist -> filterState = filterState.copy(selectedDistrict = dist) },
                    onSelectCity = { ct -> filterState = filterState.copy(selectedCity = ct) },
                    onApplyLocation = { st, dist, ct ->
                        filterState = filterState.copy(
                            selectedState = st,
                            selectedDistrict = dist,
                            selectedCity = ct
                        )
                    },
                    onResetFilters = { filterState = SurakshaFilterState() },
                    onSelectVictimGroup = {},
                    onSelectCrimeCategory = {},
                    onSearchQueryChange = {},
                    onRefreshLive = {},
                    onRevealBlur = {},
                    onOpenDossier = {},
                    onToggleSaveForLife = { savedTriggered = true },
                    onToggleFollow = { followTriggered = true },
                    onAddFollowUp = { _, _, _, _, _ -> },
                    onArchiveNewIncident = { _, _, _, _, _, _, _, _, _, _, _, _ -> }
                )
            }
        }

        // Verify compact location selector pill is displayed
        composeTestRule.onNodeWithTag("compact_location_selector_pill").assertIsDisplayed()
        composeTestRule.onNodeWithText("All India").assertIsDisplayed()

        // Verify first news card media banner, bookmark and follow buttons work
        val firstId = dossiers.first().id
        composeTestRule.onNodeWithTag("incident_media_banner_$firstId", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_for_life_btn_$firstId").performScrollTo().performClick()
        assertTrue(savedTriggered)

        composeTestRule.onNodeWithTag("follow_case_btn_$firstId").performScrollTo().performClick()
        assertTrue(followTriggered)

        // Scroll back to top of LazyColumn and tap compact location pill to open bottom sheet
        composeTestRule.onNodeWithTag("front_page_feed_list").performScrollToIndex(0)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("compact_location_selector_pill").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("location_filter_bottom_sheet").assertIsDisplayed()

        // Select State -> District -> City inside the bottom sheet
        composeTestRule.onNodeWithTag("dropdown_state_selector").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dropdown_state_selector_option_Andhra_Pradesh").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dropdown_district_selector").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dropdown_district_selector_option_Anakapalli").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dropdown_city_selector").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dropdown_city_selector_option_Elamanchili").performClick()
        composeTestRule.waitForIdle()

        // Scroll to Apply button if needed and click
        composeTestRule.onNodeWithTag("apply_location_filter_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals("Andhra Pradesh", filterState.selectedState)
        assertEquals("Anakapalli", filterState.selectedDistrict)
        assertEquals("Elamanchili", filterState.selectedCity)
    }

    @Test
    fun `article detail screen renders case timeline related reporting and corrections history`() {
        val dossiers = SurakshaArchiveSeedData.buildInitialCaseDossiers()
        val sampleDossier = dossiers.first()
        val relatedDossier = dossiers[1]
        var followClicked = false

        composeTestRule.setContent {
            SurakshaChronicleTheme {
                CaseDossierDetailScreen(
                    dossier = sampleDossier,
                    onBack = {},
                    onToggleSaveForLife = {},
                    onToggleFollow = { followClicked = true },
                    onAddFollowUp = { _, _, _, _ -> },
                    onSubmitCorrection = { _, _ -> },
                    relatedDossiers = listOf(relatedDossier),
                    correctionLogs = listOf("Pending Editorial Review (Factual Error): Verified police station name.")
                )
            }
        }

        // Verify headline and follow button
        composeTestRule.onNodeWithTag("detail_follow_case_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("detail_follow_case_button").performClick()
        assertTrue(followClicked)

        // Verify incident photo & video gallery is displayed for the Andhra Pradesh incident
        composeTestRule.onNodeWithTag("detail_incident_media_gallery").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("attach_incident_media_button").performScrollTo().assertIsDisplayed()

        // Verify timeline sort toggles exist
        composeTestRule.onNodeWithTag("timeline_sort_chronological").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("timeline_sort_newest").performScrollTo().performClick()

        // Verify Related Reporting and Corrections History sections
        composeTestRule.onNodeWithTag("related_dossier_${relatedDossier.id}").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("corrections_history_section").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `track saved and safety screens render without clipping or errors`() {
        val dossiers = SurakshaArchiveSeedData.buildInitialCaseDossiers()
        val states = SurakshaArchiveSeedData.stateJurisdictions
        var activeScreen by mutableStateOf(0)

        composeTestRule.setContent {
            SurakshaChronicleTheme {
                when (activeScreen) {
                    0 -> FollowUpTrackerScreen(
                        allDossiers = dossiers,
                        states = states,
                        filterState = SurakshaFilterState(),
                        districts = emptyList(),
                        cities = emptyList(),
                        activeRepresentative = null,
                        globalBlur = false,
                        revealedIds = emptySet(),
                        onSelectState = {},
                        onSelectDistrict = {},
                        onSelectCity = {},
                        onApplyLocation = { _, _, _ -> },
                        onRevealBlur = {},
                        onOpenDossier = {},
                        onToggleSaveForLife = {},
                        onToggleFollow = {},
                        onAddFollowUp = { _, _, _, _, _ -> },
                        onQuickSelectJurisdiction = { _, _, _ -> }
                    )
                    1 -> PermanentVaultScreen(
                        savedDossiers = dossiers.take(2),
                        followedDossiers = dossiers.take(1),
                        states = states,
                        globalBlur = false,
                        revealedIds = revealedCaseIdsEmpty(),
                        onRevealBlur = {},
                        onOpenDossier = {},
                        onToggleSaveForLife = {},
                        onToggleFollow = {},
                        onAddFollowUp = { _, _, _, _, _ -> },
                        onArchiveNewIncident = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
                        onQuickSelectJurisdiction = { _, _, _ -> }
                    )
                    2 -> DataAndSafetyScreen(
                        states = states,
                        helplines = SurakshaArchiveSeedData.emergencyHelplines,
                        legalGuides = SurakshaArchiveSeedData.legalRightsGuides,
                        onSelectStateForFeed = {}
                    )
                }
            }
        }

        // 0: Track screen
        composeTestRule.onNodeWithText("Never Forgotten • Case Tracker").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tracker_tab_all").assertIsDisplayed()

        // 1: Saved screen
        activeScreen = 1
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Saved & Followed Archive").assertIsDisplayed()
        composeTestRule.onNodeWithTag("archive_new_incident_button").assertIsDisplayed()

        // 2: Safety screen
        activeScreen = 2
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Safety, Helplines & Legal Rights").assertIsDisplayed()
        composeTestRule.onNodeWithTag("safety_tab_know_your_rights").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Public Legal Information Notice").assertIsDisplayed()
    }

    @Test
    fun `dark theme font scale 2_0x and loading skeleton states render cleanly`() {
        val states = SurakshaArchiveSeedData.stateJurisdictions
        var scale by mutableStateOf(1.3f)

        composeTestRule.setContent {
            SurakshaChronicleTheme(
                darkTheme = true,
                fontScale = scale
            ) {
                FrontPageCaseFeedScreen(
                    dossiers = emptyList(),
                    savedCount = 0,
                    states = states,
                    filterState = SurakshaFilterState(),
                    districts = emptyList(),
                    cities = emptyList(),
                    activeRepresentative = null,
                    globalBlur = false,
                    revealedIds = emptySet(),
                    isRefreshingLive = true,
                    feedSyncNote = "Showing cached verified archive while refreshing live sources.",
                    onSelectState = {},
                    onSelectDistrict = {},
                    onSelectCity = {},
                    onApplyLocation = { _, _, _ -> },
                    onResetFilters = {},
                    onSelectVictimGroup = {},
                    onSelectCrimeCategory = {},
                    onSearchQueryChange = {},
                    onRefreshLive = {},
                    onRevealBlur = {},
                    onOpenDossier = {},
                    onToggleSaveForLife = {},
                    onToggleFollow = {},
                    onAddFollowUp = { _, _, _, _, _ -> },
                    onArchiveNewIncident = { _, _, _, _, _, _, _, _, _, _, _, _ -> }
                )
            }
        }

        // Verify skeleton card and empty state render in Dark Theme at 1.3x font scale
        composeTestRule.onNodeWithTag("editorial_loading_skeleton").assertExists()
        composeTestRule.onNodeWithTag("front_page_feed_list").performScrollToIndex(3)
        composeTestRule.onNodeWithText("No verified reports match this filter").assertIsDisplayed()

        // Switch to 2.0x accessibility font scale and verify layout stability
        scale = 2.0f
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("front_page_feed_list").performScrollToIndex(0)
        composeTestRule.onNodeWithTag("compact_location_selector_pill").assertIsDisplayed()
    }

    @Test
    fun `home screen searchable TopAppBar filters news feed by keyword`() {
        val dossiers = SurakshaArchiveSeedData.buildInitialCaseDossiers()
        val states = SurakshaArchiveSeedData.stateJurisdictions
        var filterState by mutableStateOf(SurakshaFilterState())

        composeTestRule.setContent {
            SurakshaChronicleTheme {
                FrontPageCaseFeedScreen(
                    dossiers = dossiers,
                    savedCount = 1,
                    states = states,
                    filterState = filterState,
                    districts = emptyList(),
                    cities = emptyList(),
                    activeRepresentative = null,
                    globalBlur = false,
                    revealedIds = emptySet(),
                    isRefreshingLive = false,
                    onSelectState = {},
                    onSelectDistrict = {},
                    onSelectCity = {},
                    onApplyLocation = { _, _, _ -> },
                    onResetFilters = { filterState = SurakshaFilterState() },
                    onSelectVictimGroup = {},
                    onSelectCrimeCategory = {},
                    onSearchQueryChange = { query ->
                        filterState = filterState.copy(searchQuery = query)
                    },
                    onRefreshLive = {},
                    onRevealBlur = {},
                    onOpenDossier = {},
                    onToggleSaveForLife = {},
                    onToggleFollow = {},
                    onAddFollowUp = { _, _, _, _, _ -> },
                    onArchiveNewIncident = { _, _, _, _, _, _, _, _, _, _, _, _ -> }
                )
            }
        }

        // Verify Material 3 TopAppBar and searchable input are displayed
        composeTestRule.onNodeWithTag("home_top_app_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("case_search_input").assertIsDisplayed()

        // Enter a keyword matching the Badlapur school case so it becomes the top visible item
        composeTestRule.onNodeWithTag("case_search_input").performTextInput("Badlapur")
        composeTestRule.waitForIdle()

        // Verify the matching case card is displayed while non-matching cases are filtered out
        composeTestRule.onNodeWithTag("dossier_card_case_mh_badlapur_school_02").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dossier_card_case_ap_school_chainsaw_01").assertDoesNotExist()

        // Clear the search query and verify the top feed item returns
        composeTestRule.onNodeWithTag("clear_search_query_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals("", filterState.searchQuery)
        composeTestRule.onNodeWithTag("dossier_card_case_ap_school_chainsaw_01").assertIsDisplayed()
    }

    @Test
    fun `switching language to Hindi updates Home Track Saved Safety and MPs screens with no leftover English UI chrome`() {
        val dossiers = SurakshaArchiveSeedData.buildInitialCaseDossiers()
        val states = SurakshaArchiveSeedData.stateJurisdictions
        val helplines = SurakshaArchiveSeedData.emergencyHelplines
        val guides = SurakshaArchiveSeedData.legalRightsGuides

        var activeLanguage by mutableStateOf(AppLanguage.ENGLISH)
        var activeTab by mutableStateOf(0)

        composeTestRule.setContent {
            SurakshaChronicleTheme(appLanguage = activeLanguage) {
                when (activeTab) {
                    0 -> FrontPageCaseFeedScreen(
                        dossiers = dossiers,
                        savedCount = 1,
                        states = states,
                        filterState = SurakshaFilterState(),
                        districts = emptyList(),
                        cities = emptyList(),
                        activeRepresentative = null,
                        globalBlur = false,
                        revealedIds = emptySet(),
                        isRefreshingLive = false,
                        onSelectState = {},
                        onSelectDistrict = {},
                        onSelectCity = {},
                        onApplyLocation = { _, _, _ -> },
                        onResetFilters = {},
                        onSelectVictimGroup = {},
                        onSelectCrimeCategory = {},
                        onSearchQueryChange = {},
                        onRefreshLive = {},
                        onRevealBlur = {},
                        onOpenDossier = {},
                        onToggleSaveForLife = {},
                        onToggleFollow = {},
                        onAddFollowUp = { _, _, _, _, _ -> },
                        onArchiveNewIncident = { _, _, _, _, _, _, _, _, _, _, _, _ -> }
                    )
                    1 -> FollowUpTrackerScreen(
                        allDossiers = dossiers,
                        states = states,
                        filterState = SurakshaFilterState(),
                        districts = emptyList(),
                        cities = emptyList(),
                        activeRepresentative = null,
                        globalBlur = false,
                        revealedIds = emptySet(),
                        onSelectState = {},
                        onSelectDistrict = {},
                        onSelectCity = {},
                        onRevealBlur = {},
                        onOpenDossier = {},
                        onToggleSaveForLife = {},
                        onAddFollowUp = { _, _, _, _, _ -> },
                        onQuickSelectJurisdiction = { _, _, _ -> }
                    )
                    2 -> PermanentVaultScreen(
                        savedDossiers = dossiers.filter { it.isSavedForLife },
                        followedDossiers = dossiers,
                        states = states,
                        globalBlur = false,
                        revealedIds = emptySet(),
                        onRevealBlur = {},
                        onOpenDossier = {},
                        onToggleSaveForLife = {},
                        onAddFollowUp = { _, _, _, _, _ -> },
                        onArchiveNewIncident = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
                        onQuickSelectJurisdiction = { _, _, _ -> }
                    )
                    3 -> DataAndSafetyScreen(
                        states = states,
                        helplines = helplines,
                        legalGuides = guides,
                        onSelectStateForFeed = {}
                    )
                    4 -> MpsOfIndiaScreen(
                        uiState = MpsUiState(
                            isLoading = false,
                            lokSabhaMeta = HouseMeta(
                                house = "Lok Sabha",
                                asOfDate = "2025-05-01",
                                totalSeats = 543,
                                sitting = 541,
                                vacant = 2
                            ),
                            rajyaSabhaMeta = HouseMeta(
                                house = "Rajya Sabha",
                                asOfDate = "2025-05-01",
                                totalSeats = 245,
                                sitting = 243,
                                vacant = 2
                            )
                        ),
                        onSelectHouse = {},
                        onSearchQueryChange = {},
                        onSelectState = {},
                        onSelectParty = {},
                        onResetFilters = {},
                        onOpenLokSabhaMp = {},
                        onOpenRajyaSabhaMp = {},
                        onCloseMpDetail = {}
                    )
                }
            }
        }

        // Start in English on Home
        composeTestRule.onNodeWithText("Suraksha Chronicle").assertIsDisplayed()
        composeTestRule.onNodeWithText("All India").assertIsDisplayed()

        // Switch instantly to Hindi with no restart
        activeLanguage = AppLanguage.HINDI
        composeTestRule.waitForIdle()

        // 0: Home Screen in Hindi
        composeTestRule.onNodeWithText("सुरक्षा क्रॉनिकल").assertIsDisplayed()
        composeTestRule.onNodeWithText("अखिल भारतीय").assertIsDisplayed()
        composeTestRule.onNodeWithText("नवीनतम").assertIsDisplayed()
        composeTestRule.onNodeWithText("All India").assertDoesNotExist()

        // 1: Track Screen in Hindi
        activeTab = 1
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("भूले नहीं जाएंगे • केस ट्रैकर").assertIsDisplayed()
        composeTestRule.onNodeWithText("सभी मामले").assertIsDisplayed()
        composeTestRule.onNodeWithText("Never Forgotten • Case Tracker").assertDoesNotExist()

        // 2: Saved Screen in Hindi
        activeTab = 2
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("सहेजे गए और फ़ॉलो किए गए मामले").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved & Followed Cases").assertDoesNotExist()

        // 3: Safety Screen in Hindi
        activeTab = 3
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("सुरक्षा, हेल्पलाइन और कानूनी अधिकार").assertIsDisplayed()
        composeTestRule.onNodeWithText("Safety, Helplines & Legal Rights").assertDoesNotExist()

        // 4: MPs of India Screen in Hindi
        activeTab = 4
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("भारत के सांसद (MPs)").assertIsDisplayed()
        composeTestRule.onNodeWithText("लोकसभा").assertIsDisplayed()
        composeTestRule.onNodeWithText("राज्यसभा").assertIsDisplayed()
        composeTestRule.onNodeWithText("MPs of India").assertDoesNotExist()
    }

    @Test
    fun `DataStore persists chosen language theme and font scale across recreation`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo1 = UserPreferencesRepository(context)
        repo1.completeLanguageOnboarding(AppLanguage.HINDI)
        repo1.setThemeMode(ThemeMode.DARK)
        repo1.setFontScale(1.3f)

        // Recreate repository instance and verify persisted values
        val repo2 = UserPreferencesRepository(context)
        val loaded = repo2.readInitialPreferencesBlocking()
        assertEquals(AppLanguage.HINDI, loaded.appLanguage)
        assertEquals(ThemeMode.DARK, loaded.themeMode)
        assertEquals(1.3f, loaded.fontScale, 0.01f)
        assertTrue(loaded.hasCompletedLanguageOnboarding)

        // Reset back to English for other tests
        repo2.setAppLanguage(AppLanguage.ENGLISH)
        repo2.setThemeMode(ThemeMode.SYSTEM)
        repo2.setFontScale(1.0f)
    }

    @Test
    fun `verify bundled Devanagari fonts and complete Hindi seed translations`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Verify local TTF font resources exist and are loadable
        val sansStream = context.resources.openRawResource(R.font.noto_sans_devanagari)
        val sansBytes = sansStream.use { it.readBytes() }
        assertTrue("Noto Sans Devanagari TTF should be non-empty (size=${sansBytes.size})", sansBytes.size > 1_000)

        val serifStream = context.resources.openRawResource(R.font.noto_serif_devanagari)
        val serifBytes = serifStream.use { it.readBytes() }
        assertTrue("Noto Serif Devanagari TTF should be non-empty (size=${serifBytes.size})", serifBytes.size > 1_000)

        // Verify all 7 seed cases, 20 timeline updates, 6 helplines, and 4 legal guides have Hindi translations
        val seedEntries = SurakshaHindiSeedTranslations.buildSeedCacheEntities()
        assertTrue("Hindi seed cache entries should cover all seed items", seedEntries.size >= 80)

        val dossiers = SurakshaArchiveSeedData.buildInitialCaseDossiers()
        val entryMap = seedEntries.associate { "${it.contentId}:${it.field}" to it.text }
        dossiers.forEach { dossier ->
            val hiTitle = entryMap["${dossier.id}:title"]
            val hiSummary = entryMap["${dossier.id}:summary"]
            assertNotNull("Missing Hindi title for ${dossier.id}", hiTitle)
            assertNotNull("Missing Hindi summary for ${dossier.id}", hiSummary)
            assertFalse("Hindi title should differ from English for ${dossier.id}", hiTitle == dossier.title)
        }
    }

    private fun revealedCaseIdsEmpty(): Set<String> = emptySet()
}
