package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SavedCaseOfflineDao
import com.example.data.local.SurakshaArchiveSeedData
import com.example.data.local.SurakshaHindiSeedTranslations
import com.example.data.local.TranslationCacheDao
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AppLanguage
import com.example.data.model.TranslationCacheEntity
import com.example.data.model.CaseDossier
import com.example.data.model.CaseFollowUp
import com.example.data.model.CaseStage
import com.example.data.model.CityJurisdiction
import com.example.data.model.DistrictJurisdiction
import com.example.data.model.EmergencyHelpline
import com.example.data.model.LegalRightGuide
import com.example.data.model.SavedCase
import com.example.data.model.SavedCaseOfflineEntity
import com.example.data.model.SensitivityLevel
import com.example.data.model.StateJurisdiction
import com.example.data.model.ThemeMode
import com.example.data.model.TimelineUpdateItem
import com.example.data.model.VerificationStatus
import com.example.data.remote.IndianCrimeNewsRssClient
import com.example.data.remote.SurakshaTranslationService
import com.example.data.repository.SurakshaFirestoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

@OptIn(ExperimentalCoroutinesApi::class)
class SurakshaViewModel(
    private val firestoreRepository: SurakshaFirestoreRepository,
    private val offlineDao: SavedCaseOfflineDao,
    private val rssClient: IndianCrimeNewsRssClient,
    private val currentUserId: String,
    private val userPreferencesRepository: UserPreferencesRepository? = null,
    private val translationCacheDao: TranslationCacheDao? = null,
    private val translationService: SurakshaTranslationService? = null
) : ViewModel() {

    val isGuestUser: Boolean = currentUserId.isBlank() || currentUserId == "guest_user"

    private val initialPrefs = userPreferencesRepository?.readInitialPreferencesBlocking()

    val allStates: List<StateJurisdiction> = SurakshaArchiveSeedData.stateJurisdictions
    val emergencyHelplines = SurakshaArchiveSeedData.emergencyHelplines
    val legalRightsGuides = SurakshaArchiveSeedData.legalRightsGuides

    private val _filterState = MutableStateFlow(SurakshaFilterState())
    val filterState: StateFlow<SurakshaFilterState> = _filterState.asStateFlow()

    private val _globalSensitiveBlur = MutableStateFlow(true)
    val globalSensitiveBlur: StateFlow<Boolean> = _globalSensitiveBlur.asStateFlow()

    private val _revealedCaseIds = MutableStateFlow<Set<String>>(emptySet())
    val revealedCaseIds: StateFlow<Set<String>> = _revealedCaseIds.asStateFlow()

    private val _followedCaseIds = MutableStateFlow<Set<String>>(emptySet())
    val followedCaseIds: StateFlow<Set<String>> = _followedCaseIds.asStateFlow()

    private val _themeMode = MutableStateFlow(initialPrefs?.themeMode ?: ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(initialPrefs?.appLanguage ?: AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _fontScale = MutableStateFlow((initialPrefs?.fontScale ?: 1.0f).coerceIn(0.85f, 1.6f))
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _hasCompletedLanguageOnboarding = MutableStateFlow(
        initialPrefs?.hasCompletedLanguageOnboarding ?: true
    )
    val hasCompletedLanguageOnboarding: StateFlow<Boolean> = _hasCompletedLanguageOnboarding.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(false)
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

    private val cloudSavedCasesFlow = if (isGuestUser) {
        flowOf(emptyList())
    } else {
        firestoreRepository.observeSavedCases(currentUserId)
            .catch { e ->
                Log.w("SurakshaVM", "Cloud saved-case stream unavailable; no account details logged")
                emit(emptyList())
            }
    }

    private val cloudFollowUpsFlow = if (isGuestUser) {
        flowOf(emptyList())
    } else {
        firestoreRepository.observeCaseFollowUps(currentUserId)
            .catch { e ->
                Log.w("SurakshaVM", "Cloud follow-up stream unavailable; no account details logged")
                emit(emptyList())
            }
    }

    // TODO(release blocker): partition Room data by authenticated UID before production.
    private val offlineSavedCasesFlow = offlineDao.observeAllSavedCases()
        .catch { e ->
            Log.w("SurakshaVM", "Room saved-case stream unavailable")
            emit(emptyList())
        }

    private val translationCacheFlow = _appLanguage.flatMapLatest { lang ->
        if (lang == AppLanguage.ENGLISH || translationCacheDao == null) {
            flowOf(emptyList())
        } else {
            translationCacheDao.observeTranslationsForLanguage(lang.code)
                .catch { emit(emptyList()) }
        }
    }

    private val baseMergedDossiersFlow = combine(
        cloudSavedCasesFlow,
        cloudFollowUpsFlow,
        offlineSavedCasesFlow,
        _liveRssDossiers,
        _followedCaseIds
    ) { cloudSaved, cloudUpdates, offlineSaved, liveRss, followedIds ->
        mergeAllDossiers(cloudSaved, cloudUpdates, offlineSaved, liveRss, followedIds)
    }

    val allDossiers: StateFlow<List<CaseDossier>> = combine(
        baseMergedDossiersFlow,
        _appLanguage,
        translationCacheFlow
    ) { merged, language, cacheEntries ->
        localizeDossiers(merged, language, cacheEntries)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = localizeDossiers(
            dossiers = SurakshaArchiveSeedData.buildPublishedCaseDossiers().map {
                it.copy(isFollowed = _followedCaseIds.value.contains(it.id))
            },
            language = _appLanguage.value,
            cacheEntries = emptyList()
        )
    )

    val localizedHelplines: StateFlow<List<EmergencyHelpline>> = combine(
        _appLanguage,
        translationCacheFlow
    ) { language, cacheEntries ->
        localizeHelplinesList(emergencyHelplines, language, cacheEntries)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = localizeHelplinesList(emergencyHelplines, _appLanguage.value, emptyList())
    )

    val localizedLegalGuides: StateFlow<List<LegalRightGuide>> = combine(
        _appLanguage,
        translationCacheFlow
    ) { language, cacheEntries ->
        localizeLegalGuidesList(legalRightsGuides, language, cacheEntries)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = localizeLegalGuidesList(legalRightsGuides, _appLanguage.value, emptyList())
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
                dossier.originalTitle.contains(filter.searchQuery, ignoreCase = true) ||
                dossier.summary.contains(filter.searchQuery, ignoreCase = true) ||
                dossier.originalSummary.contains(filter.searchQuery, ignoreCase = true) ||
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
        initialValue = allDossiers.value
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
        userPreferencesRepository?.let { repo ->
            viewModelScope.launch {
                repo.preferencesFlow.collect { prefs ->
                    _appLanguage.value = prefs.appLanguage
                    _themeMode.value = prefs.themeMode
                    _fontScale.value = prefs.fontScale.coerceIn(0.85f, 1.6f)
                    _hasCompletedLanguageOnboarding.value = prefs.hasCompletedLanguageOnboarding
                }
            }
        }
        viewModelScope.launch {
            translationService?.ensureHindiSeedTranslationsCached()
        }
        refreshLiveIndianNews()
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
            _statusBannerMessage.value = "Added to follow list for this session. Notifications are not active."
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            userPreferencesRepository?.setThemeMode(mode)
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        val changed = _appLanguage.value != language
        _appLanguage.value = language
        _hasCompletedLanguageOnboarding.value = true
        viewModelScope.launch {
            userPreferencesRepository?.setAppLanguage(language)
            if (language == AppLanguage.HINDI) {
                translationService?.ensureHindiSeedTranslationsCached()
            }
            if (changed) {
                refreshLiveIndianNews()
            }
            translationService?.translateUncachedDossiersIfNeeded(
                dossiers = baseMergedDossiersFlowToSnapshot(),
                targetLanguage = language
            )
        }
    }

    fun completeLanguageOnboarding(language: AppLanguage) {
        setAppLanguage(language)
    }

    fun setFontScale(scale: Float) {
        val clamped = scale.coerceIn(0.85f, 1.6f)
        _fontScale.value = clamped
        viewModelScope.launch {
            userPreferencesRepository?.setFontScale(clamped)
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
        val entry = "Unsent local draft ($issueType): ${details.trim()}"
        _correctionLogs.value = _correctionLogs.value + (caseId to (existing + entry))
        _statusBannerMessage.value =
            "Draft recorded on this screen only. No report was submitted or reference ID issued."
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
        if (stateName == "All India" || districtName == "All Districts" || cityName == "All Cities") return null
        val stateObj = allStates.find { it.stateName.equals(stateName, ignoreCase = true) } ?: return null
        val districtObj = stateObj.districts.find {
            it.districtName.equals(districtName, ignoreCase = true)
        } ?: return null
        return districtObj.cities.find { it.cityName.equals(cityName, ignoreCase = true) }
    }

    private var latestRssRequestId = 0L

    fun refreshLiveIndianNews(stateOverride: String? = null) {
        val requestId = ++latestRssRequestId
        val targetState = stateOverride ?: _filterState.value.selectedState
        val activeLang = _appLanguage.value
        viewModelScope.launch {
            _isRefreshingLive.value = true
            _feedSyncNote.value = null
            try {
                val live = rssClient.fetchLiveIndianWomenSafetyDispatches(
                    stateQuery = targetState,
                    language = activeLang
                )
                // A late reply for State A must not overwrite newer State B results.
                if (requestId != latestRssRequestId) return@launch
                if (live.isNotEmpty()) {
                    _liveRssDossiers.value = live
                    if (activeLang != AppLanguage.ENGLISH) {
                        translationService?.translateUncachedDossiersIfNeeded(
                            dossiers = live,
                            targetLanguage = activeLang
                        )
                    }
                } else {
                    _feedSyncNote.value =
                        "Unable to refresh. Showing previously available reports; verification status varies."
                }
            } catch (e: Exception) {
                if (requestId == latestRssRequestId) {
                    _feedSyncNote.value = "Unable to refresh. Showing previously available reports; verification status varies."
                }
            } finally {
                if (requestId == latestRssRequestId) _isRefreshingLive.value = false
            }
        }
    }

    fun toggleSaveForLife(dossier: CaseDossier, personalNotes: String = "") {
        viewModelScope.launch {
            if (dossier.isSavedForLife) {
                offlineDao.deleteSavedCaseById(dossier.id)
                if (!isGuestUser) {
                    firestoreRepository.removeSavedCase(dossier.id)
                }
                _statusBannerMessage.value = "Removed from Saved Articles & Offline Cache."
            } else {
                val note = personalNotes.ifBlank {
                    dossier.personalNotes.ifBlank {
                        "Saved for offline access and long-term case tracking."
                    }
                }
                offlineDao.upsertSavedCase(dossier.toOfflineEntity(note))
                if (!isGuestUser) {
                    val cloudModel = SavedCase(
                        id = dossier.id,
                        userId = currentUserId,
                        title = dossier.originalTitle.ifBlank { dossier.title },
                        summary = dossier.originalSummary.ifBlank { dossier.summary },
                        fullReport = dossier.originalFullReport.ifBlank { dossier.fullReport },
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
                        "Saved on this device. Cloud sync failed; no automatic retry is configured."
                    }
                } else {
                    _statusBannerMessage.value = "Saved to device for offline reading."
                }
            }
        }
    }

    fun updatePersonalNote(dossier: CaseDossier, newNote: String) {
        viewModelScope.launch {
            val cleanNote = newNote.trim()
            offlineDao.upsertSavedCase(dossier.toOfflineEntity(cleanNote))
            if (!isGuestUser) {
                val cloudModel = SavedCase(
                    id = dossier.id,
                    userId = currentUserId,
                    title = dossier.originalTitle.ifBlank { dossier.title },
                    summary = dossier.originalSummary.ifBlank { dossier.summary },
                    fullReport = dossier.originalFullReport.ifBlank { dossier.fullReport },
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
            }
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
            val result = if (!isGuestUser) {
                firestoreRepository.addCaseFollowUp(
                    updateId = updateId,
                    caseId = dossier.id,
                    caseTitle = dossier.originalTitle.ifBlank { dossier.title },
                    updateDateLabel = updateDateLabel.ifBlank { "Day ${dossier.daysSinceIncident} Update" },
                    stageBadge = newStage.code,
                    headline = headline.trim(),
                    details = details.trim()
                )
            } else {
                Result.failure(IllegalStateException("Guest mode"))
            }
            val updatedDossier = dossier // User notes must never change a verified case's official legal stage.
            offlineDao.upsertSavedCase(
                updatedDossier.toOfflineEntity(
                    dossier.personalNotes.ifBlank { "Latest logged update: $headline" }
                )
            )
            if (!isGuestUser) {
                firestoreRepository.saveCaseForLife(
                    dossier.id,
                    SavedCase(
                        id = dossier.id,
                        userId = currentUserId,
                        title = dossier.originalTitle.ifBlank { dossier.title },
                        summary = dossier.originalSummary.ifBlank { dossier.summary },
                        fullReport = dossier.originalFullReport.ifBlank { dossier.fullReport },
                        stateName = dossier.stateName,
                        districtName = dossier.districtName,
                        cityName = dossier.cityName,
                        mlaName = "${dossier.mlaName} (${dossier.mlaConstituency})",
                        mpName = "${dossier.mpName} (${dossier.mpConstituency})",
                        victimCategory = dossier.victimCategory,
                        crimeCategory = dossier.crimeCategory,
                        caseStage = dossier.caseStage.code,
                        incidentDateLabel = dossier.incidentDateLabel,
                        personalNotes = "Latest logged update: $headline"
                    )
                )
            }
            _followedCaseIds.value = _followedCaseIds.value + dossier.id
            _statusBannerMessage.value = if (result.isSuccess) {
                "Timeline update recorded (marked 'Submitted • Awaiting Verification' & synced)."
            } else {
                "Timeline entry not saved: cloud update failed. Only the base dossier was cached."
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
                stateName = stateName.ifBlank { "Location not specified" },
                districtName = districtName.ifBlank { "Anakapalli" },
                cityName = cityName.ifBlank { "City not specified" },
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
            val cloudResult = if (!isGuestUser) {
                firestoreRepository.saveCaseForLife(
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
            } else {
                Result.failure(IllegalStateException("Guest mode"))
            }
            _statusBannerMessage.value = if (cloudResult.isSuccess) {
                "Case archived in your Saved Vault (marked 'Awaiting Verification')."
            } else {
                "Case archived locally in your Saved Vault."
            }
        }
    }

    private fun baseMergedDossiersFlowToSnapshot(): List<CaseDossier> {
        return SurakshaArchiveSeedData.buildPublishedCaseDossiers() + _liveRssDossiers.value
    }

    private fun localizeDossiers(
        dossiers: List<CaseDossier>,
        language: AppLanguage,
        cacheEntries: List<TranslationCacheEntity>
    ): List<CaseDossier> {
        if (language == AppLanguage.ENGLISH) {
            return dossiers.map { d ->
                d.copy(
                    isMachineTranslated = false,
                    originalTitle = d.title,
                    originalSummary = d.summary,
                    originalFullReport = d.fullReport
                )
            }
        }

        val cacheMap: Map<Pair<String, String>, String> = cacheEntries.associate {
            (it.contentId to it.field) to it.text
        }
        val hindiSeedCases = if (language == AppLanguage.HINDI) {
            SurakshaHindiSeedTranslations.hindiCaseMap
        } else {
            emptyMap()
        }
        val hindiSeedUpdates = if (language == AppLanguage.HINDI) {
            SurakshaHindiSeedTranslations.hindiTimelineMap
        } else {
            emptyMap()
        }

        return dossiers.map { dossier ->
            val seedCaseHi = hindiSeedCases[dossier.id]
            val cachedTitle = cacheMap[dossier.id to "title"]
            val cachedSummary = cacheMap[dossier.id to "summary"]
            val cachedFullReport = cacheMap[dossier.id to "fullReport"]

            val translatedTitle = seedCaseHi?.title ?: cachedTitle ?: dossier.title
            val translatedSummary = seedCaseHi?.summary ?: cachedSummary ?: dossier.summary
            val translatedFullReport = seedCaseHi?.fullReport ?: cachedFullReport ?: dossier.fullReport
            val translatedVictim = seedCaseHi?.victimCategory ?: dossier.victimCategory
            val translatedCrime = seedCaseHi?.crimeCategory ?: dossier.crimeCategory
            val translatedDate = seedCaseHi?.incidentDateLabel ?: dossier.incidentDateLabel
            val translatedWarning = seedCaseHi?.triggerWarning ?: dossier.triggerWarning

            // Seed cases in Hindi are human-curated editorial translations; live RSS or AI-cached non-seed items are machine-translated
            val isAutoTranslated = seedCaseHi == null && (
                (cachedTitle != null && cachedTitle != dossier.title) ||
                    (cachedSummary != null && cachedSummary != dossier.summary)
                )

            val localizedTimeline = dossier.timelineUpdates.map { item ->
                val seedUpdHi = hindiSeedUpdates[item.id]
                val cachedUpdHeadline = cacheMap[item.id to "headline"]
                val cachedUpdDetails = cacheMap[item.id to "details"]
                val newHeadline = seedUpdHi?.headline ?: cachedUpdHeadline ?: item.headline
                val newDetails = seedUpdHi?.details ?: cachedUpdDetails ?: item.details
                val newDateLabel = seedUpdHi?.dateLabel ?: item.dateLabel
                val updAuto = seedUpdHi == null && (cachedUpdHeadline != null || cachedUpdDetails != null)
                item.copy(
                    dateLabel = newDateLabel,
                    headline = newHeadline,
                    details = newDetails,
                    isMachineTranslated = updAuto,
                    originalHeadline = item.originalHeadline.ifBlank { item.headline },
                    originalDetails = item.originalDetails.ifBlank { item.details }
                )
            }

            dossier.copy(
                title = translatedTitle,
                summary = translatedSummary,
                fullReport = translatedFullReport,
                victimCategory = translatedVictim,
                crimeCategory = translatedCrime,
                incidentDateLabel = translatedDate,
                triggerWarning = translatedWarning,
                timelineUpdates = localizedTimeline,
                isMachineTranslated = isAutoTranslated,
                originalTitle = dossier.originalTitle.ifBlank { dossier.title },
                originalSummary = dossier.originalSummary.ifBlank { dossier.summary },
                originalFullReport = dossier.originalFullReport.ifBlank { dossier.fullReport }
            )
        }
    }

    private fun localizeHelplinesList(
        helplines: List<EmergencyHelpline>,
        language: AppLanguage,
        cacheEntries: List<TranslationCacheEntity>
    ): List<EmergencyHelpline> {
        if (language == AppLanguage.ENGLISH) return helplines
        val cacheMap = cacheEntries.associate { (it.contentId to it.field) to it.text }
        val hindiMap = if (language == AppLanguage.HINDI) SurakshaHindiSeedTranslations.hindiHelplineMap else emptyMap()

        return helplines.map { h ->
            val hi = hindiMap[h.id]
            h.copy(
                title = hi?.title ?: cacheMap[h.id to "title"] ?: h.title,
                subtitle = hi?.subtitle ?: cacheMap[h.id to "subtitle"] ?: h.subtitle,
                availability = hi?.availability ?: cacheMap[h.id to "availability"] ?: h.availability,
                targetGroup = hi?.targetGroup ?: cacheMap[h.id to "targetGroup"] ?: h.targetGroup
            )
        }
    }

    private fun localizeLegalGuidesList(
        guides: List<LegalRightGuide>,
        language: AppLanguage,
        cacheEntries: List<TranslationCacheEntity>
    ): List<LegalRightGuide> {
        if (language == AppLanguage.ENGLISH) return guides
        val cacheMap = cacheEntries.associate { (it.contentId to it.field) to it.text }
        val hindiMap = if (language == AppLanguage.HINDI) SurakshaHindiSeedTranslations.hindiLegalGuideMap else emptyMap()

        return guides.map { g ->
            val hi = hindiMap[g.id]
            val cachedChecklist = cacheMap[g.id to "checklist"]
                ?.split("\n")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
            g.copy(
                codeBadge = hi?.codeBadge ?: g.codeBadge,
                title = hi?.title ?: cacheMap[g.id to "title"] ?: g.title,
                summary = hi?.summary ?: cacheMap[g.id to "summary"] ?: g.summary,
                actionChecklist = hi?.actionChecklist ?: cachedChecklist ?: g.actionChecklist,
                statutoryCitation = hi?.statutoryCitation ?: g.statutoryCitation
            )
        }
    }

    private fun mergeAllDossiers(
        cloudSaved: List<SavedCase>,
        cloudUpdates: List<CaseFollowUp>,
        offlineSaved: List<SavedCaseOfflineEntity>,
        liveRss: List<CaseDossier>,
        followedIds: Set<String>
    ): List<CaseDossier> {
        val seedList = SurakshaArchiveSeedData.buildPublishedCaseDossiers()
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
                    stageBadge = CaseStage.fromCode(upd.stageBadge).code,
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
                        stageBadge = CaseStage.fromCode(upd.stageBadge).code,
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
                            stageBadge = CaseStage.fromCode(offline.caseStageCode).code,
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
                        stageBadge = CaseStage.fromCode(upd.stageBadge).code,
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
            title = originalTitle.ifBlank { title },
            summary = originalSummary.ifBlank { summary },
            fullReport = originalFullReport.ifBlank { fullReport },
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
