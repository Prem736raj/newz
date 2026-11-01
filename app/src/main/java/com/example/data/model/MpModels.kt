package com.example.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ParliamentHouse(val displayName: String) {
    LOK_SABHA("Lok Sabha"),
    RAJYA_SABHA("Rajya Sabha")
}

@Serializable
data class HouseMeta(
    val house: String = "",
    @SerialName("lok_sabha_term")
    val lokSabhaTerm: String? = null,
    @SerialName("as_of_date")
    val asOfDate: String = "",
    @SerialName("total_seats")
    val totalSeats: Int = 0,
    val sitting: Int = 0,
    val vacant: Int = 0,
    val sources: List<String> = emptyList()
) {
    val formattedAsOfDate: String
        get() = formatIsoDateNicely(asOfDate)
}

@Serializable
data class LokSabhaMpRecord(
    val id: Int = 0,
    @SerialName("mp_name")
    val mpName: String? = null,
    @SerialName("constituency_name")
    val constituencyName: String = "",
    @SerialName("constituency_number")
    val constituencyNumber: Int = 0,
    @SerialName("state_or_ut")
    val stateOrUt: String = "",
    val reservation: String? = null,
    val party: String? = null,
    @SerialName("party_short")
    val partyShort: String? = null,
    val gender: String? = null,
    val status: String = "sitting",
    @SerialName("source_url")
    val sourceUrl: String? = null,
    val notes: String? = null
) {
    val isVacant: Boolean
        get() = status.equals("vacant", ignoreCase = true) || mpName.isNullOrBlank()
}

@Serializable
data class RajyaSabhaMpRecord(
    val id: Int = 0,
    @SerialName("mp_name")
    val mpName: String? = null,
    @SerialName("state_or_ut_represented")
    val stateOrUtRepresented: String = "",
    val party: String? = null,
    @SerialName("party_short")
    val partyShort: String? = null,
    @SerialName("term_end_year")
    val termEndYear: Int? = null,
    val nominated: Boolean = false,
    @SerialName("source_url")
    val sourceUrl: String? = null,
    val notes: String? = null
) {
    val groupSectionName: String
        get() = if (nominated || stateOrUtRepresented.equals("Nominated", ignoreCase = true)) {
            "Nominated"
        } else {
            stateOrUtRepresented.ifBlank { "Other" }
        }
}

@Serializable
data class LokSabhaDataset(
    val meta: HouseMeta = HouseMeta(house = "Lok Sabha"),
    val mps: List<LokSabhaMpRecord> = emptyList()
)

@Serializable
data class RajyaSabhaDataset(
    val meta: HouseMeta = HouseMeta(house = "Rajya Sabha"),
    val mps: List<RajyaSabhaMpRecord> = emptyList()
)

sealed interface SelectedMpDetail {
    data class LokSabha(
        val record: LokSabhaMpRecord,
        val meta: HouseMeta
    ) : SelectedMpDetail

    data class RajyaSabha(
        val record: RajyaSabhaMpRecord,
        val meta: HouseMeta
    ) : SelectedMpDetail
}

fun formatIsoDateNicely(isoDate: String): String {
    val trimmed = isoDate.trim()
    if (trimmed.isEmpty()) return "Unknown date"
    return try {
        val parsed = LocalDate.parse(trimmed)
        parsed.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
    } catch (_: Exception) {
        // Fallback manual YYYY-MM-DD formatting for older API levels if needed
        val parts = trimmed.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val monthNum = parts[1].toIntOrNull()
            val day = parts[2].trimStart('0').ifEmpty { "1" }
            val monthShort = when (monthNum) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> null
            }
            if (monthShort != null) "$day $monthShort $year" else trimmed
        } else {
            trimmed
        }
    }
}
