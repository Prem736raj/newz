package com.example.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

/**
 * Diagnostics intentionally exclude UIDs, emails, document IDs, report contents
 * and exception messages: those can all contain private user information.
 * An error category is enough for production troubleshooting.
 */
fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val operationArea = when {
        path?.contains("/saved_cases/") == true -> "saved_cases"
        path?.contains("/case_updates/") == true -> "case_updates"
        else -> "unspecified"
    }
    val errorInfoJson = JSONObject().apply {
        put("operationType", operationType.value)
        put("operationArea", operationArea)
        put("errorClass", exception.javaClass.simpleName)
        put("firestoreCode", (exception as? FirebaseFirestoreException)?.code?.name ?: "UNKNOWN")
        put("authenticated", FirebaseAuth.getInstance().currentUser != null)
    }
    val safeJson = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore operation failed: $safeJson")
    return safeJson
}
