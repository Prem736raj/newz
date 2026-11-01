package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CaseFollowUp
import com.example.data.model.SavedCase
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SurakshaFirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeSavedCases(userId: String = requireUserId()): Flow<List<SavedCase>> = flow {
        val path = "users/$userId/saved_cases"
        emitAll(
            db.collection("users").document(userId).collection("saved_cases")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SavedCase::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeCaseFollowUps(userId: String = requireUserId()): Flow<List<CaseFollowUp>> = flow {
        val path = "users/$userId/case_updates"
        emitAll(
            db.collection("users").document(userId).collection("case_updates")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CaseFollowUp::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveCaseForLife(caseId: String, savedCase: SavedCase): Result<String> {
        val uid = requireUserId()
        val safeDocId = caseId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120).ifBlank { "case_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("saved_cases").document(safeDocId)
        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "title" to savedCase.title.take(300),
            "summary" to savedCase.summary.take(1000),
            "fullReport" to savedCase.fullReport.take(10000),
            "stateName" to savedCase.stateName.take(100),
            "districtName" to savedCase.districtName.take(100),
            "cityName" to savedCase.cityName.take(100),
            "mlaName" to savedCase.mlaName.take(200),
            "mpName" to savedCase.mpName.take(200),
            "victimCategory" to savedCase.victimCategory.take(100),
            "crimeCategory" to savedCase.crimeCategory.take(100),
            "caseStage" to savedCase.caseStage.take(100),
            "incidentDateLabel" to savedCase.incidentDateLabel.take(100),
            "createdAt" to (savedCase.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!savedCase.personalNotes.isNullOrBlank()) {
            payload["personalNotes"] = savedCase.personalNotes.take(2000)
        }
        return try {
            docRef.set(payload).await()
            Result.success(safeDocId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getSavedCaseById(targetUserId: String, caseId: String): Result<SavedCase?> {
        val docRef = db.collection("users").document(targetUserId).collection("saved_cases").document(caseId)
        return try {
            val snap = docRef.get().await()
            Result.success(
                snap.toObject(
                    SavedCase::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun removeSavedCase(caseId: String): Result<Unit> {
        val uid = requireUserId()
        val safeDocId = caseId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
        val docRef = db.collection("users").document(uid).collection("saved_cases").document(safeDocId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun addCaseFollowUp(
        updateId: String,
        caseId: String,
        caseTitle: String,
        updateDateLabel: String,
        stageBadge: String,
        headline: String,
        details: String
    ): Result<String> {
        val uid = requireUserId()
        val safeUpdateId = updateId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120).ifBlank { "upd_${System.currentTimeMillis()}" }
        val safeCaseId = caseId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
        val docRef = db.collection("users").document(uid).collection("case_updates").document(safeUpdateId)
        val payload = mapOf(
            "userId" to uid,
            "caseId" to safeCaseId,
            "caseTitle" to caseTitle.take(300),
            "updateDateLabel" to updateDateLabel.take(100),
            "stageBadge" to stageBadge.take(100),
            "headline" to headline.take(300),
            "details" to details.take(5000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(safeUpdateId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
