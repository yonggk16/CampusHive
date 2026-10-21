package com.example.campushive.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = currentUser != null

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
                        _uiState.value = AuthUiState.Success("Account created successfully!")
                        onSuccess()
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
        onSignOutComplete()
    }

    fun clearUiState() {
        _uiState.value = AuthUiState.Idle
    }
}
