package com.example

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ParliamentHouse
import com.example.data.repository.MpsRepository
import com.example.ui.screens.MpsOfIndiaScreen
import com.example.ui.theme.SurakshaChronicleTheme
import com.example.ui.viewmodel.MpsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MpsOfIndiaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `real lok_sabha_mps and rajya_sabha_mps JSON assets parse with exact counts and no duplicate constituencies`() =
        runBlocking {
            val repository = MpsRepository(context = context, ioDispatcher = Dispatchers.Unconfined)
            val (lokDataset, rajyaDataset) = repository.loadDatasets()

            // 1. Lok Sabha total = 543, sitting = 541, vacant = 2
            assertEquals(543, lokDataset.meta.totalSeats)
            assertEquals(541, lokDataset.meta.sitting)
            assertEquals(2, lokDataset.meta.vacant)
            assertEquals(543, lokDataset.mps.size)
            assertEquals(541, lokDataset.mps.count { it.status == "sitting" && !it.isVacant })
            assertEquals(2, lokDataset.mps.count { it.status == "vacant" && it.isVacant })

            // Verify the 2 vacant Lok Sabha records have null mpName, party, partyShort, gender and non-null notes
            val vacantRecords = lokDataset.mps.filter { it.isVacant }
            assertEquals(2, vacantRecords.size)
            vacantRecords.forEach { vacant ->
                assertNull(vacant.mpName)
                assertNull(vacant.party)
                assertNull(vacant.partyShort)
                assertNull(vacant.gender)
                assertNotNull(vacant.notes)
                assertTrue(vacant.notes!!.isNotBlank())
            }

            // 2. Rajya Sabha total MPs = 243, nominated = 12
            assertEquals(243, rajyaDataset.mps.size)
            assertEquals(243, rajyaDataset.meta.sitting)
            assertEquals(12, rajyaDataset.mps.count { it.nominated })
            assertTrue(
                rajyaDataset.mps
                    .filter { it.nominated }
                    .all { it.stateOrUtRepresented == "Nominated" }
            )

            // 3. No duplicate (state_or_ut, constituency_name) pairs in Lok Sabha
            val stateConstituencyPairs = lokDataset.mps.map {
                it.stateOrUt.trim().lowercase() to it.constituencyName.trim().lowercase()
            }
            val uniquePairs = stateConstituencyPairs.toSet()
            assertEquals(
                "Expected all (state_or_ut, constituency_name) pairs in Lok Sabha to be unique",
                lokDataset.mps.size,
                uniquePairs.size
            )
        }

    @Test
    fun `MpsViewModel sorts Lok Sabha by state and constituency_number and puts Nominated last in Rajya Sabha`() =
        runBlocking {
            val repository = MpsRepository(context = context, ioDispatcher = Dispatchers.Unconfined)
            val viewModel = MpsViewModel(repository = repository)

            val initialState = viewModel.uiState.value
            assertFalse(initialState.isLoading)
            assertEquals(543, initialState.filteredLokSabhaMps.size)

            // Verify Lok Sabha default sort is by state_or_ut, then constituency_number
            val firstMp = initialState.filteredLokSabhaMps.first()
            assertEquals("Andaman and Nicobar Islands", firstMp.stateOrUt)
            assertEquals(1, firstMp.constituencyNumber)

            // Verify Rajya Sabha grouping puts "Nominated" as the last section
            val rajyaGroupsKeys = initialState.groupedRajyaSabhaMps.keys.toList()
            assertTrue(rajyaGroupsKeys.isNotEmpty())
            assertEquals("Nominated", rajyaGroupsKeys.last())
            assertEquals(12, initialState.groupedRajyaSabhaMps["Nominated"]?.size)

            // Verify instant search for vacant seat "Basirhat"
            viewModel.updateSearchQuery("Basirhat")
            val searchResult = viewModel.uiState.value.filteredLokSabhaMps
            assertEquals(1, searchResult.size)
            assertEquals("Basirhat", searchResult.first().constituencyName)
            assertTrue(searchResult.first().isVacant)
        }

    @Test
    fun `MpsOfIndiaScreen displays house counts search filters and MP detail screen`() {
        val repository = MpsRepository(context = context, ioDispatcher = Dispatchers.Unconfined)
        val viewModel = MpsViewModel(repository = repository)

        composeTestRule.setContent {
            val state = viewModel.uiState.collectAsState().value
            SurakshaChronicleTheme {
                MpsOfIndiaScreen(
                    uiState = state,
                    onSelectHouse = viewModel::selectHouse,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onSelectState = viewModel::selectStateFilter,
                    onSelectParty = viewModel::selectPartyFilter,
                    onResetFilters = viewModel::resetFilters,
                    onOpenLokSabhaMp = viewModel::openLokSabhaMpDetail,
                    onOpenRajyaSabhaMp = viewModel::openRajyaSabhaMpDetail,
                    onCloseMpDetail = viewModel::closeMpDetail
                )
            }
        }

        // Verify entry cards and counts
        composeTestRule.onNodeWithTag("house_card_lok_sabha").assertIsDisplayed()
        composeTestRule.onNodeWithTag("house_card_rajya_sabha").assertIsDisplayed()
        composeTestRule.onNodeWithText("541 Sitting · 2 Vacant").assertIsDisplayed()
        composeTestRule.onNodeWithText("243 Sitting · 2 Vacant").assertIsDisplayed()

        // Search for Shillong (vacant seat) and open detail screen
        composeTestRule.onNodeWithTag("mps_search_input").performTextInput("Shillong")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("mps_list_column").performScrollToIndex(1)
        composeTestRule.onNodeWithTag("lok_sabha_row_287").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Detail screen should show Vacant and the notes as Reason for Vacancy
        composeTestRule.onNodeWithTag("mp_detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reason for Vacancy").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("mps_public_sources_footer").performScrollTo().assertIsDisplayed()

        // Navigate back to list
        composeTestRule.onNodeWithTag("mp_detail_back_button").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun `MpsOfIndiaScreen supports Rajya Sabha sticky headers Nominated group party filters and empty state reset`() {
        val repository = MpsRepository(context = context, ioDispatcher = Dispatchers.Unconfined)
        val viewModel = MpsViewModel(repository = repository)

        composeTestRule.setContent {
            val state = viewModel.uiState.collectAsState().value
            SurakshaChronicleTheme {
                MpsOfIndiaScreen(
                    uiState = state,
                    onSelectHouse = viewModel::selectHouse,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onSelectState = viewModel::selectStateFilter,
                    onSelectParty = viewModel::selectPartyFilter,
                    onResetFilters = viewModel::resetFilters,
                    onOpenLokSabhaMp = viewModel::openLokSabhaMpDetail,
                    onOpenRajyaSabhaMp = viewModel::openRajyaSabhaMpDetail,
                    onCloseMpDetail = viewModel::closeMpDetail
                )
            }
        }

        // Switch to Rajya Sabha tab
        composeTestRule.onNodeWithTag("house_card_rajya_sabha").performClick()
        composeTestRule.waitForIdle()
        assertEquals(ParliamentHouse.RAJYA_SABHA, viewModel.uiState.value.selectedHouse)

        // Filter by Nominated section and verify sticky header and 12 members
        viewModel.selectStateFilter("Nominated")
        composeTestRule.waitForIdle()
        assertEquals(12, viewModel.uiState.value.totalFilteredRajyaSabhaCount)

        composeTestRule.onNodeWithTag("mps_list_column").performScrollToIndex(1)
        composeTestRule.onNodeWithTag("rajya_sticky_header_Nominated").assertIsDisplayed()

        // Enter a non-existent query to trigger the empty state and test reset button
        viewModel.updateSearchQuery("NonExistentConstituency99999")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("mps_list_column").performScrollToIndex(1)
        composeTestRule.onNodeWithTag("mps_empty_state_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("mps_empty_reset_button").performClick()
        composeTestRule.waitForIdle()

        assertEquals("", viewModel.uiState.value.searchQuery)
        assertEquals(243, viewModel.uiState.value.totalFilteredRajyaSabhaCount)
    }
}
