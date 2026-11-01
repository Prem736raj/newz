package com.example

import com.example.data.local.SurakshaArchiveSeedData
import com.example.data.local.SurakshaDatabase
import com.example.data.model.CaseStage
import com.example.data.model.VerificationStatus
import com.example.data.remote.IndianCrimeNewsRssClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Prevent regressions where an uncertain report becomes an official claim. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EditorialSafetyRegressionTest {

    private fun fixture(
        url: String = "https://example.org/story",
        publicationDate: String = "Sun, 01 Nov 2026 10:00:00 GMT"
    ): String = """
        <rss version="2.0"><channel><item>
            <title>Women's community safety initiative - Example Publisher</title>
            <link>$url</link>
            <pubDate>$publicationDate</pubDate>
            <description>Community reporting on local safety policy.</description>
        </item></channel></rss>
    """.trimIndent()

    @Test
    fun unknownCaseStageNeverBecomesAFir() {
        assertEquals(CaseStage.STATUS_UNVERIFIED, CaseStage.fromCode("UNRECOGNISED"))
        assertEquals(CaseStage.STATUS_UNVERIFIED, CaseStage.fromCode(""))
        assertEquals(CaseStage.FIR_FILED, CaseStage.fromCode("FIR_FILED"))
    }

    @Test
    fun rssNeverInventsIncidentDateFirOrPublisherVerification() {
        val report = IndianCrimeNewsRssClient().parseRssXml(fixture(), null).single()
        assertEquals(CaseStage.STATUS_UNVERIFIED, report.caseStage)
        assertEquals(0L, report.incidentEpochMillis)
        assertEquals("Incident date unverified", report.incidentDateLabel)
        assertNotNull(report.publishedAtEpochMillis)
        assertEquals("Publisher not specified", report.sourceName)
        assertEquals(VerificationStatus.REPORTED_UNCONFIRMED, report.verificationStatus)
        assertEquals("Affected group and age unverified", report.victimCategory)
        assertTrue(report.relevantLaws.isEmpty())
    }

    @Test
    fun invalidPublicationDateStaysUnknownInsteadOfUsingCurrentClock() {
        val report = IndianCrimeNewsRssClient().parseRssXml(fixture(publicationDate = "not-a-date"), null).single()
        assertNull(report.publishedAtEpochMillis)
        assertEquals(0L, report.incidentEpochMillis)
    }

    @Test
    fun unsafePublisherLinksAreNotPresentedAsEvidence() {
        val report = IndianCrimeNewsRssClient().parseRssXml(fixture(url = "javascript:alert(1)"), null).single()
        assertEquals("", report.sourceUrl)
        assertEquals("", report.timelineUpdates.first().sourceReferenceUrl)
    }

    @Test
    fun rssLocationGuessDoesNotSupplyRepresentativeIdentity() {
        val xml = fixture().replace(
            "Community reporting on local safety policy.",
            "Community reporting from Elamanchili on local safety policy."
        )
        val report = IndianCrimeNewsRssClient().parseRssXml(xml, null).single()
        assertEquals("Not verified", report.mlaName)
        assertEquals("Not verified", report.mpName)
    }

    @Test
    fun accountStorageNamesAreDistinctAndDoNotRevealUserIds() {
        val a = SurakshaDatabase.databaseNameForUser("test-account-a")
        val b = SurakshaDatabase.databaseNameForUser("test-account-b")
        assertNotEquals(a, b)
        assertEquals(a, SurakshaDatabase.databaseNameForUser("test-account-a"))
        assertFalse(a.contains("test-account-a"))
    }

    @Test
    fun unsourcedExampleDossiersAreNotInPublishedFeed() {
        assertTrue(SurakshaArchiveSeedData.buildPublishedCaseDossiers().isEmpty())
    }
}
