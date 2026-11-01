package com.example.data.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class CaseStage(
    val code: String,
    @StringRes val labelRes: Int,
    val stepIndex: Int
) {
    FIR_FILED("FIR_FILED", R.string.stage_fir_filed, 1),
    ACCUSED_ARRESTED("ACCUSED_ARRESTED", R.string.stage_arrest_made, 2),
    CHARGESHEET_FILED("CHARGESHEET_FILED", R.string.stage_chargesheet_filed, 3),
    TRIAL_ONGOING("TRIAL_ONGOING", R.string.stage_court_hearing, 4),
    VERDICT_DELIVERED("VERDICT_DELIVERED", R.string.stage_verdict, 5),
    JUSTICE_PENDING("JUSTICE_PENDING", R.string.stage_under_investigation, 2),
    STATUS_UNVERIFIED("STATUS_UNVERIFIED", R.string.stage_status_unverified, 0);

    companion object {
        fun fromCode(code: String): CaseStage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: STATUS_UNVERIFIED
        }
    }
}

enum class VerificationStatus(@StringRes val labelRes: Int) {
    CONFIRMED(R.string.verification_police_fir),
    REPORTED_UNCONFIRMED(R.string.verification_single_report),
    AWAITING_VERIFICATION(R.string.verification_developing),
    CORRECTED(R.string.verification_multi_source)
}

enum class SensitivityLevel(@StringRes val labelRes: Int) {
    HIGH_DISTURBANCE(R.string.sensitivity_minor_protected),
    SENSITIVE(R.string.sensitivity_identity_withheld),
    STANDARD(R.string.sensitivity_standard)
}

enum class ThemeMode(@StringRes val labelRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}

enum class LanguageTier {
    TIER_A, // Native Google News RSS available + full UI & content support
    TIER_B  // UI & content translated; news obtained by translating the English feed
}

