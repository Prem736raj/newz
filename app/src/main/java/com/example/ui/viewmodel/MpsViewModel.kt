package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.HouseMeta
import com.example.data.model.LokSabhaMpRecord
import com.example.data.model.ParliamentHouse
import com.example.data.model.RajyaSabhaMpRecord
import com.example.data.model.SelectedMpDetail
import com.example.data.repository.MpsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MpsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedHouse: ParliamentHouse = ParliamentHouse.LOK_SABHA,
    val lokSabhaMeta: HouseMeta = HouseMeta(house = "Lok Sabha"),
    val rajyaSabhaMeta: HouseMeta = HouseMeta(house = "Rajya Sabha"),
    val searchQuery: String = "",
    val selectedState: String = ALL_STATES_OPTION,
    val selectedParty: String = ALL_PARTIES_OPTION,
    val availableStates: List<String> = listOf(ALL_STATES_OPTION),
    val topPartyOptions: List<String> = listOf(ALL_PARTIES_OPTION),
    val filteredLokSabhaMps: List<LokSabhaMpRecord> = emptyList(),
    val groupedRajyaSabhaMps: Map<String, List<RajyaSabhaMpRecord>> = emptyMap(),
    val totalFilteredRajyaSabhaCount: Int = 0,
    val selectedMpDetail: SelectedMpDetail? = null
) {
    companion object {
        const val ALL_STATES_OPTION = "All States & UTs"
        const val ALL_PARTIES_OPTION = "All"
        val DEFAULT_TOP_PARTIES = listOf("BJP", "INC", "SP", "AITC", "DMK", "TDP", "JD(U)", "SHS")
    }
}

