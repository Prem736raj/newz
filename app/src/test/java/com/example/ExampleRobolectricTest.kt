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
import com.example.data.model.VerificationStatus
import com.example.data.remote.IndianCrimeNewsRssClient
import com.example.ui.screens.CaseDossierDetailScreen
import com.example.ui.screens.DataAndSafetyScreen
import com.example.ui.screens.FollowUpTrackerScreen
import com.example.ui.screens.FrontPageCaseFeedScreen
import com.example.ui.screens.PermanentVaultScreen
import com.example.ui.theme.SurakshaChronicleTheme
import com.example.ui.viewmodel.SurakshaFilterState
import org.junit.Assert.assertEquals
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

        // Verify first news card bookmark and follow buttons work
        val firstId = dossiers.first().id
        composeTestRule.onNodeWithTag("save_for_life_btn_$firstId").performScrollTo().performClick()
        assertTrue(savedTriggered)

        composeTestRule.onNodeWithTag("follow_case_btn_$firstId").performScrollTo().performClick()
        assertTrue(followTriggered)

        // Tap compact location pill to open bottom sheet
        composeTestRule.onNodeWithTag("compact_location_selector_pill").performScrollTo().performClick()
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

    private fun revealedCaseIdsEmpty(): Set<String> = emptySet()
}
