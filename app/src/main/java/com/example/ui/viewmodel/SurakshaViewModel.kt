package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SavedCaseOfflineDao
import com.example.data.local.SurakshaArchiveSeedData
import com.example.data.model.AppLanguage
import com.example.data.model.CaseDossier
import com.example.data.model.CaseFollowUp
import com.example.data.model.CaseStage
import com.example.data.model.CityJurisdiction
import com.example.data.model.DistrictJurisdiction
import com.example.data.model.SavedCase
import com.example.data.model.SavedCaseOfflineEntity
import com.example.data.model.SensitivityLevel
import com.example.data.model.StateJurisdiction
import com.example.data.model.ThemeMode
import com.example.data.model.TimelineUpdateItem
import com.example.data.model.VerificationStatus
import com.example.data.remote.IndianCrimeNewsRssClient
import com.example.data.repository.SurakshaFirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SurakshaFilterState(
    val selectedState: String = "All India",
    val selectedDistrict: String = "All Districts",
    val selectedCity: String = "All Cities",
    val selectedVictimGroup: String = "All",
    val selectedCrimeCategory: String = "All",
    val searchQuery: String = "",
    val onlyLongTermStalled: Boolean = false
)

class SurakshaViewModel(
    private val firestoreRepository: SurakshaFirestoreRepository,
    private val offlineDao: SavedCaseOfflineDao,
    private val rssClient: IndianCrimeNewsRssClient,
    private val currentUserId: String
) : ViewModel() {

    val allStates: List<StateJurisdiction> = SurakshaArchiveSeedData.stateJurisdictions
    val emergencyHelplines = SurakshaArchiveSeedData.emergencyHelplines
    val legalRightsGuides = SurakshaArchiveSeedData.legalRightsGuides

    private val _filterState = MutableStateFlow(SurakshaFilterState())
    val filterState: StateFlow<SurakshaFilterState> = _filterState.asStateFlow()

    private val _globalSensitiveBlur = MutableStateFlow(false)
    val globalSensitiveBlur: StateFlow<Boolean> = _globalSensitiveBlur.asStateFlow()

    private val _revealedCaseIds = MutableStateFlow<Set<String>>(emptySet())
    val revealedCaseIds: StateFlow<Set<String>> = _revealedCaseIds.asStateFlow()

    private val _followedCaseIds = MutableStateFlow(
        setOf("case_ap_elamanchili_chainsaw_2026", "case_wb_kolkata_medical_2024", "case_mh_badlapur_school_2024")
    )
    val followedCaseIds: StateFlow<Set<String>> = _followedCaseIds.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _visibleFeedLimit = MutableStateFlow(8)
    val visibleFeedLimit: StateFlow<Int> = _visibleFeedLimit.asStateFlow()

    private val _liveRssDossiers = MutableStateFlow<List<CaseDossier>>(emptyList())
    private val _isRefreshingLive = MutableStateFlow(false)
    val isRefreshingLive: StateFlow<Boolean> = _isRefreshingLive.asStateFlow()

    private val _feedSyncNote = MutableStateFlow<String?>(null)
    val feedSyncNote: StateFlow<String?> = _feedSyncNote.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _selectedDossierId = MutableStateFlow<String?>(null)
    val selectedDossierId: StateFlow<String?> = _selectedDossierId.asStateFlow()

    private val _correctionLogs = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val correctionLogs: StateFlow<Map<String, List<String>>> = _correctionLogs.asStateFlow()

    private val cloudSavedCasesFlow = firestoreRepository.observeSavedCases(currentUserId)
        .catch { e ->
            Log.w("SurakshaVM", "Offline fallback for cloud saved_cases", e)
            emit(emptyList())
        }

    private val cloudFollowUpsFlow = firestoreRepository.observeCaseFollowUps(currentUserId)
        .catch { e ->
            Log.w("SurakshaVM", "Offline fallback for cloud case_updates", e)
            emit(emptyList())
        }

    private val offlineSavedCasesFlow = offlineDao.observeAllSavedCases()
        .catch { e ->
            Log.w("SurakshaVM", "Error observing Room saved_cases_offline", e)
            emit(emptyList())
        }

    val allDossiers: StateFlow<List<CaseDossier>> = combine(
        cloudSavedCasesFlow,
        cloudFollowUpsFlow,
        offlineSavedCasesFlow,
        _liveRssDossiers,
        _followedCaseIds
    ) { cloudSaved, cloudUpdates, offlineSaved, liveRss, followedIds ->
        mergeAllDossiers(cloudSaved, cloudUpdates, offlineSaved, liveRss, followedIds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = SurakshaArchiveSeedData.buildInitialCaseDossiers().map {
            it.copy(isFollowed = _followedCaseIds.value.contains(it.id))
        }
    )

    val filteredDossiers: StateFlow<List<CaseDossier>> = combine(
        allDossiers,
        _filterState
    ) { dossiers, filter ->
        dossiers.filter { dossier ->
            val matchesState = filter.selectedState == "All India" ||
                dossier.stateName.equals(filter.selectedState, ignoreCase = true)
            val matchesDistrict = filter.selectedDistrict == "All Districts" ||
                dossier.districtName.equals(filter.selectedDistrict, ignoreCase = true)
            val matchesCity = filter.selectedCity == "All Cities" ||
                dossier.cityName.equals(filter.selectedCity, ignoreCase = true)
            val matchesVictim = filter.selectedVictimGroup == "All" ||
                dossier.victimCategory.contains(filter.selectedVictimGroup, ignoreCase = true)
            val matchesCategory = filter.selectedCrimeCategory == "All" ||
                dossier.crimeCategory.contains(filter.selectedCrimeCategory, ignoreCase = true)
            val matchesStalled = !filter.onlyLongTermStalled || dossier.daysSinceIncident >= 30
            val matchesQuery = filter.searchQuery.isBlank() ||
                dossier.title.contains(filter.searchQuery, ignoreCase = true) ||
                dossier.summary.contains(filter.searchQuery, ignoreCase = true) ||
                dossier.stateName.contains(filter.searchQuery, ignoreCase = true) ||
                dossier.districtName.contains(filter.searchQuery, ignoreCase = true) ||
                dossier.cityName.contains(filter.searchQuery, ignoreCase = true) ||
                dossier.mlaName.contains(filter.searchQuery, ignoreCase = true) ||
                dossier.mpName.contains(filter.searchQuery, ignoreCase = true) ||
                dossier.sourceName.contains(filter.searchQuery, ignoreCase = true)

            matchesState && matchesDistrict && matchesCity && matchesVictim && matchesCategory && matchesStalled && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = SurakshaArchiveSeedData.buildInitialCaseDossiers()
    )

    val savedForLifeDossiers: StateFlow<List<CaseDossier>> = combine(
        allDossiers,
        offlineSavedCasesFlow
    ) { dossiers, _ ->
        dossiers.filter { it.isSavedForLife }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val followedDossiers: StateFlow<List<CaseDossier>> = combine(
        allDossiers,
        _followedCaseIds
    ) { dossiers, followedIds ->
        dossiers.filter { followedIds.contains(it.id) || it.isFollowed }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    init {
        seedFlagshipAndhraPradeshCaseIfEmpty()
        refreshLiveIndianNews()
    }

    private fun seedFlagshipAndhraPradeshCaseIfEmpty() {
        viewModelScope.launch {
            val flagship = SurakshaArchiveSeedData.buildInitialCaseDossiers().first()
            val existingOffline = offlineDao.getSavedCaseById(flagship.id)
            if (existingOffline == null) {
                offlineDao.upsertSavedCase(
                    flagship.toOfflineEntity(
                        "Pinned flagship dossier: Andhra Pradesh Class 10 classroom security breach tracked for court and school-safety accountability."
                    )
                )
            }
        }
    }

    fun selectState(stateName: String) {
        _filterState.value = _filterState.value.copy(
            selectedState = stateName,
            selectedDistrict = "All Districts",
            selectedCity = "All Cities"
        )
        refreshLiveIndianNews(stateName)
    }

    fun selectDistrict(districtName: String) {
        _filterState.value = _filterState.value.copy(
            selectedDistrict = districtName,
            selectedCity = "All Cities"
        )
    }

    fun selectCity(cityName: String) {
        _filterState.value = _filterState.value.copy(
            selectedCity = cityName
        )
    }

    fun applyLocationSelection(stateName: String, districtName: String, cityName: String) {
        val prevState = _filterState.value.selectedState
        _filterState.value = _filterState.value.copy(
            selectedState = stateName,
            selectedDistrict = if (stateName == "All India") "All Districts" else districtName,
            selectedCity = if (stateName == "All India" || districtName == "All Districts") "All Cities" else cityName
        )
        if (prevState != stateName) {
            refreshLiveIndianNews(stateName)
        }
    }

    fun resetAllFilters() {
        _filterState.value = SurakshaFilterState()
        refreshLiveIndianNews("All India")
    }

    fun selectVictimGroup(group: String) {
        _filterState.value = _filterState.value.copy(selectedVictimGroup = group)
    }

    fun selectCrimeCategory(category: String) {
        _filterState.value = _filterState.value.copy(selectedCrimeCategory = category)
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun toggleOnlyLongTermStalled() {
        _filterState.value = _filterState.value.copy(
            onlyLongTermStalled = !_filterState.value.onlyLongTermStalled
        )
    }

    fun toggleGlobalSensitiveBlur() {
        _globalSensitiveBlur.value = !_globalSensitiveBlur.value
    }

    fun revealCaseContent(caseId: String) {
        _revealedCaseIds.value = _revealedCaseIds.value + caseId
    }

    fun toggleFollowCase(dossier: CaseDossier) {
        val current = _followedCaseIds.value
        if (current.contains(dossier.id)) {
            _followedCaseIds.value = current - dossier.id
            _statusBannerMessage.value = "Unfollowed case updates for '${dossier.title.take(42)}...'"
        } else {
            _followedCaseIds.value = current + dossier.id
            _statusBannerMessage.value = "Following case. You will be notified of verified developments."
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale.coerceIn(0.85f, 2.0f)
    }

    fun toggleNotificationsEnabled() {
        _notificationsEnabled.value = !_notificationsEnabled.value
        _statusBannerMessage.value = if (_notificationsEnabled.value) {
            "Verified case update alerts enabled."
        } else {
            "Verified case update alerts paused."
        }
    }

    fun loadMoreFeedItems() {
        _visibleFeedLimit.value += 8
    }

    fun openDossierDetail(caseId: String?) {
        _selectedDossierId.value = caseId
    }

    fun clearBannerMessage() {
        _statusBannerMessage.value = null
    }

    fun submitCorrectionOrPrivacyReport(caseId: String, issueType: String, details: String) {
        if (details.isBlank()) return
        val existing = _correctionLogs.value[caseId] ?: emptyList()
        val entry = "Pending Editorial Review ($issueType): ${details.trim()}"
        _correctionLogs.value = _correctionLogs.value + (caseId to (existing + entry))
        _statusBannerMessage.value =
            "Editorial & Privacy Review logged ($issueType). Reference ID #${caseId.takeLast(5).uppercase()}."
    }

    fun getAvailableDistricts(stateName: String): List<DistrictJurisdiction> {
        if (stateName == "All India") return emptyList()
        return allStates.find { it.stateName.equals(stateName, ignoreCase = true) }?.districts ?: emptyList()
    }

    fun getAvailableCities(stateName: String, districtName: String): List<CityJurisdiction> {
        if (stateName == "All India" || districtName == "All Districts") return emptyList()
        return getAvailableDistricts(stateName)
            .find { it.districtName.equals(districtName, ignoreCase = true) }
            ?.cities ?: emptyList()
    }

    fun getActiveConstituencyRepresentative(
        stateName: String,
        districtName: String,
        cityName: String
    ): CityJurisdiction? {
        if (stateName == "All India") return null
        val stateObj = allStates.find { it.stateName.equals(stateName, ignoreCase = true) } ?: return null
        val districtObj = if (districtName == "All Districts") {
            stateObj.districts.firstOrNull()
        } else {
            stateObj.districts.find { it.districtName.equals(districtName, ignoreCase = true) }
        } ?: return null

        return if (cityName == "All Cities") {
            districtObj.cities.firstOrNull()
        } else {
            districtObj.cities.find { it.cityName.equals(cityName, ignoreCase = true) }
                ?: districtObj.cities.firstOrNull()
        }
    }

    fun refreshLiveIndianNews(stateOverride: String? = null) {
        val targetState = stateOverride ?: _filterState.value.selectedState
        viewModelScope.launch {
            _isRefreshingLive.value = true
            _feedSyncNote.value = null
            try {
                val live = rssClient.fetchLiveIndianWomenSafetyDispatches(targetState)
                if (live.isNotEmpty()) {
                    _liveRssDossiers.value = live
                } else {
                    _feedSyncNote.value = "Showing verified archival & cached reports (live syndication feed unreachable or rate-limited)."
                }
            } catch (e: Exception) {
                _feedSyncNote.value = "Offline mode: displaying verified local archive."
            } finally {
                _isRefreshingLive.value = false
            }
        }
    }

    fun toggleSaveForLife(dossier: CaseDossier, personalNotes: String = "") {
        viewModelScope.launch {
            if (dossier.isSavedForLife) {
                offlineDao.deleteSavedCaseById(dossier.id)
                firestoreRepository.removeSavedCase(dossier.id)
                _statusBannerMessage.value = "Removed from Saved Articles & Offline Cache."
            } else {
                val note = personalNotes.ifBlank {
                    dossier.personalNotes.ifBlank {
                        "Saved for offline access and long-term case tracking."
                    }
                }
                offlineDao.upsertSavedCase(dossier.toOfflineEntity(note))
                val cloudModel = SavedCase(
                    id = dossier.id,
                    userId = currentUserId,
                    title = dossier.title,
                    summary = dossier.summary,
                    fullReport = dossier.fullReport,
                    stateName = dossier.stateName,
                    districtName = dossier.districtName,
                    cityName = dossier.cityName,
                    mlaName = "${dossier.mlaName} (${dossier.mlaConstituency})",
                    mpName = "${dossier.mpName} (${dossier.mpConstituency})",
                    victimCategory = dossier.victimCategory,
                    crimeCategory = dossier.crimeCategory,
                    caseStage = dossier.caseStage.code,
                    incidentDateLabel = dossier.incidentDateLabel,
                    personalNotes = note
                )
                val res = firestoreRepository.saveCaseForLife(dossier.id, cloudModel)
                _statusBannerMessage.value = if (res.isSuccess) {
                    "Saved for offline reading & synced to your account."
                } else {
                    "Saved to device for offline reading (cloud sync pending)."
                }
            }
        }
    }

    fun updatePersonalNote(dossier: CaseDossier, newNote: String) {
        viewModelScope.launch {
            val cleanNote = newNote.trim()
            offlineDao.upsertSavedCase(dossier.toOfflineEntity(cleanNote))
            val cloudModel = SavedCase(
                id = dossier.id,
                userId = currentUserId,
                title = dossier.title,
                summary = dossier.summary,
                fullReport = dossier.fullReport,
                stateName = dossier.stateName,
                districtName = dossier.districtName,
                cityName = dossier.cityName,
                mlaName = "${dossier.mlaName} (${dossier.mlaConstituency})",
                mpName = "${dossier.mpName} (${dossier.mpConstituency})",
                victimCategory = dossier.victimCategory,
                crimeCategory = dossier.crimeCategory,
                caseStage = dossier.caseStage.code,
                incidentDateLabel = dossier.incidentDateLabel,
                personalNotes = cleanNote
            )
            firestoreRepository.saveCaseForLife(dossier.id, cloudModel)
            _statusBannerMessage.value = "Private note saved."
        }
    }

    fun addFollowUpMilestone(
        dossier: CaseDossier,
        updateDateLabel: String,
        newStage: CaseStage,
        headline: String,
        details: String
    ) {
        if (headline.isBlank() || details.isBlank()) return
        viewModelScope.launch {
            val updateId = "upd_${System.currentTimeMillis()}"
            val result = firestoreRepository.addCaseFollowUp(
                updateId = updateId,
                caseId = dossier.id,
                caseTitle = dossier.title,
                updateDateLabel = updateDateLabel.ifBlank { "Day ${dossier.daysSinceIncident} Update" },
                stageBadge = newStage.code,
                headline = headline.trim(),
                details = details.trim()
            )
            val updatedDossier = dossier.copy(caseStage = newStage)
            offlineDao.upsertSavedCase(
                updatedDossier.toOfflineEntity(
                    dossier.personalNotes.ifBlank { "Latest logged update: $headline" }
                )
            )
            firestoreRepository.saveCaseForLife(
                dossier.id,
                SavedCase(
                    id = dossier.id,
                    userId = currentUserId,
                    title = dossier.title,
                    summary = dossier.summary,
                    fullReport = dossier.fullReport,
                    stateName = dossier.stateName,
                    districtName = dossier.districtName,
                    cityName = dossier.cityName,
                    mlaName = "${dossier.mlaName} (${dossier.mlaConstituency})",
                    mpName = "${dossier.mpName} (${dossier.mpConstituency})",
                    victimCategory = dossier.victimCategory,
                    crimeCategory = dossier.crimeCategory,
                    caseStage = newStage.code,
                    incidentDateLabel = dossier.incidentDateLabel,
                    personalNotes = "Latest logged update: $headline"
                )
            )
            _followedCaseIds.value = _followedCaseIds.value + dossier.id
            _statusBannerMessage.value = if (result.isSuccess) {
                "Timeline update recorded (marked 'Submitted • Awaiting Verification' & synced)."
            } else {
                "Timeline update saved to local device."
            }
        }
    }

    fun archiveNewIncidentForLife(
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
    ) {
        if (title.isBlank() || summary.isBlank()) return
        viewModelScope.launch {
            val newCaseId = "case_user_${System.currentTimeMillis()}"
            val rep = getActiveConstituencyRepresentative(stateName, districtName, cityName)
            val resolvedMla = mlaName.ifBlank { rep?.mlaName ?: "Not verified" }
            val resolvedMp = mpName.ifBlank { rep?.mpName ?: "Not verified" }

            val newDossier = CaseDossier(
                id = newCaseId,
                title = title.trim(),
                summary = summary.trim(),
                fullReport = fullReport.ifBlank { summary }.trim(),
                stateName = stateName.ifBlank { "Andhra Pradesh" },
                districtName = districtName.ifBlank { "Anakapalli" },
                cityName = cityName.ifBlank { "Elamanchili" },
                mlaName = resolvedMla,
                mlaConstituency = rep?.mlaConstituency ?: "Not verified",
                mlaParty = rep?.mlaParty ?: "Not verified",
                mpName = resolvedMp,
                mpConstituency = rep?.mpConstituency ?: "Not verified",
                mpParty = rep?.mpParty ?: "Not verified",
                policeJurisdiction = rep?.policeRange ?: "$districtName Police Jurisdiction",
                victimCategory = victimCategory,
                crimeCategory = crimeCategory,
                caseStage = caseStage,
                incidentDateLabel = "Submitted Today • Awaiting Editorial Verification",
                incidentEpochMillis = System.currentTimeMillis(),
                sensitivityLevel = SensitivityLevel.SENSITIVE,
                triggerWarning = "Content Advisory: User-submitted case dossier pending verification.",
                relevantLaws = listOf("BNSS Section 173 (Zero FIR)", "POCSO Section 23 (Minor Privacy Protection)"),
                sourceName = "User Archived Dossier (Private / Pending Verification)",
                isSavedForLife = true,
                isFollowed = true,
                personalNotes = personalNotes,
                verificationStatus = VerificationStatus.AWAITING_VERIFICATION
            )

            offlineDao.upsertSavedCase(newDossier.toOfflineEntity(personalNotes))
            _followedCaseIds.value = _followedCaseIds.value + newCaseId
            val cloudResult = firestoreRepository.saveCaseForLife(
                newCaseId,
                SavedCase(
                    id = newCaseId,
                    userId = currentUserId,
                    title = newDossier.title,
                    summary = newDossier.summary,
                    fullReport = newDossier.fullReport,
                    stateName = newDossier.stateName,
                    districtName = newDossier.districtName,
                    cityName = newDossier.cityName,
                    mlaName = resolvedMla,
                    mpName = resolvedMp,
                    victimCategory = victimCategory,
                    crimeCategory = crimeCategory,
                    caseStage = caseStage.code,
                    incidentDateLabel = newDossier.incidentDateLabel,
                    personalNotes = personalNotes.ifBlank { "Archived in private vault." }
                )
            )
            _statusBannerMessage.value = if (cloudResult.isSuccess) {
                "Case archived in your Saved Vault (marked 'Awaiting Verification')."
            } else {
                "Case archived locally in your Saved Vault."
            }
        }
    }

    private fun mergeAllDossiers(
        cloudSaved: List<SavedCase>,
        cloudUpdates: List<CaseFollowUp>,
        offlineSaved: List<SavedCaseOfflineEntity>,
        liveRss: List<CaseDossier>,
        followedIds: Set<String>
    ): List<CaseDossier> {
        val seedList = SurakshaArchiveSeedData.buildInitialCaseDossiers()
        val savedIds = (cloudSaved.map { it.id } + offlineSaved.map { it.caseId }).toSet()
        val updatesByCaseId = cloudUpdates.groupBy { it.caseId }

        val mergedMap = linkedMapOf<String, CaseDossier>()

        // 1. Seed verified flagship & long-term dossiers
        for (seed in seedList) {
            val extraUpdates = updatesByCaseId[seed.id].orEmpty().map { upd ->
                TimelineUpdateItem(
                    id = upd.id,
                    caseId = seed.id,
                    dateLabel = upd.updateDateLabel,
                    stageBadge = CaseStage.fromCode(upd.stageBadge).label,
                    headline = upd.headline,
                    details = upd.details,
                    sourceName = "User Logged Note (Awaiting Verification)",
                    isUserAdded = true,
                    verificationStatus = VerificationStatus.AWAITING_VERIFICATION,
                    lastCheckedDate = "Logged Today"
                )
            }
            val offlineMatch = offlineSaved.find { it.caseId == seed.id }
            val cloudMatch = cloudSaved.find { it.id == seed.id }
            val latestStage = cloudMatch?.let { CaseStage.fromCode(it.caseStage) }
                ?: offlineMatch?.let { CaseStage.fromCode(it.caseStageCode) }
                ?: seed.caseStage

            mergedMap[seed.id] = seed.copy(
                caseStage = latestStage,
                isSavedForLife = savedIds.contains(seed.id),
                isFollowed = followedIds.contains(seed.id),
                personalNotes = cloudMatch?.personalNotes ?: offlineMatch?.personalNotes ?: seed.personalNotes,
                timelineUpdates = seed.timelineUpdates + extraUpdates
            )
        }

        // 2. User-created / offline saved cases not in seed
        for (offline in offlineSaved) {
            if (!mergedMap.containsKey(offline.caseId)) {
                val extraUpdates = updatesByCaseId[offline.caseId].orEmpty().map { upd ->
                    TimelineUpdateItem(
                        id = upd.id,
                        caseId = offline.caseId,
                        dateLabel = upd.updateDateLabel,
                        stageBadge = CaseStage.fromCode(upd.stageBadge).label,
                        headline = upd.headline,
                        details = upd.details,
                        sourceName = "User Logged Note",
                        isUserAdded = true,
                        verificationStatus = VerificationStatus.AWAITING_VERIFICATION,
                        lastCheckedDate = "Logged Today"
                    )
                }
                mergedMap[offline.caseId] = CaseDossier(
                    id = offline.caseId,
                    title = offline.title,
                    summary = offline.summary,
                    fullReport = offline.fullReport,
                    stateName = offline.stateName,
                    districtName = offline.districtName,
                    cityName = offline.cityName,
                    mlaName = offline.mlaName,
                    mlaConstituency = offline.mlaConstituency,
                    mlaParty = offline.mlaParty,
                    mpName = offline.mpName,
                    mpConstituency = offline.mpConstituency,
                    mpParty = offline.mpParty,
                    policeJurisdiction = offline.policeJurisdiction,
                    victimCategory = offline.victimCategory,
                    crimeCategory = offline.crimeCategory,
                    caseStage = CaseStage.fromCode(offline.caseStageCode),
                    incidentDateLabel = offline.incidentDateLabel,
                    incidentEpochMillis = offline.incidentEpochMillis,
                    sensitivityLevel = SensitivityLevel.entries.find { it.name == offline.sensitivityCode }
                        ?: SensitivityLevel.SENSITIVE,
                    triggerWarning = offline.triggerWarning,
                    relevantLaws = offline.relevantLaws.split("|").filter { it.isNotBlank() },
                    sourceName = "User Saved Archive",
                    timelineUpdates = listOf(
                        TimelineUpdateItem(
                            id = "${offline.caseId}_init",
                            caseId = offline.caseId,
                            dateLabel = offline.incidentDateLabel,
                            stageBadge = CaseStage.fromCode(offline.caseStageCode).label,
                            headline = "Archived in Private Vault",
                            details = offline.summary,
                            sourceName = "Private Archive",
                            isUserAdded = true,
                            verificationStatus = VerificationStatus.AWAITING_VERIFICATION
                        )
                    ) + extraUpdates,
                    isSavedForLife = true,
                    isFollowed = followedIds.contains(offline.caseId),
                    personalNotes = offline.personalNotes,
                    verificationStatus = VerificationStatus.AWAITING_VERIFICATION
                )
            }
        }

        // 3. Cloud saved cases not in seed or offline yet
        for (cloud in cloudSaved) {
            if (!mergedMap.containsKey(cloud.id)) {
                val extraUpdates = updatesByCaseId[cloud.id].orEmpty().map { upd ->
                    TimelineUpdateItem(
                        id = upd.id,
                        caseId = cloud.id,
                        dateLabel = upd.updateDateLabel,
                        stageBadge = CaseStage.fromCode(upd.stageBadge).label,
                        headline = upd.headline,
                        details = upd.details,
                        sourceName = "User Logged Note",
                        isUserAdded = true,
                        verificationStatus = VerificationStatus.AWAITING_VERIFICATION
                    )
                }
                mergedMap[cloud.id] = CaseDossier(
                    id = cloud.id,
                    title = cloud.title,
                    summary = cloud.summary,
                    fullReport = cloud.fullReport,
                    stateName = cloud.stateName,
                    districtName = cloud.districtName,
                    cityName = cloud.cityName,
                    mlaName = cloud.mlaName.ifBlank { "Not verified" },
                    mlaConstituency = "${cloud.cityName} AC",
                    mlaParty = "Not verified",
                    mpName = cloud.mpName.ifBlank { "Not verified" },
                    mpConstituency = "${cloud.districtName} PC",
                    mpParty = "Not verified",
                    policeJurisdiction = "${cloud.districtName} District Police",
                    victimCategory = cloud.victimCategory,
                    crimeCategory = cloud.crimeCategory,
                    caseStage = CaseStage.fromCode(cloud.caseStage),
                    incidentDateLabel = cloud.incidentDateLabel,
                    incidentEpochMillis = cloud.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                    sensitivityLevel = SensitivityLevel.SENSITIVE,
                    triggerWarning = "Content Advisory: Saved case dossier.",
                    relevantLaws = listOf("BNSS Section 173 (Zero FIR)", "POCSO & BNS Provisions"),
                    sourceName = "Synced User Archive",
                    timelineUpdates = extraUpdates,
                    isSavedForLife = true,
                    isFollowed = followedIds.contains(cloud.id),
                    personalNotes = cloud.personalNotes.orEmpty(),
                    verificationStatus = VerificationStatus.AWAITING_VERIFICATION
                )
            }
        }

        // 4. Live RSS dispatches (deduplicated against existing titles)
        val existingStems = mergedMap.values.map { rssClient.normalizeTitleStem(it.title) }.toMutableSet()
        for (rss in liveRss) {
            val stem = rssClient.normalizeTitleStem(rss.title)
            if (!mergedMap.containsKey(rss.id) && existingStems.add(stem)) {
                mergedMap[rss.id] = rss.copy(
                    isSavedForLife = savedIds.contains(rss.id),
                    isFollowed = followedIds.contains(rss.id)
                )
            }
        }

        return mergedMap.values.toList()
    }

    private fun CaseDossier.toOfflineEntity(notes: String): SavedCaseOfflineEntity {
        return SavedCaseOfflineEntity(
            caseId = id,
            title = title,
            summary = summary,
            fullReport = fullReport,
            stateName = stateName,
            districtName = districtName,
            cityName = cityName,
            mlaName = mlaName,
            mlaConstituency = mlaConstituency,
            mlaParty = mlaParty,
            mpName = mpName,
            mpConstituency = mpConstituency,
            mpParty = mpParty,
            policeJurisdiction = policeJurisdiction,
            victimCategory = victimCategory,
            crimeCategory = crimeCategory,
            caseStageCode = caseStage.code,
            incidentDateLabel = incidentDateLabel,
            incidentEpochMillis = incidentEpochMillis,
            sensitivityCode = sensitivityLevel.name,
            triggerWarning = triggerWarning,
            relevantLaws = relevantLaws.joinToString("|"),
            personalNotes = notes,
            savedAtMillis = System.currentTimeMillis()
        )
    }
}
