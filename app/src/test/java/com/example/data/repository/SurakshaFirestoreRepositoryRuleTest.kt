package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.SavedCase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class SurakshaFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun sampleSavedCase(uid: String) = SavedCase(
        userId = uid,
        title = "Andhra Pradesh Class 10 Classroom Chainsaw Attack",
        summary = "21-year-old man attacked a Class 10 girl student with a mechanical chainsaw inside her classroom.",
        fullReport = "Comprehensive archive of the Andhra Pradesh school security breach and chainsaw attack on a Class 10 teenage student.",
        stateName = "Andhra Pradesh",
        districtName = "Anakapalli",
        cityName = "Elamanchili",
        mlaName = "Sundarapu Vijay Kumar (Elamanchili)",
        mpName = "C. M. Ramesh (Anakapalli)",
        victimCategory = "Teenage Student (13-19)",
        crimeCategory = "School/Campus Attack",
        caseStage = "CHARGESHEET_FILED",
        incidentDateLabel = "14 Aug 2026",
        personalNotes = "Saved for life to track fast-track court verdict."
    )

    @Test
    fun saveCaseForLife_and_observeSavedCases_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = SurakshaFirestoreRepository(firestore)
        val caseId = "case_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveCaseForLife(caseId, sampleSavedCase(aliceUid))
        }
        assertTrue("Expected saveCaseForLife to succeed, got ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

        val emittedCases = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSavedCases(aliceUid).first { list -> list.any { it.id == caseId } }
        }
        assertTrue(emittedCases.any { it.id == caseId && it.stateName == "Andhra Pradesh" })
    }

    @Test
    fun addCaseFollowUp_and_observeCaseFollowUps_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = SurakshaFirestoreRepository(firestore)
        val updateId = "upd_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addCaseFollowUp(
                updateId = updateId,
                caseId = "case_ap_school_01",
                caseTitle = "Andhra Pradesh Class 10 Classroom Chainsaw Attack",
                updateDateLabel = "Month 2 Follow-Up",
                stageBadge = "CHARGESHEET_FILED",
                headline = "Forensic and medical reports submitted to fast-track court",
                details = "Chargesheet filed under BNS and POCSO provisions; school security audit mandated across district."
            )
        }
        assertTrue("Expected addCaseFollowUp to succeed, got ${result.exceptionOrNull()}", result.isSuccess)

        val emittedUpdates = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCaseFollowUps(aliceUid).first { list -> list.any { it.id == updateId } }
        }
        assertTrue(emittedUpdates.any { it.id == updateId })
    }

    @Test
    fun getSavedCaseById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = SurakshaFirestoreRepository(firestore)
        val caseId = "case_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveCaseForLife(caseId, sampleSavedCase(aliceUid))
        }
        assertTrue(saveResult.isSuccess)

        signInTestUser(BOB_EMAIL)
        val bobRepo = SurakshaFirestoreRepository(firestore)
        val bobReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getSavedCaseById(aliceUid, caseId)
        }
        assertTrue(bobReadResult.isFailure)
        val firestoreEx = bobReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeSavedCases_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = SurakshaFirestoreRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeSavedCases(aliceUid).first()
            }
            fail("Expected exception for unauthenticated user")
        } catch (e: Exception) {
            val firestoreEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@suraksha.test"
        const val BOB_EMAIL = "bob@suraksha.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
