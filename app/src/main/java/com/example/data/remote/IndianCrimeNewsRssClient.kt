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
import java.io.ByteArrayOutputStream
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Locale
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class IndianCrimeNewsRssClient {

    private val maxRssBytes = 512 * 1024

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
                // Google RSS feeds are untrusted; enforce an input cap even without Content-Length.
                val body = response.body ?: return emptyList()
                if (body.contentLength() > maxRssBytes) return emptyList()
                val xmlBody = body.byteStream().use { input ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = input.read(buffer)
                        if (count == -1) break
                        if (output.size() + count > maxRssBytes) return emptyList()
                        output.write(buffer, 0, count)
                    }
                    output.toString(Charsets.UTF_8.name())
                }
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
        var source = "Publisher not specified"
        var description = ""

        while (eventType != XmlPullParser.END_DOCUMENT && results.size < 12) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name.lowercase()) {
                        "item" -> {
                            insideItem = true
                            title = ""
                            link = ""
                            pubDate = ""
                            source = "Publisher not specified"
                            description = ""
                        }
                        "title" -> if (insideItem) title = parser.nextText().trim()
                        "link" -> if (insideItem) link = parser.nextText().trim()
                        "pubdate" -> if (insideItem) pubDate = parser.nextText().trim()
                        "source" -> if (insideItem) source = parser.nextText().trim().ifBlank { "Publisher not specified" }
                        "description" -> if (insideItem) description = parser.nextText().trim()
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

                                val publicationEpoch = parseRssPublicationMillis(pubDate)
                                val safeSourceUrl = runCatching {
                                    URI(link).takeIf { it.scheme.equals("https", ignoreCase = true) && !it.host.isNullOrBlank() }
                                        ?.toString()
                                }.getOrNull().orEmpty()
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
                                            appendLine("This is an unverified syndicated excerpt, not an independently checked article or legal record. Read the linked original when available.")
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
                                        victimCategory = "Affected group and age unverified",
                                        crimeCategory = when {
                                            cleanTitle.contains("school", true) || cleanTitle.contains("college", true) || cleanTitle.contains("स्कूल") || cleanTitle.contains("कॉलेज") -> "School/Campus Safety"
                                            cleanTitle.contains("pocso", true) || cleanTitle.contains("minor", true) || cleanTitle.contains("नाबालिग") -> "POCSO / Minor Protection"
                                            cleanTitle.contains("cyber", true) || cleanTitle.contains("online", true) || cleanTitle.contains("साइबर") -> "Cyber Safety"
                                            cleanTitle.contains("court", true) || cleanTitle.contains("hc", true) || cleanTitle.contains("sc", true) || cleanTitle.contains("कोर्ट") -> "Legal Updates"
                                            else -> "Public Safety Report"
                                        },
                                        caseStage = CaseStage.STATUS_UNVERIFIED,
                                        incidentDateLabel = "Incident date unverified",
                                        incidentEpochMillis = 0L,
                                        publishedAtEpochMillis = publicationEpoch,
                                        sensitivityLevel = if (isHighTrigger) SensitivityLevel.HIGH_DISTURBANCE else SensitivityLevel.SENSITIVE,
                                        triggerWarning = "Content Advisory: Report concerning violence or safety issues affecting women or minors.",
                                        relevantLaws = emptyList(),
                                        sourceName = source,
                                        sourceUrl = safeSourceUrl,
                                        timelineUpdates = listOf(
                                            TimelineUpdateItem(
                                                id = "${id}_init",
                                                caseId = id,
                                                dateLabel = pubDate.ifBlank { "Publication date unavailable" },
                                                stageBadge = "Initial Report",
                                                headline = "Published by $source",
                                                details = "Syndicated headline. No automatic official follow-up or notification service is configured.",
                                                sourceName = source,
                                                verificationStatus = VerificationStatus.REPORTED_UNCONFIRMED,
                                                sourceReferenceUrl = safeSourceUrl,
                                                lastCheckedDate = "Not independently reviewed"
                                            )
                                        ),
                                        isLiveRss = true,
                                        verificationStatus = VerificationStatus.REPORTED_UNCONFIRMED,
                                        lastVerifiedDate = "Not independently verified",
                                        representativeDataSource = matchedLocation.repSourceNote
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

    internal fun parseRssPublicationMillis(raw: String): Long? = runCatching {
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).apply { isLenient = false }
            .parse(raw.trim())?.time
    }.getOrNull()

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
                    if (text.contains(city.cityName, ignoreCase = true)) {
                        return MatchedJurisdiction(
                            stateName = state.stateName,
                            districtName = district.districtName,
                            cityName = city.cityName,
                            mlaName = "Not verified",
                            mlaConstituency = "Constituency not established by RSS",
                            mlaParty = "Not verified",
                            mpName = "Not verified",
                            mpConstituency = "Constituency not established by RSS",
                            mpParty = "Not verified",
                            policeRange = "Jurisdiction not independently verified",
                            repSourceNote = "Location keyword matched; constituency and representative identity have not been independently verified."
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
