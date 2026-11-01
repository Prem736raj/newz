package com.example.data.remote

import android.util.Xml
import com.example.data.local.SurakshaArchiveSeedData
import com.example.data.model.AppLanguage
import com.example.data.model.CaseDossier
import com.example.data.model.CaseStage
import com.example.data.model.LanguageTier
import com.example.data.model.SensitivityLevel
import com.example.data.model.TimelineUpdateItem
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class IndianCrimeNewsRssClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches live Indian women & minor safety news dispatches.
     * - For Tier A languages (Hindi, Bengali, Marathi, Telugu, Tamil, Gujarati, Kannada, Malayalam, Punjabi, English),
     *   queries Google News RSS natively in that language (`hl=<code-IN>&gl=IN&ceid=IN:<code>`) with native keywords,
     *   and falls back to the English query if the localized feed returns no items.
     * - For Tier B languages (Odia, Assamese, Urdu), fetches the English feed so `SurakshaTranslationService`
     *   can translate and cache the dispatches.
     */
    suspend fun fetchLiveIndianWomenSafetyDispatches(
        stateQuery: String? = null,
        language: AppLanguage = AppLanguage.ENGLISH
    ): List<CaseDossier> = withContext(Dispatchers.IO) {
        val locationTerm = if (!stateQuery.isNullOrBlank() && stateQuery != "All India") {
            "$stateQuery India"
        } else {
            "India"
        }

        if (language.tier == LanguageTier.TIER_A && language != AppLanguage.ENGLISH) {
            val nativeUrl = buildGoogleNewsRssUrl(language, locationTerm)
            val nativeResults = executeRssRequest(nativeUrl, stateQuery)
            if (nativeResults.isNotEmpty()) {
                return@withContext nativeResults
            }
        }

        val englishFallbackUrl = buildGoogleNewsRssUrl(AppLanguage.ENGLISH, locationTerm)
        executeRssRequest(englishFallbackUrl, stateQuery)
    }

    internal fun buildGoogleNewsRssUrl(language: AppLanguage, locationTerm: String): String {
        val effectiveLang = if (language.tier == LanguageTier.TIER_A) language else AppLanguage.ENGLISH
        val rawQuery = "${effectiveLang.nativeRssQuery} $locationTerm".trim()
        val encodedQuery = try {
            URLEncoder.encode(rawQuery, "UTF-8")
        } catch (_: Exception) {
            "crime+against+women+OR+school+girl+safety+OR+POCSO+India"
        }
        val langCode = effectiveLang.code
        return "https://news.google.com/rss/search?q=$encodedQuery&hl=$langCode-IN&gl=IN&ceid=IN:$langCode"
    }

    private fun executeRssRequest(url: String, stateQuery: String?): List<CaseDossier> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; SurakshaChronicle/2.0)")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val xmlBody = response.body?.string() ?: return emptyList()
                parseRssXml(xmlBody, stateQuery)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    internal fun parseRssXml(xml: String, requestedState: String?): List<CaseDossier> {
        val results = mutableListOf<CaseDossier>()
        val seenTitleStems = mutableSetOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var insideItem = false
        var title = ""
        var link = ""
        var pubDate = ""
        var source = "Verified News Publisher"
        var description = ""
        var mediaImageUrl = ""
        var mediaVideoUrl = ""

        while (eventType != XmlPullParser.END_DOCUMENT && results.size < 12) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name.lowercase()
                    when (tagName) {
                        "item" -> {
                            insideItem = true
                            title = ""
                            link = ""
                            pubDate = ""
                            source = "Verified News Publisher"
                            description = ""
                            mediaImageUrl = ""
                            mediaVideoUrl = ""
                        }
                        "title" -> if (insideItem) title = parser.nextText().trim()
                        "link" -> if (insideItem) link = parser.nextText().trim()
                        "pubdate" -> if (insideItem) pubDate = parser.nextText().trim()
                        "source" -> if (insideItem) source = parser.nextText().trim()
                        "description" -> if (insideItem) description = parser.nextText().trim()
                        "media:content", "content", "enclosure", "media:thumbnail", "thumbnail" -> {
                            if (insideItem) {
                                val attrUrl = parser.getAttributeValue(null, "url")?.trim().orEmpty()
                                val attrType = parser.getAttributeValue(null, "type")?.lowercase().orEmpty()
                                val attrMedium = parser.getAttributeValue(null, "medium")?.lowercase().orEmpty()
                                if (attrUrl.startsWith("http")) {
                                    if (attrType.contains("video") || attrMedium == "video" || attrUrl.endsWith(".mp4")) {
                                        if (mediaVideoUrl.isBlank()) mediaVideoUrl = attrUrl
                                    } else if (mediaImageUrl.isBlank()) {
                                        mediaImageUrl = attrUrl
                                    }
                                }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("item", ignoreCase = true) && insideItem) {
                        insideItem = false
                        if (title.isNotBlank()) {
                            val rawTitle = title.substringBeforeLast(" - ").trim()
                            val cleanTitle = sanitizeHeadlineForEditorialIntegrity(rawTitle)
                            val titleStem = normalizeTitleStem(cleanTitle)

                            // Deduplicate identical or near-identical stories across Latin and Indic scripts
                            if (titleStem.length > 4 && seenTitleStems.add(titleStem)) {
                                if (mediaImageUrl.isBlank()) {
                                    val imgMatch = Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                                        .find(description)
                                    val candidateImg = imgMatch?.groupValues?.getOrNull(1)?.trim().orEmpty()
                                    if (candidateImg.startsWith("http")) {
                                        mediaImageUrl = candidateImg
                                    }
                                }

                                val cleanDesc = description
                                    .replace(Regex("<[^>]*>"), " ")
                                    .replace("&nbsp;", " ")
                                    .replace("&amp;", "&")
                                    .replace("&quot;", "\"")
                                    .replace("&#39;", "'")
                                    .replace(Regex("\\s+"), " ")
                                    .trim()
                                    .ifBlank { cleanTitle }

                                val matchedLocation = detectLocationFromText("$cleanTitle $cleanDesc", requestedState)

                                val isHighTrigger = cleanTitle.contains("attack", ignoreCase = true) ||
                                    cleanTitle.contains("assault", ignoreCase = true) ||
                                    cleanTitle.contains("rape", ignoreCase = true) ||
                                    cleanTitle.contains("chainsaw", ignoreCase = true) ||
                                    cleanTitle.contains("stab", ignoreCase = true) ||
                                    cleanTitle.contains("murder", ignoreCase = true) ||
                                    cleanTitle.contains("हमला") ||
                                    cleanTitle.contains("दुष्कर्म") ||
                                    cleanTitle.contains("हत्या")

                                val id = "live_rss_${titleStem.hashCode().toUInt()}"
                                results.add(
                                    CaseDossier(
                                        id = id,
                                        title = cleanTitle,
                                        summary = cleanDesc.take(260),
                                        fullReport = buildString {
                                            appendLine("Reported by $source")
                                            if (pubDate.isNotBlank()) {
                                                appendLine("Published: $pubDate")
                                            }
                                            appendLine()
                                            appendLine(cleanDesc)
                                            appendLine()
                                            appendLine("Editorial Attribution & Licensing Note:")
                                            appendLine("This summary is attributed to $source. To respect publisher copyright and review full reporting, use the 'Open Original Source' button above.")
                                        },
                                        stateName = matchedLocation.stateName,
                                        districtName = matchedLocation.districtName,
                                        cityName = matchedLocation.cityName,
                                        mlaName = matchedLocation.mlaName,
                                        mlaConstituency = matchedLocation.mlaConstituency,
                                        mlaParty = matchedLocation.mlaParty,
                                        mpName = matchedLocation.mpName,
                                        mpConstituency = matchedLocation.mpConstituency,
                                        mpParty = matchedLocation.mpParty,
                                        policeJurisdiction = matchedLocation.policeRange,
                                        victimCategory = if (cleanTitle.contains("school", true) || cleanTitle.contains("minor", true) || cleanTitle.contains("class", true) || cleanTitle.contains("girl", true) || cleanTitle.contains("छात्रा") || cleanTitle.contains("नाबालिग")) {
                                            "Teenage Student (13-19)"
                                        } else {
                                            "Woman"
                                        },
                                        crimeCategory = when {
                                            cleanTitle.contains("school", true) || cleanTitle.contains("college", true) || cleanTitle.contains("स्कूल") || cleanTitle.contains("कॉलेज") -> "School/Campus Safety"
                                            cleanTitle.contains("pocso", true) || cleanTitle.contains("minor", true) || cleanTitle.contains("नाबालिग") -> "POCSO / Minor Protection"
                                            cleanTitle.contains("cyber", true) || cleanTitle.contains("online", true) || cleanTitle.contains("साइबर") -> "Cyber Safety"
                                            cleanTitle.contains("court", true) || cleanTitle.contains("hc", true) || cleanTitle.contains("sc", true) || cleanTitle.contains("कोर्ट") -> "Legal Updates"
                                            else -> "Public Safety Report"
                                        },
                                        caseStage = CaseStage.FIR_FILED,
                                        incidentDateLabel = pubDate.take(16).ifBlank { "Recent Dispatch" },
                                        incidentEpochMillis = System.currentTimeMillis() - 43_200_000L,
                                        sensitivityLevel = if (isHighTrigger) SensitivityLevel.HIGH_DISTURBANCE else SensitivityLevel.SENSITIVE,
                                        triggerWarning = "Content Advisory: Report concerning violence or safety issues affecting women or minors.",
                                        relevantLaws = listOf(
                                            "BNSS Section 173 (Zero FIR Mandate)",
                                            "POCSO Section 23 (Minor Identity Protection)"
                                        ),
                                        sourceName = source,
                                        sourceUrl = link.ifBlank { "https://news.google.com/" },
                                        timelineUpdates = listOf(
                                            TimelineUpdateItem(
                                                id = "${id}_init",
                                                caseId = id,
                                                dateLabel = pubDate.take(16).ifBlank { "Initial Report" },
                                                stageBadge = "Initial Report",
                                                headline = "Published by $source",
                                                details = "Syndicated dispatch. Follow this report to track confirmed FIR, investigation, or court updates.",
                                                sourceName = source,
                                                verificationStatus = VerificationStatus.REPORTED_UNCONFIRMED,
                                                sourceReferenceUrl = link.ifBlank { "https://news.google.com/" },
                                                lastCheckedDate = "Checked Today"
                                            )
                                        ),
                                        isLiveRss = true,
                                        verificationStatus = VerificationStatus.REPORTED_UNCONFIRMED,
                                        lastVerifiedDate = "Syndicated Today",
                                        representativeDataSource = matchedLocation.repSourceNote,
                                        imageUrl = mediaImageUrl,
                                        videoUrl = mediaVideoUrl,
                                        mediaCaption = if (mediaImageUrl.isNotBlank() || mediaVideoUrl.isNotBlank()) {
                                            "Syndicated media attachment from $source"
                                        } else {
                                            ""
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return results
    }

    internal fun normalizeTitleStem(title: String): String {
        return title
            .substringBeforeLast(" - ")
            .lowercase()
            .replace(Regex("[\\p{Punct}]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 1 }
            .take(8)
            .joinToString("_")
    }

    internal fun sanitizeHeadlineForEditorialIntegrity(headline: String): String {
        return headline
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private data class MatchedJurisdiction(
        val stateName: String,
        val districtName: String,
        val cityName: String,
        val mlaName: String,
        val mlaConstituency: String,
        val mlaParty: String,
        val mpName: String,
        val mpConstituency: String,
        val mpParty: String,
        val policeRange: String,
        val repSourceNote: String
    )

    private fun detectLocationFromText(text: String, fallbackState: String?): MatchedJurisdiction {
        for (state in SurakshaArchiveSeedData.stateJurisdictions) {
            for (district in state.districts) {
                for (city in district.cities) {
                    if (text.contains(city.cityName, ignoreCase = true) ||
                        text.contains(district.districtName, ignoreCase = true)
                    ) {
                        return MatchedJurisdiction(
                            stateName = state.stateName,
                            districtName = district.districtName,
                            cityName = city.cityName,
                            mlaName = city.mlaName,
                            mlaConstituency = city.mlaConstituency,
                            mlaParty = city.mlaParty,
                            mpName = city.mpName,
                            mpConstituency = city.mpConstituency,
                            mpParty = city.mpParty,
                            policeRange = city.policeRange,
                            repSourceNote = "Election Commission of India (ECI) Constituency Roll"
                        )
                    }
                }
            }
        }

        for (state in SurakshaArchiveSeedData.stateJurisdictions) {
            if (text.contains(state.stateName, ignoreCase = true)) {
                return MatchedJurisdiction(
                    stateName = state.stateName,
                    districtName = "Statewide / Multiple Districts",
                    cityName = state.stateName,
                    mlaName = "Not verified (Exact Assembly Constituency not specified in report)",
                    mlaConstituency = "Pending Constituency Mapping",
                    mlaParty = "Not verified",
                    mpName = "Not verified (Exact Parliamentary Constituency not specified in report)",
                    mpConstituency = "Pending Constituency Mapping",
                    mpParty = "Not verified",
                    policeRange = "${state.stateName} State Police",
                    repSourceNote = "Constituency not explicitly named in source dispatch; representative mapping withheld to prevent inaccuracy."
                )
            }
        }

        val resolvedState = if (!fallbackState.isNullOrBlank() && fallbackState != "All India") {
            fallbackState
        } else {
            "National / All India"
        }
        return MatchedJurisdiction(
            stateName = resolvedState,
            districtName = "All Districts",
            cityName = resolvedState,
            mlaName = "Not verified (Exact Assembly Constituency not specified)",
            mlaConstituency = "Not verified",
            mlaParty = "Not verified",
            mpName = "Not verified (Exact Parliamentary Constituency not specified)",
            mpConstituency = "Not verified",
            mpParty = "Not verified",
            policeRange = "State / District Police Jurisdiction",
            repSourceNote = "Constituency mapping requires confirmed local police station or ward reference."
        )
    }
}
