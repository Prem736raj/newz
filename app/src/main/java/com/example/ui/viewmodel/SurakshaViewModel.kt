package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CorrectionRequestDao
import com.example.data.local.SavedCaseOfflineDao
import com.example.data.local.SurakshaArchiveSeedData
import com.example.data.local.SurakshaHindiSeedTranslations
import com.example.data.local.TranslationCacheDao
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.AppLanguage
import com.example.data.model.CorrectionRequestEntity
import com.example.data.model.TranslationCacheEntity
import com.example.data.model.CaseDossier
import com.example.data.model.CaseFollowUp
import com.example.data.model.CaseMediaAttachment
import com.example.data.model.CaseStage
import com.example.data.model.CityJurisdiction
import com.example.data.model.DistrictJurisdiction
import com.example.data.model.EmergencyHelpline
import com.example.data.model.LegalRightGuide
import com.example.data.model.MediaAttachmentType
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
    private val translationService: SurakshaTranslationService? = null,
    private val correctionRequestDao: CorrectionRequestDao? = null
) : ViewModel() {

    val isGuestUser: Boolean = currentUserId.isBlank() || currentUserId == "guest_user"

    private val initialPrefs = userPreferencesRepository?.readInitialPreferencesBlocking()

    val allStates: List<StateJurisdiction> = SurakshaArchiveSeedData.stateJurisdictions
    val emergencyHelplines = SurakshaArchiveSeedData.emergencyHelplines
    val legalRightsGuides = SurakshaArchiveSeedData.legalRightsGuides

    private val _filterState = MutableStateFlow(SurakshaFilterState())
    val filterState: StateFlow<SurakshaFilterState> = _filterState.asStateFlow()

    private val _globalSensitiveBlur = MutableStateFlow(initialPrefs?.sensitiveBlurEnabled ?: false)
    val globalSensitiveBlur: StateFlow<Boolean> = _globalSensitiveBlur.asStateFlow()

    private val _revealedCaseIds = MutableStateFlow<Set<String>>(emptySet())
    val revealedCaseIds: StateFlow<Set<String>> = _revealedCaseIds.asStateFlow()

    private val _followedCaseIds = MutableStateFlow(
        initialPrefs?.followedCaseIds ?: setOf(
            "case_ap_school_chainsaw_01",
            "case_wb_kolkata_medical_03",
            "case_mh_badlapur_school_02"
        )
    )
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

    private val _notificationsEnabled = MutableStateFlow(initialPrefs?.notificationsEnabled ?: true)
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
                Log.w("SurakshaVM", "Offline fallback for cloud saved_cases", e)
                emit(emptyList())
            }
    }

    private val cloudFollowUpsFlow = if (isGuestUser) {
        flowOf(emptyList())
    } else {
        firestoreRepository.observeCaseFollowUps(currentUserId)
            .catch { e ->
                Log.w("SurakshaVM", "Offline fallback for cloud case_updates", e)
                emit(emptyList())
            }
    }

    private val publicDispatchesFlow = if (isGuestUser) {
        flowOf(emptyList())
    } else {
        firestoreRepository.observePublicDispatches()
            .catch { e ->
                Log.w("SurakshaVM", "Offline fallback for public_dispatches", e)
                emit(emptyList())
            }
    }

    private val offlineSavedCasesFlow = offlineDao.observeAllSavedCases()
        .catch { e ->
            Log.w("SurakshaVM", "Error observing Room saved_cases_offline", e)
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
        combine(cloudSavedCasesFlow, cloudFollowUpsFlow, publicDispatchesFlow) { saved, updates, pub ->
            Triple(saved, updates, pub)
        },
        offlineSavedCasesFlow,
        _liveRssDossiers,
        _followedCaseIds
    ) { (cloudSaved, cloudUpdates, publicDispatches), offlineSaved, liveRss, followedIds ->
        mergeAllDossiers(cloudSaved, cloudUpdates, publicDispatches, offlineSaved, liveRss, followedIds)
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
            dossiers = SurakshaArchiveSeedData.buildInitialCaseDossiers().map {
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
                    _followedCaseIds.value = prefs.followedCaseIds
                    _notificationsEnabled.value = prefs.notificationsEnabled
                    _globalSensitiveBlur.value = prefs.sensitiveBlurEnabled
                }
            }
        }
        correctionRequestDao?.let { dao ->
            viewModelScope.launch {
                dao.observeAllCorrectionRequests()
                    .catch { emit(emptyList()) }
                    .collect { entities ->
                        val grouped = entities.groupBy { it.caseId }.mapValues { (_, list) ->
                            list.map { "Pending Editorial Review (${it.issueType}): ${it.details}" }
                        }
                        if (grouped.isNotEmpty()) {
                            val merged = _correctionLogs.value.toMutableMap()
                            for ((cid, entries) in grouped) {
                                val curr = merged[cid].orEmpty()
                                merged[cid] = (curr + entries).distinct()
                            }
                            _correctionLogs.value = merged
                        }
                    }
            }
        }
        viewModelScope.launch {
            translationService?.ensureHindiSeedTranslationsCached()
        }
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
        val next = !_globalSensitiveBlur.value
        _globalSensitiveBlur.value = next
        viewModelScope.launch {
            userPreferencesRepository?.setSensitiveBlurEnabled(next)
        }
    }

    fun revealCaseContent(caseId: String) {
        _revealedCaseIds.value = _revealedCaseIds.value + caseId
    }

    fun toggleFollowCase(dossier: CaseDossier) {
        val current = _followedCaseIds.value
        val updated = if (current.contains(dossier.id)) {
            _statusBannerMessage.value = "Unfollowed case updates for '${dossier.title.take(42)}...'"
            current - dossier.id
        } else {
            _statusBannerMessage.value = "Following case. You will be notified of verified developments."
            current + dossier.id
        }
        _followedCaseIds.value = updated
        viewModelScope.launch {
            userPreferencesRepository?.setFollowedCaseIds(updated)
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

    fun toggleNotificationsEnabled() {
        val next = !_notificationsEnabled.value
        _notificationsEnabled.value = next
        _statusBannerMessage.value = if (next) {
            "Verified case update alerts enabled."
        } else {
            "Verified case update alerts paused."
        }
        viewModelScope.launch {
            userPreferencesRepository?.setNotificationsEnabled(next)
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
        val cleanDetails = details.trim()
        if (cleanDetails.isBlank()) return
        val existing = _correctionLogs.value[caseId] ?: emptyList()
        val entry = "Pending Editorial Review ($issueType): $cleanDetails"
        _correctionLogs.value = _correctionLogs.value + (caseId to (existing + entry).distinct())
        val reqId = "corr_${caseId.takeLast(8)}_${System.currentTimeMillis()}"
        viewModelScope.launch {
            correctionRequestDao?.upsertCorrectionRequest(
                CorrectionRequestEntity(
                    requestId = reqId,
                    caseId = caseId,
                    issueType = issueType,
                    details = cleanDetails,
                    status = "PENDING_EDITORIAL_REVIEW"
                )
            )
            if (!isGuestUser) {
                firestoreRepository.submitCorrectionRequest(
                    requestId = reqId,
                    caseId = caseId,
                    issueType = issueType,
                    details = cleanDetails
                )
            }
        }
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
        val activeLang = _appLanguage.value
        viewModelScope.launch {
            _isRefreshingLive.value = true
            _feedSyncNote.value = null
            try {
                val live = rssClient.fetchLiveIndianWomenSafetyDispatches(
                    stateQuery = targetState,
                    language = activeLang
                )
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
                        "Showing verified archival & cached reports (live syndication feed unreachable or rate-limited)."
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
                        personalNotes = note,
                        imageUrl = dossier.imageUrl,
                        videoUrl = dossier.videoUrl,
                        mediaCaption = dossier.mediaCaption
                    )
                    val res = firestoreRepository.saveCaseForLife(dossier.id, cloudModel)
                    _statusBannerMessage.value = if (res.isSuccess) {
                        "Saved for offline reading & synced to your account."
                    } else {
                        "Saved to device for offline reading (cloud sync pending)."
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
                    personalNotes = cleanNote,
                    imageUrl = dossier.imageUrl,
                    videoUrl = dossier.videoUrl,
                    mediaCaption = dossier.mediaCaption
                )
                firestoreRepository.saveCaseForLife(dossier.id, cloudModel)
            }
            _statusBannerMessage.value = "Private note saved."
        }
    }

    fun attachMediaToDossier(
        dossier: CaseDossier,
        imageUrl: String,
        videoUrl: String,
        mediaCaption: String
    ) {
        val cleanImg = imageUrl.trim()
        val cleanVid = videoUrl.trim()
        val cleanCaption = mediaCaption.trim().ifBlank {
            "Verified incident photo/video attached by user (POCSO Section 23 compliant)."
        }
        if (cleanImg.isBlank() && cleanVid.isBlank()) return
        viewModelScope.launch {
            val updatedDossier = dossier.copy(
                imageUrl = cleanImg.ifBlank { dossier.imageUrl },
                videoUrl = cleanVid.ifBlank { dossier.videoUrl },
                mediaCaption = cleanCaption
            )
            offlineDao.upsertSavedCase(
                updatedDossier.toOfflineEntity(
                    dossier.personalNotes.ifBlank { "Attached incident media." }
                )
            )
            if (!isGuestUser) {
                val cloudCase = SavedCase(
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
                    personalNotes = dossier.personalNotes.ifBlank { "Attached incident media." },
                    imageUrl = updatedDossier.imageUrl,
                    videoUrl = updatedDossier.videoUrl,
                    mediaCaption = updatedDossier.mediaCaption
                )
                // Keep user-attached media strictly in the user's private saved vault; never auto-publish to /public_dispatches
                firestoreRepository.saveCaseForLife(dossier.id, cloudCase)
            }
            _statusBannerMessage.value = "Incident photo/video saved to your private case dossier."
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
            val updatedDossier = dossier.copy(caseStage = newStage)
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
                        caseStage = newStage.code,
                        incidentDateLabel = dossier.incidentDateLabel,
                        personalNotes = "Latest logged update: $headline",
                        imageUrl = dossier.imageUrl,
                        videoUrl = dossier.videoUrl,
                        mediaCaption = dossier.mediaCaption
                    )
                )
            }
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
        personalNotes: String,
        imageUrl: String = "",
        videoUrl: String = "",
        mediaCaption: String = ""
    ) {
        if (title.isBlank() || summary.isBlank()) return
        viewModelScope.launch {
            val newCaseId = "case_user_${System.currentTimeMillis()}"
            val rep = getActiveConstituencyRepresentative(stateName, districtName, cityName)
            val resolvedMla = mlaName.ifBlank { rep?.mlaName ?: "Not verified" }
            val resolvedMp = mpName.ifBlank { rep?.mpName ?: "Not verified" }
            val cleanImg = imageUrl.trim()
            val cleanVid = videoUrl.trim()
            val cleanCaption = mediaCaption.trim().ifBlank {
                if (cleanImg.isNotBlank() || cleanVid.isNotBlank()) {
                    "Attached incident photo/video (POCSO Section 23 & BNS Section 72 compliant)."
                } else {
                    ""
                }
            }

            val attachments = buildList {
                if (cleanImg.isNotBlank()) {
                    add(
                        CaseMediaAttachment(
                            id = "${newCaseId}_img",
                            type = MediaAttachmentType.IMAGE,
                            url = cleanImg,
                            thumbnailUrl = cleanImg,
                            title = "Attached Incident Photo — ${cityName.ifBlank { "Elamanchili" }}",
                            caption = cleanCaption,
                            sourceCredit = "User Submitted Media (Awaiting Editorial Verification)"
                        )
                    )
                }
                if (cleanVid.isNotBlank()) {
                    add(
                        CaseMediaAttachment(
                            id = "${newCaseId}_vid",
                            type = MediaAttachmentType.VIDEO,
                            url = cleanVid,
                            thumbnailUrl = cleanImg,
                            title = "Attached Incident Video Report — ${cityName.ifBlank { "Elamanchili" }}",
                            caption = cleanCaption,
                            sourceCredit = "User Submitted Video (Awaiting Editorial Verification)",
                            durationLabel = "VIDEO",
                            externalBroadcastUrl = cleanVid
                        )
                    )
                }
            }

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
                imageUrl = cleanImg,
                videoUrl = cleanVid,
                mediaCaption = cleanCaption,
                mediaAttachments = attachments,
                isSavedForLife = true,
                isFollowed = true,
                personalNotes = personalNotes,
                verificationStatus = VerificationStatus.AWAITING_VERIFICATION
            )

            offlineDao.upsertSavedCase(newDossier.toOfflineEntity(personalNotes))
            val updatedFollowed = _followedCaseIds.value + newCaseId
            _followedCaseIds.value = updatedFollowed
            userPreferencesRepository?.setFollowedCaseIds(updatedFollowed)
            val cloudResult = if (!isGuestUser) {
                val cloudCase = SavedCase(
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
                    personalNotes = personalNotes.ifBlank { "Archived in private vault." },
                    imageUrl = cleanImg,
                    videoUrl = cleanVid,
                    mediaCaption = cleanCaption
                )
                // SECURITY & PRIVACY RULE: Store strictly in user's private vault; never auto-publish to /public_dispatches
                firestoreRepository.saveCaseForLife(newCaseId, cloudCase)
            } else {
                Result.failure(IllegalStateException("Guest mode"))
            }
            _statusBannerMessage.value = if (cloudResult.isSuccess) {
                "Case archived in your private Saved Vault (marked 'Awaiting Editorial Review')."
            } else {
                "Case archived locally in your Saved Vault."
            }
        }
    }

    private fun baseMergedDossiersFlowToSnapshot(): List<CaseDossier> {
        return SurakshaArchiveSeedData.buildInitialCaseDossiers() + _liveRssDossiers.value
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
        publicDispatches: List<SavedCase>,
        offlineSaved: List<SavedCaseOfflineEntity>,
        liveRss: List<CaseDossier>,
        followedIds: Set<String>
    ): List<CaseDossier> {
        val seedList = SurakshaArchiveSeedData.buildInitialCaseDossiers()
        val savedIds = (cloudSaved.map { it.id } + offlineSaved.map { it.caseId }).toSet()
        val updatesByCaseId = cloudUpdates.groupBy { it.caseId }
        val allCloudRecords = (cloudSaved + publicDispatches).distinctBy { it.id }

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
                ?: publicDispatches.find { it.id == seed.id }
            val latestStage = cloudMatch?.let { CaseStage.fromCode(it.caseStage) }
                ?: offlineMatch?.let { CaseStage.fromCode(it.caseStageCode) }
                ?: seed.caseStage

            val extraMedia = buildList {
                val customImg = cloudMatch?.imageUrl?.takeIf { it.isNotBlank() && it != seed.imageUrl }
                    ?: offlineMatch?.imageUrl?.takeIf { it.isNotBlank() && it != seed.imageUrl }
                val customVid = cloudMatch?.videoUrl?.takeIf { it.isNotBlank() && it != seed.videoUrl }
                    ?: offlineMatch?.videoUrl?.takeIf { it.isNotBlank() && it != seed.videoUrl }
                val customCap = cloudMatch?.mediaCaption?.takeIf { it.isNotBlank() }
                    ?: offlineMatch?.mediaCaption?.takeIf { it.isNotBlank() }
                    ?: "User-attached incident media"

                if (customImg != null) {
                    add(
                        CaseMediaAttachment(
                            id = "${seed.id}_user_img",
                            type = MediaAttachmentType.IMAGE,
                            url = customImg,
                            thumbnailUrl = customImg,
                            title = "Attached Incident Photo — ${seed.cityName}",
                            caption = customCap,
                            sourceCredit = "User Attached Photo"
                        )
                    )
                }
                if (customVid != null) {
                    add(
                        CaseMediaAttachment(
                            id = "${seed.id}_user_vid",
                            type = MediaAttachmentType.VIDEO,
                            url = customVid,
                            thumbnailUrl = customImg ?: seed.imageUrl,
                            title = "Attached Incident Video — ${seed.cityName}",
                            caption = customCap,
                            sourceCredit = "User Attached Video",
                            durationLabel = "VIDEO",
                            externalBroadcastUrl = customVid
                        )
                    )
                }
            }

            mergedMap[seed.id] = seed.copy(
                caseStage = latestStage,
                isSavedForLife = savedIds.contains(seed.id),
                isFollowed = followedIds.contains(seed.id),
                personalNotes = cloudMatch?.personalNotes ?: offlineMatch?.personalNotes ?: seed.personalNotes,
                imageUrl = cloudMatch?.imageUrl?.ifBlank { null }
                    ?: offlineMatch?.imageUrl?.ifBlank { null }
                    ?: seed.imageUrl,
                videoUrl = cloudMatch?.videoUrl?.ifBlank { null }
                    ?: offlineMatch?.videoUrl?.ifBlank { null }
                    ?: seed.videoUrl,
                mediaCaption = cloudMatch?.mediaCaption?.ifBlank { null }
                    ?: offlineMatch?.mediaCaption?.ifBlank { null }
                    ?: seed.mediaCaption,
                mediaAttachments = seed.mediaAttachments + extraMedia,
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
                val offlineAttachments = buildList {
                    if (offline.imageUrl.isNotBlank()) {
                        add(
                            CaseMediaAttachment(
                                id = "${offline.caseId}_img",
                                type = MediaAttachmentType.IMAGE,
                                url = offline.imageUrl,
                                thumbnailUrl = offline.imageUrl,
                                title = "Attached Incident Photo — ${offline.cityName}",
                                caption = offline.mediaCaption.ifBlank { offline.summary },
                                sourceCredit = "User Submitted Media"
                            )
                        )
                    }
                    if (offline.videoUrl.isNotBlank()) {
                        add(
                            CaseMediaAttachment(
                                id = "${offline.caseId}_vid",
                                type = MediaAttachmentType.VIDEO,
                                url = offline.videoUrl,
                                thumbnailUrl = offline.imageUrl,
                                title = "Attached Incident Video — ${offline.cityName}",
                                caption = offline.mediaCaption.ifBlank { offline.summary },
                                sourceCredit = "User Submitted Video",
                                durationLabel = "VIDEO",
                                externalBroadcastUrl = offline.videoUrl
                            )
                        )
                    }
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
                    imageUrl = offline.imageUrl,
                    videoUrl = offline.videoUrl,
                    mediaCaption = offline.mediaCaption,
                    mediaAttachments = offlineAttachments,
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

        // 3. Cloud saved cases & shared public dispatches not in seed or offline yet
        for (cloud in allCloudRecords) {
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
                val cloudAttachments = buildList {
                    if (cloud.imageUrl.isNotBlank()) {
                        add(
                            CaseMediaAttachment(
                                id = "${cloud.id}_img",
                                type = MediaAttachmentType.IMAGE,
                                url = cloud.imageUrl,
                                thumbnailUrl = cloud.imageUrl,
                                title = "Attached Incident Photo — ${cloud.cityName}",
                                caption = cloud.mediaCaption.ifBlank { cloud.summary },
                                sourceCredit = "Shared Cloud Dispatch"
                            )
                        )
                    }
                    if (cloud.videoUrl.isNotBlank()) {
                        add(
                            CaseMediaAttachment(
                                id = "${cloud.id}_vid",
                                type = MediaAttachmentType.VIDEO,
                                url = cloud.videoUrl,
                                thumbnailUrl = cloud.imageUrl,
                                title = "Attached Incident Video — ${cloud.cityName}",
                                caption = cloud.mediaCaption.ifBlank { cloud.summary },
                                sourceCredit = "Shared Cloud Dispatch",
                                durationLabel = "VIDEO",
                                externalBroadcastUrl = cloud.videoUrl
                            )
                        )
                    }
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
                    triggerWarning = "Content Advisory: Shared case dossier.",
                    relevantLaws = listOf("BNSS Section 173 (Zero FIR)", "POCSO & BNS Provisions"),
                    sourceName = if (cloud.visibility == "PUBLIC") "Community Cloud Dispatch (Awaiting Verification)" else "Synced User Archive",
                    imageUrl = cloud.imageUrl,
                    videoUrl = cloud.videoUrl,
                    mediaCaption = cloud.mediaCaption,
                    mediaAttachments = cloudAttachments,
                    timelineUpdates = extraUpdates,
                    isSavedForLife = savedIds.contains(cloud.id),
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
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            mediaCaption = mediaCaption,
            savedAtMillis = System.currentTimeMillis()
        )
    }
}
