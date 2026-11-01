package com.example.data.remote

import com.example.data.local.SurakshaHindiSeedTranslations
import com.example.data.local.TranslationCacheDao
import com.example.data.model.AppLanguage
import com.example.data.model.CaseDossier
import com.example.data.model.TranslationCacheEntity
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class GeminiCaseTranslationResponse(
    val title: String = "",
    val summary: String = "",
    val fullReport: String = ""
)

/**
 * Handles content translation and Room-backed caching (`translation_cache`).
 * - Seeds verified Hindi translations on initialization so seed cases, helplines, and legal guides
 *   render immediately and offline.
 * - Uses Firebase AI Logic (`gemini-3.8-flash`) to translate uncached live RSS items or user-added cases
 *   into the active language, sending ONLY the public article/case text (never user PII) and preserving
 *   all facts, dates, numbers, and court/station names.
 */
class SurakshaTranslationService(
    private val translationCacheDao: TranslationCacheDao
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val translationSchema = Schema.obj(
        mapOf(
            "title" to Schema.string("Factual, dignified headline translated into target language"),
            "summary" to Schema.string("Translated summary preserving all dates, numbers, and court names"),
            "fullReport" to Schema.string("Translated full report preserving facts and survivor anonymity")
        )
    )

    suspend fun ensureHindiSeedTranslationsCached() = withContext(Dispatchers.IO) {
        val existing = translationCacheDao.getTranslatedField("case_ap_school_chainsaw_01", "hi", "title")
        if (existing.isNullOrBlank()) {
            translationCacheDao.upsertTranslations(SurakshaHindiSeedTranslations.buildSeedCacheEntities())
        }
    }

    /**
     * Checks if a given text already contains characters from the target script (e.g., Devanagari for Hindi),
     * in which case native RSS articles do not need machine translation.
     */
    fun isAlreadyInTargetScript(text: String, language: AppLanguage): Boolean {
        if (language == AppLanguage.ENGLISH) return true
        val unicodeRange: IntRange = when (language.code) {
            "hi", "mr" -> 0x0900..0x097F // Devanagari
            "bn", "as" -> 0x0980..0x09FF // Bengali / Assamese
            "pa" -> 0x0A00..0x0A7F       // Gurmukhi
            "gu" -> 0x0A80..0x0AFF       // Gujarati
            "or" -> 0x0B00..0x0B7F       // Odia
            "ta" -> 0x0B80..0x0BFF       // Tamil
            "te" -> 0x0C00..0x0C7F       // Telugu
            "kn" -> 0x0C80..0x0CFF       // Kannada
            "ml" -> 0x0D00..0x0D7F       // Malayalam
            "ur" -> 0x0600..0x06FF       // Perso-Arabic
            else -> return true
        }
        val scriptCount = text.count { it.code in unicodeRange }
        return scriptCount >= 4
    }

    /**
     * Translates any uncached CaseDossier items for the selected language using Gemini 3.8 Flash
     * and stores them in `translation_cache`.
     */
    suspend fun translateUncachedDossiersIfNeeded(
        dossiers: List<CaseDossier>,
        targetLanguage: AppLanguage,
        existingCache: Map<String, Map<String, String>>? = null
    ) = withContext(Dispatchers.IO) {
        if (targetLanguage == AppLanguage.ENGLISH) return@withContext

        val resolvedCache: Map<String, Map<String, String>> = existingCache ?: run {
            val entries = translationCacheDao.getTranslationsForLanguage(targetLanguage.code)
            entries.groupBy { it.contentId }
                .mapValues { (_, list) -> list.associate { it.field to it.text } }
        }

        val itemsToTranslate = dossiers.filter { dossier ->
            // Skip seed cases in Hindi because they already have human-verified translations
            if (targetLanguage == AppLanguage.HINDI &&
                SurakshaHindiSeedTranslations.hindiCaseMap.containsKey(dossier.id)
            ) {
                return@filter false
            }
            // Skip if already in target script (e.g., native Hindi Google News RSS items)
            if (isAlreadyInTargetScript(dossier.originalTitle, targetLanguage)) {
                return@filter false
            }
            // Skip if already cached in Room
            val cachedFields = resolvedCache[dossier.id]
            cachedFields == null || cachedFields["title"].isNullOrBlank()
        }.take(6) // Translate up to 6 uncached items per batch

        if (itemsToTranslate.isEmpty()) return@withContext

        try {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    responseSchema = translationSchema
                }
            )

            for (dossier in itemsToTranslate) {
                try {
                    val prompt = """
                        Translate the following Indian public-safety investigative news report into ${targetLanguage.englishName} (${targetLanguage.nativeName}, language code '${targetLanguage.code}').
                        Strict editorial rules:
                        1. Preserve every factual detail, date, number, statute section (e.g., POCSO, BNS), police station name, and court name accurately.
                        2. Keep a respectful, non-sensational journalistic tone.
                        3. Do not invent or omit any facts.
                        
                        Headline: ${dossier.originalTitle}
                        Summary: ${dossier.originalSummary}
                        Full Report: ${dossier.originalFullReport}
                    """.trimIndent()

                    val response = model.generateContent(prompt)
                    val responseText = response.text ?: continue
                    val parsed = json.decodeFromString<GeminiCaseTranslationResponse>(responseText)
                    if (parsed.title.isNotBlank() && parsed.summary.isNotBlank()) {
                        val now = System.currentTimeMillis()
                        translationCacheDao.upsertTranslations(
                            listOf(
                                TranslationCacheEntity(dossier.id, targetLanguage.code, "title", parsed.title.trim(), now),
                                TranslationCacheEntity(dossier.id, targetLanguage.code, "summary", parsed.summary.trim(), now),
                                TranslationCacheEntity(
                                    dossier.id,
                                    targetLanguage.code,
                                    "fullReport",
                                    parsed.fullReport.trim().ifBlank { parsed.summary.trim() },
                                    now
                                )
                            )
                        )
                    }
                } catch (_: Exception) {
                    // Continue translating remaining items; fallback to original text gracefully if offline/unconfigured
                }
            }
        } catch (_: Exception) {
            // Firebase AI not initialized in unit test environment; fallback gracefully
        }
    }
}