enum class LanguageScript(val isIndicOrTallMatra: Boolean) {
    LATIN(false),
    DEVANAGARI(true),
    BENGALI(true),
    TELUGU(true),
    TAMIL(true),
    GUJARATI(true),
    KANNADA(true),
    MALAYALAM(true),
    GURMUKHI(true),
    ODIA(true),
    PERSO_ARABIC(true)
}

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val tier: LanguageTier,
    val script: LanguageScript,
    val isRtl: Boolean = false,
    val nativeRssQuery: String = ""
) {
    ENGLISH(
        code = "en",
        nativeName = "English",
        englishName = "English",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.LATIN,
        isRtl = false,
        nativeRssQuery = "India crime against women OR girl safety OR POCSO OR chargesheet"
    ),
    HINDI(
        code = "hi",
        nativeName = "हिन्दी",
        englishName = "Hindi",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.DEVANAGARI,
        isRtl = false,
        nativeRssQuery = "महिला सुरक्षा OR महिलाओं के खिलाफ अपराध OR POCSO OR छात्रा सुरक्षा OR चार्जशीट"
    ),
    BENGALI(
        code = "bn",
        nativeName = "বাংলা",
        englishName = "Bengali",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.BENGALI,
        isRtl = false,
        nativeRssQuery = "নারী নিরাপত্তা OR পকসো OR চার্জশিট OR মহিলা অপরাধ"
    ),
    MARATHI(
        code = "mr",
        nativeName = "मराठी",
        englishName = "Marathi",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.DEVANAGARI,
        isRtl = false,
        nativeRssQuery = "महिला सुरक्षा OR पॉक्सो OR आरोपपत्र OR महिलांवरील अत्याचार"
    ),
    TELUGU(
        code = "te",
        nativeName = "తెలుగు",
        englishName = "Telugu",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.TELUGU,
        isRtl = false,
        nativeRssQuery = "మహిళా భద్రత OR పోక్సో OR చార్జిషీట్"
    ),
    TAMIL(
        code = "ta",
        nativeName = "தமிழ்",
        englishName = "Tamil",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.TAMIL,
        isRtl = false,
        nativeRssQuery = "பெண்கள் பாதுகாப்பு OR போக்சோ OR குற்றப்பத்திரிகை"
    ),
    GUJARATI(
        code = "gu",
        nativeName = "ગુજરાતી",
        englishName = "Gujarati",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.GUJARATI,
        isRtl = false,
        nativeRssQuery = "મહિલા સુરક્ષા OR પોક્સો OR ચાર્જશીટ"
    ),
    KANNADA(
        code = "kn",
        nativeName = "ಕನ್ನಡ",
        englishName = "Kannada",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.KANNADA,
        isRtl = false,
        nativeRssQuery = "ಮಹಿಳಾ ಸುರಕ್ಷತೆ OR ಪೋಕ್ಸೋ OR ಚಾರ್ಜ್‌ಶೀಟ್"
    ),
    MALAYALAM(
        code = "ml",
        nativeName = "മലയാളം",
        englishName = "Malayalam",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.MALAYALAM,
        isRtl = false,
        nativeRssQuery = "സ്ത്രീ സുരക്ഷ OR പോക്സോ OR കുറ്റപത്രം"
    ),
    PUNJABI(
        code = "pa",
        nativeName = "ਪੰਜਾਬੀ",
        englishName = "Punjabi",
        tier = LanguageTier.TIER_A,
        script = LanguageScript.GURMUKHI,
        isRtl = false,
        nativeRssQuery = "ਮਹਿਲਾ ਸੁਰੱਖਿਆ OR ਪੋਕਸੋ OR ਚਾਰਜਸ਼ੀਟ"
    ),
    ODIA(
        code = "or",
        nativeName = "ଓଡ଼ିଆ",
        englishName = "Odia",
        tier = LanguageTier.TIER_B,
        script = LanguageScript.ODIA,
        isRtl = false
    ),
    ASSAMESE(
        code = "as",
        nativeName = "অসমীয়া",
        englishName = "Assamese",
        tier = LanguageTier.TIER_B,
        script = LanguageScript.BENGALI,
        isRtl = false
    ),
    URDU(
        code = "ur",
        nativeName = "اردو",
        englishName = "Urdu",
        tier = LanguageTier.TIER_B,
        script = LanguageScript.PERSO_ARABIC,
        isRtl = true
    );

    val label: String
        get() = nativeName

    companion object {
        fun fromCode(code: String?): AppLanguage {
            if (code.isNullOrBlank()) return ENGLISH
            return entries.find { it.code.equals(code.trim(), ignoreCase = true) } ?: ENGLISH
        }
    }
}

@Entity(
    tableName = "translation_cache",
    primaryKeys = ["contentId", "langCode", "field"]
)
data class TranslationCacheEntity(
    val contentId: String,
    val langCode: String,
    val field: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

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
    val caseStage: String = "STATUS_UNVERIFIED",
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
        VerificationStatus.AWAITING_VERIFICATION
    },
    val sourceReferenceUrl: String = "",
    val lastCheckedDate: String = "Not independently verified",
    val isMachineTranslated: Boolean = false,
    val originalHeadline: String = headline,
    val originalDetails: String = details
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
    val sourceUrl: String = "",
    val timelineUpdates: List<TimelineUpdateItem> = emptyList(),
    val isSavedForLife: Boolean = false,
    val isFollowed: Boolean = false,
    val personalNotes: String = "",
    val isLiveRss: Boolean = false,
    val verificationStatus: VerificationStatus = if (isLiveRss) {
        VerificationStatus.REPORTED_UNCONFIRMED
    } else {
        VerificationStatus.AWAITING_VERIFICATION
    },
    val lastVerifiedDate: String = "Not independently verified",
    val representativeDataSource: String = "Representative data not independently verified",
    val isMachineTranslated: Boolean = false,
    val originalTitle: String = title,
    val originalSummary: String = summary,
    val originalFullReport: String = fullReport,
    val publishedAtEpochMillis: Long? = null
) {
    val daysSinceIncident: Int
        get() {
            val now = System.currentTimeMillis()
            val diff = if (incidentEpochMillis <= 0L || incidentEpochMillis > now) 0L else (now - incidentEpochMillis).coerceAtLeast(0L)
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
