package com.example.data.remote

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.ContentItem
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    private val auth: FirebaseAuth get() = Firebase.auth
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun initAuthListener() {
        _currentUser.value = auth.currentUser
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun getFirestore(context: Context): FirebaseFirestore {
        val databaseId = context.getString(R.string.firestore_database_id)
        return FirebaseFirestore.getInstance(databaseId)
    }

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google")
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Google Sign-In configuration missing")
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        _currentUser.value = user
                        syncUserProfile(activity, user)
                        onSuccess(user)
                    } else {
                        onError("Failed to obtain signed-in user")
                    }
                } else {
                    onError("Unexpected credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In was cancelled or dismissed: ${e.message}", e)
                onError("Sign-in dismissed")
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Sign-in error")
            }
        }
    }

    fun signOut(context: Context, scope: CoroutineScope, onComplete: () -> Unit) {
        val credentialManager = CredentialManager.create(context)
        auth.signOut()
        _currentUser.value = null
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed clearing credential state", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    private suspend fun syncUserProfile(context: Context, user: FirebaseUser) {
        try {
            val db = getFirestore(context)
            val userDoc = db.collection("users").document(user.uid)
            val data = mapOf(
                "userId" to user.uid,
                "email" to (user.email ?: ""),
                "displayName" to (user.displayName ?: "Creator"),
                "credits" to 50,
                "tier" to "FREE",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDoc.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user profile: ${e.message}", e)
        }
    }

    suspend fun saveCreationToFirestore(context: Context, item: ContentItem): Result<String> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        try {
            val db = getFirestore(context)
            val creationsRef = db.collection("users").document(user.uid).collection("creations")
            val docRef = creationsRef.document()
            val payload = mapOf(
                "id" to docRef.id,
                "userId" to user.uid,
                "title" to item.title,
                "type" to item.type,
                "platform" to item.platform,
                "originalInput" to item.originalInput,
                "targetAudience" to item.targetAudience,
                "tone" to item.tone,
                "content" to item.content,
                "isFavorite" to item.isFavorite,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }
}
