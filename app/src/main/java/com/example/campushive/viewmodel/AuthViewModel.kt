package com.example.campushive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushive.data.FirestoreRepository
import com.example.campushive.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = currentUser != null

    val isEmailVerified: Boolean
        get() = currentUser?.isEmailVerified == true

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val user = currentUser
        if (user != null) {
            viewModelScope.launch {
                repository.getUserProfileFlow(user.uid).collect { profile ->
                    if (profile != null) {
                        _userProfile.value = profile
                    } else {
                        // Create a default initial user profile state if not found in Firestore yet
                        _userProfile.value = UserProfile(
                            uid = user.uid,
                            displayName = user.displayName.orEmpty().ifBlank { "Student User" },
                            email = user.email.orEmpty(),
                            isVerifiedStudent = user.isEmailVerified
                        )
                    }
                }
            }
        } else {
            _userProfile.value = null
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your student email and password.")
            return
        }

        _uiState.value = AuthUiState.Loading
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = AuthUiState.Success("Welcome back!")
                    fetchUserProfile()
                    onSuccess()
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Login failed. Check your credentials."
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            }
    }

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all required fields.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Passwords do not match.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }

        _uiState.value = AuthUiState.Loading
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName.trim())
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                        // Automatically send email verification link
                        user.sendEmailVerification()

                        // Initialize UserProfile document in Firestore
                        val initialProfile = UserProfile(
                            uid = user.uid,
                            displayName = fullName.trim(),
                            email = email.trim(),
                            isVerifiedStudent = false
                        )
                        viewModelScope.launch {
                            repository.saveUserProfile(initialProfile)
                            _uiState.value = AuthUiState.Success("Account created! A verification email has been sent.")
                            fetchUserProfile()
                            onSuccess()
                        }
                    } ?: run {
                        _uiState.value = AuthUiState.Success("Account created successfully!")
                        onSuccess()
                    }
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Sign up failed."
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            }
    }

    fun sendEmailVerification(onComplete: ((Boolean, String) -> Unit)? = null) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AuthUiState.Error("No logged-in user found.")
            onComplete?.invoke(false, "No logged-in user found.")
            return
        }

        _uiState.value = AuthUiState.Loading
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val msg = "Verification email sent to ${user.email}. Check your inbox or spam folder."
                _uiState.value = AuthUiState.Success(msg)
                onComplete?.invoke(true, msg)
            } else {
                val err = task.exception?.localizedMessage ?: "Failed to send verification email."
                _uiState.value = AuthUiState.Error(err)
                onComplete?.invoke(false, err)
            }
        }
    }

    fun reloadUserVerificationStatus(onComplete: ((Boolean, Boolean) -> Unit)? = null) {
        val user = auth.currentUser
        if (user == null) {
            onComplete?.invoke(false, false)
            return
        }

        user.reload().addOnCompleteListener { task ->
            val isVerified = auth.currentUser?.isEmailVerified == true
            if (isVerified) {
                val current = _userProfile.value ?: UserProfile(uid = user.uid, email = user.email ?: "")
                val updated = current.copy(isVerifiedStudent = true)
                viewModelScope.launch {
                    repository.saveUserProfile(updated)
                }
            }
            onComplete?.invoke(task.isSuccessful, isVerified)
        }
    }

    fun updateUserProfile(
        displayName: String,
        bio: String,
        campusLocation: String,
        facultyOrMajor: String,
        phoneNumber: String,
        onComplete: (Boolean) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AuthUiState.Error("No user logged in.")
            onComplete(false)
            return
        }

        _uiState.value = AuthUiState.Loading

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName.trim())
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val current = _userProfile.value ?: UserProfile(
                    uid = user.uid,
                    email = user.email.orEmpty()
                )
                val updated = current.copy(
                    uid = user.uid,
                    displayName = displayName.trim(),
                    email = user.email.orEmpty(),
                    bio = bio.trim(),
                    campusLocation = campusLocation.trim(),
                    facultyOrMajor = facultyOrMajor.trim(),
                    phoneNumber = phoneNumber.trim(),
                    isVerifiedStudent = user.isEmailVerified
                )

                viewModelScope.launch {
                    val saved = repository.saveUserProfile(updated)
                    if (saved) {
                        _uiState.value = AuthUiState.Success("Profile updated successfully!")
                        onComplete(true)
                    } else {
                        _uiState.value = AuthUiState.Error("Failed to save profile details.")
                        onComplete(false)
                    }
                }
            } else {
                val err = task.exception?.localizedMessage ?: "Failed to update profile."
                _uiState.value = AuthUiState.Error(err)
                onComplete(false)
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address first.")
            return
        }

        _uiState.value = AuthUiState.Loading
        auth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = AuthUiState.Success("Password reset email sent to ${email.trim()}")
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Failed to send reset email."
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            }
    }

    fun signOut(onSignOutComplete: () -> Unit) {
        auth.signOut()
        _uiState.value = AuthUiState.Idle
        _userProfile.value = null
        onSignOutComplete()
    }

    fun clearUiState() {
        _uiState.value = AuthUiState.Idle
    }
}
