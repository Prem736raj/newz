package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class CaseStage(val code: String, val label: String, val stepIndex: Int) {
    FIR_FILED("FIR_FILED", "FIR Registered", 1),
    ACCUSED_ARRESTED("ACCUSED_ARRESTED", "Accused Remanded / In Custody", 2),
    CHARGESHEET_FILED("CHARGESHEET_FILED", "Chargesheet Filed", 3),
    TRIAL_ONGOING("TRIAL_ONGOING", "Court Proceedings Ongoing", 4),
    VERDICT_DELIVERED("VERDICT_DELIVERED", "Judgment Delivered", 5),
    JUSTICE_PENDING("JUSTICE_PENDING", "Awaiting Confirmed Update", 2);

    companion object {
        fun fromCode(code: String): CaseStage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: FIR_FILED
        }
    }
}

enum class VerificationStatus(val label: String) {
    CONFIRMED("Confirmed Official Record"),
    REPORTED_UNCONFIRMED("Reported • Awaiting Independent Confirmation"),
    AWAITING_VERIFICATION("Submitted • Awaiting Verification"),
    CORRECTED("Corrected Editorial Record")
}

enum class SensitivityLevel(val label: String) {
    HIGH_DISTURBANCE("Content Advisory • Serious Violence"),
    SENSITIVE("Content Advisory • Assault / Safety Report"),
    STANDARD("Standard Public Safety Report")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Editorial"),
    DARK("Dark Reader")
}

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी")
}

@IgnoreExtraProperties
data class SavedCase(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val summary: String = "",
    val fullReport: String = "",
    val stateName: String = "",
    val districtName: String = "",
    val cityName: String = "",
    val mlaName: String = "",
    val mpName: String = "",
    val victimCategory: String = "",
    val crimeCategory: String = "",
    val caseStage: String = "FIR_FILED",
    val incidentDateLabel: String = "",
    val personalNotes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CaseFollowUp(
    @DocumentId val id: String = "",
    val userId: String = "",
    val caseId: String = "",
    val caseTitle: String = "",
    val updateDateLabel: String = "",
    val stageBadge: String = "",
    val headline: String = "",
    val details: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Entity(tableName = "saved_cases_offline")
data class SavedCaseOfflineEntity(
    @PrimaryKey val caseId: String,
    val title: String,
    val summary: String,
    val fullReport: String,
    val stateName: String,
    val districtName: String,
    val cityName: String,
    val mlaName: String,
    val mlaConstituency: String,
    val mlaParty: String,
    val mpName: String,
    val mpConstituency: String,
    val mpParty: String,
    val policeJurisdiction: String,
    val victimCategory: String,
    val crimeCategory: String,
    val caseStageCode: String,
    val incidentDateLabel: String,
    val incidentEpochMillis: Long,
    val sensitivityCode: String,
    val triggerWarning: String,
    val relevantLaws: String,
    val personalNotes: String,
    val savedAtMillis: Long
)

data class TimelineUpdateItem(
    val id: String,
    val caseId: String,
    val dateLabel: String,
    val stageBadge: String,
    val headline: String,
    val details: String,
    val sourceName: String,
    val isUserAdded: Boolean = false,
    val verificationStatus: VerificationStatus = if (isUserAdded) {
        VerificationStatus.AWAITING_VERIFICATION
    } else {
        VerificationStatus.CONFIRMED
    },
    val sourceReferenceUrl: String = "https://ecourts.gov.in/",
    val lastCheckedDate: String = "Verified Oct 2026"
)

data class CaseDossier(
    val id: String,
    val title: String,
    val summary: String,
    val fullReport: String,
    val stateName: String,
    val districtName: String,
    val cityName: String,
    val mlaName: String,
    val mlaConstituency: String,
    val mlaParty: String,
    val mpName: String,
    val mpConstituency: String,
    val mpParty: String,
    val policeJurisdiction: String,
    val victimCategory: String,
    val crimeCategory: String,
    val caseStage: CaseStage,
    val incidentDateLabel: String,
    val incidentEpochMillis: Long,
    val sensitivityLevel: SensitivityLevel,
    val triggerWarning: String,
    val relevantLaws: List<String>,
    val sourceName: String,
    val sourceUrl: String = "https://ncw.nic.in/",
    val timelineUpdates: List<TimelineUpdateItem> = emptyList(),
    val isSavedForLife: Boolean = false,
    val isFollowed: Boolean = false,
    val personalNotes: String = "",
    val isLiveRss: Boolean = false,
    val verificationStatus: VerificationStatus = if (isLiveRss) {
        VerificationStatus.REPORTED_UNCONFIRMED
    } else {
        VerificationStatus.CONFIRMED
    },
    val lastVerifiedDate: String = "Oct 2026",
    val representativeDataSource: String = "Election Commission of India (ECI) Official Affidavit & Secretariat Roll"
) {
    val daysSinceIncident: Int
        get() {
            val now = System.currentTimeMillis()
            val diff = (now - incidentEpochMillis).coerceAtLeast(86_400_000L)
            return (diff / 86_400_000L).toInt()
        }
}

data class CityJurisdiction(
    val cityName: String,
    val mlaName: String,
    val mlaConstituency: String,
    val mlaParty: String,
    val mpName: String,
    val mpConstituency: String,
    val mpParty: String,
    val policeRange: String,
    val activeTrackedCases: Int
)

data class DistrictJurisdiction(
    val districtName: String,
    val cities: List<CityJurisdiction>
)

data class StateJurisdiction(
    val stateName: String,
    val stateCode: String,
    val annualReportedCrimesAgainstWomen: Int,
    val chargeSheetRatePercent: Double,
    val pocsoMinorCasesAnnual: Int,
    val keySafetyConcern: String,
    val districts: List<DistrictJurisdiction>
)

data class EmergencyHelpline(
    val id: String,
    val number: String,
    val title: String,
    val subtitle: String,
    val availability: String,
    val targetGroup: String
)

data class LegalRightGuide(
    val id: String,
    val codeBadge: String,
    val title: String,
    val summary: String,
    val actionChecklist: List<String>,
    val statutoryCitation: String
)