class MpsViewModel(
    private val repository: MpsRepository
) : ViewModel() {

    private var allLokSabhaMps: List<LokSabhaMpRecord> = emptyList()
    private var allRajyaSabhaMps: List<RajyaSabhaMpRecord> = emptyList()
    private var lokMeta: HouseMeta = HouseMeta(house = "Lok Sabha")
    private var rajyaMeta: HouseMeta = HouseMeta(house = "Rajya Sabha")

    private val _uiState = MutableStateFlow(MpsUiState(isLoading = true))
    val uiState: StateFlow<MpsUiState> = _uiState.asStateFlow()

    init {
        loadMpsData()
    }

    fun loadMpsData(forceReload: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.loadDatasets(forceReload = forceReload)
            }.onSuccess { (lokDataset, rajyaDataset) ->
                lokMeta = lokDataset.meta
                rajyaMeta = rajyaDataset.meta
                // Sort Lok Sabha by state_or_ut, then constituency_number by default
                allLokSabhaMps = lokDataset.mps.sortedWith(
                    compareBy<LokSabhaMpRecord> { it.stateOrUt.lowercase() }
                        .thenBy { it.constituencyNumber }
                        .thenBy { it.constituencyName.lowercase() }
                )
                // Sort Rajya Sabha records inside each group by MP name
                allRajyaSabhaMps = rajyaDataset.mps.sortedWith(
                    compareBy<RajyaSabhaMpRecord> {
                        if (it.groupSectionName == "Nominated") 1 else 0
                    }
                        .thenBy { it.groupSectionName.lowercase() }
                        .thenBy { (it.mpName ?: "").lowercase() }
                )
                recomputeFilteredState(
                    house = _uiState.value.selectedHouse,
                    searchQuery = _uiState.value.searchQuery,
                    selectedState = _uiState.value.selectedState,
                    selectedParty = _uiState.value.selectedParty,
                    selectedDetail = _uiState.value.selectedMpDetail
                )
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Unable to read MP datasets from offline assets."
                    )
                }
            }
        }
    }

    fun selectHouse(house: ParliamentHouse) {
        val current = _uiState.value
        if (current.selectedHouse == house) return
        // Reset state/party filter if the selected state is not present in the switched house
        val validStates = computeAvailableStates(house)
        val nextState = if (current.selectedState in validStates) {
            current.selectedState
        } else {
            MpsUiState.ALL_STATES_OPTION
        }
        val validParties = computeTopPartyOptions(house)
        val nextParty = if (current.selectedParty in validParties) {
            current.selectedParty
        } else {
            MpsUiState.ALL_PARTIES_OPTION
        }
        recomputeFilteredState(
            house = house,
            searchQuery = current.searchQuery,
            selectedState = nextState,
            selectedParty = nextParty,
            selectedDetail = current.selectedMpDetail
        )
    }

    fun updateSearchQuery(query: String) {
        val current = _uiState.value
        recomputeFilteredState(
            house = current.selectedHouse,
            searchQuery = query,
            selectedState = current.selectedState,
            selectedParty = current.selectedParty,
            selectedDetail = current.selectedMpDetail
        )
    }

    fun selectStateFilter(stateOrUt: String) {
        val current = _uiState.value
        recomputeFilteredState(
            house = current.selectedHouse,
            searchQuery = current.searchQuery,
            selectedState = stateOrUt,
            selectedParty = current.selectedParty,
            selectedDetail = current.selectedMpDetail
        )
    }

    fun selectPartyFilter(partyShort: String) {
        val current = _uiState.value
        recomputeFilteredState(
            house = current.selectedHouse,
            searchQuery = current.searchQuery,
            selectedState = current.selectedState,
            selectedParty = partyShort,
            selectedDetail = current.selectedMpDetail
        )
    }

    fun resetFilters() {
        val current = _uiState.value
        recomputeFilteredState(
            house = current.selectedHouse,
            searchQuery = "",
            selectedState = MpsUiState.ALL_STATES_OPTION,
            selectedParty = MpsUiState.ALL_PARTIES_OPTION,
            selectedDetail = current.selectedMpDetail
        )
    }

    fun openLokSabhaMpDetail(record: LokSabhaMpRecord) {
        _uiState.update {
            it.copy(selectedMpDetail = SelectedMpDetail.LokSabha(record = record, meta = lokMeta))
        }
    }

    fun openRajyaSabhaMpDetail(record: RajyaSabhaMpRecord) {
        _uiState.update {
            it.copy(selectedMpDetail = SelectedMpDetail.RajyaSabha(record = record, meta = rajyaMeta))
        }
    }

    fun closeMpDetail() {
        _uiState.update { it.copy(selectedMpDetail = null) }
    }

    private fun computeAvailableStates(house: ParliamentHouse): List<String> {
        return when (house) {
            ParliamentHouse.LOK_SABHA -> {
                val states = allLokSabhaMps
                    .map { it.stateOrUt.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()
                listOf(MpsUiState.ALL_STATES_OPTION) + states
            }
            ParliamentHouse.RAJYA_SABHA -> {
                val nonNominated = allRajyaSabhaMps
                    .map { it.groupSectionName }
                    .filter { it.isNotEmpty() && !it.equals("Nominated", ignoreCase = true) }
                    .distinct()
                    .sorted()
                val hasNominated = allRajyaSabhaMps.any { it.groupSectionName.equals("Nominated", ignoreCase = true) }
                buildList {
                    add(MpsUiState.ALL_STATES_OPTION)
                    addAll(nonNominated)
                    if (hasNominated) add("Nominated")
                }
            }
        }
    }

    private fun computeTopPartyOptions(house: ParliamentHouse): List<String> {
        val partyCounts: Map<String, Int> = when (house) {
            ParliamentHouse.LOK_SABHA -> {
                allLokSabhaMps
                    .mapNotNull { it.partyShort?.trim()?.takeIf { s -> s.isNotEmpty() } }
                    .groupingBy { it }
                    .eachCount()
            }
            ParliamentHouse.RAJYA_SABHA -> {
                allRajyaSabhaMps
                    .mapNotNull { it.partyShort?.trim()?.takeIf { s -> s.isNotEmpty() } }
                    .groupingBy { it }
                    .eachCount()
            }
        }

        // Include the 8 highlighted parties plus any additional top parties sorted by seat count
        val orderedParties = (
            MpsUiState.DEFAULT_TOP_PARTIES.filter { partyCounts.containsKey(it) } +
                partyCounts.entries
                    .sortedByDescending { it.value }
                    .map { it.key }
                    .take(10)
            ).distinct()

        return listOf(MpsUiState.ALL_PARTIES_OPTION) + orderedParties
    }

    private fun recomputeFilteredState(
        house: ParliamentHouse,
        searchQuery: String,
        selectedState: String,
        selectedParty: String,
        selectedDetail: SelectedMpDetail?
    ) {
        val normalizedQuery = searchQuery.trim().lowercase()
        val availableStates = computeAvailableStates(house)
        val topParties = computeTopPartyOptions(house)

        val filteredLok = allLokSabhaMps.filter { mp ->
            val matchesState = selectedState == MpsUiState.ALL_STATES_OPTION ||
                mp.stateOrUt.equals(selectedState, ignoreCase = true)

            val matchesParty = selectedParty == MpsUiState.ALL_PARTIES_OPTION ||
                mp.partyShort.equals(selectedParty, ignoreCase = true)

            val matchesSearch = if (normalizedQuery.isEmpty()) {
                true
            } else {
                val nameMatch = mp.mpName?.lowercase()?.contains(normalizedQuery) == true
                val vacantMatch = mp.isVacant && "vacant".contains(normalizedQuery)
                val constituencyMatch = mp.constituencyName.lowercase().contains(normalizedQuery)
                val stateMatch = mp.stateOrUt.lowercase().contains(normalizedQuery)
                val partyFullMatch = mp.party?.lowercase()?.contains(normalizedQuery) == true
                val partyShortMatch = mp.partyShort?.lowercase()?.contains(normalizedQuery) == true
                nameMatch || vacantMatch || constituencyMatch || stateMatch || partyFullMatch || partyShortMatch
            }

            matchesState && matchesParty && matchesSearch
        }

        val filteredRajya = allRajyaSabhaMps.filter { mp ->
            val section = mp.groupSectionName
            val matchesState = selectedState == MpsUiState.ALL_STATES_OPTION ||
                section.equals(selectedState, ignoreCase = true) ||
                mp.stateOrUtRepresented.equals(selectedState, ignoreCase = true)

            val matchesParty = selectedParty == MpsUiState.ALL_PARTIES_OPTION ||
                mp.partyShort.equals(selectedParty, ignoreCase = true)

            val matchesSearch = if (normalizedQuery.isEmpty()) {
                true
            } else {
                val nameMatch = mp.mpName?.lowercase()?.contains(normalizedQuery) == true
                val stateMatch = mp.stateOrUtRepresented.lowercase().contains(normalizedQuery) ||
                    section.lowercase().contains(normalizedQuery)
                val partyFullMatch = mp.party?.lowercase()?.contains(normalizedQuery) == true
                val partyShortMatch = mp.partyShort?.lowercase()?.contains(normalizedQuery) == true
                nameMatch || stateMatch || partyFullMatch || partyShortMatch
            }

            matchesState && matchesParty && matchesSearch
        }

        // Group Rajya Sabha by state alphabetically, with "Nominated" always as the last group
        val nonNominatedGroups = filteredRajya
            .filter { !it.groupSectionName.equals("Nominated", ignoreCase = true) }
            .groupBy { it.groupSectionName }
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)

        val nominatedMembers = filteredRajya
            .filter { it.groupSectionName.equals("Nominated", ignoreCase = true) }

        val orderedRajyaGroups = LinkedHashMap<String, List<RajyaSabhaMpRecord>>().apply {
            for ((stateKey, members) in nonNominatedGroups) {
                put(stateKey, members)
            }
            if (nominatedMembers.isNotEmpty()) {
                put("Nominated", nominatedMembers)
            }
        }

        _uiState.value = MpsUiState(
            isLoading = false,
            errorMessage = null,
            selectedHouse = house,
            lokSabhaMeta = lokMeta,
            rajyaSabhaMeta = rajyaMeta,
            searchQuery = searchQuery,
            selectedState = selectedState,
            selectedParty = selectedParty,
            availableStates = availableStates,
            topPartyOptions = topParties,
            filteredLokSabhaMps = filteredLok,
            groupedRajyaSabhaMps = orderedRajyaGroups,
            totalFilteredRajyaSabhaCount = filteredRajya.size,
            selectedMpDetail = selectedDetail
        )
    }
}
