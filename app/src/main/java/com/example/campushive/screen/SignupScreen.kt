@file:Suppress("DEPRECATION")

package com.example.campushive.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.R
import com.example.campushive.components.CampusEnvironmentBackground
import com.example.campushive.components.CampusLocationIndicator
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.AuthUiState
import com.example.campushive.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel = viewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val isLoading = uiState is AuthUiState.Loading

    val webClientId = "1052933591746-mmdnqdaikfeqdeiqh15gdehkell39c19.apps.googleusercontent.com"

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                authViewModel.signInWithGoogle(idToken) {
                    onSignUpSuccess()
                }
            } else {
                Toast.makeText(context, "Failed to retrieve Google token", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Google Sign-In failed or cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Observe UiState for messages
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.clearUiState()
            }
            is AuthUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                authViewModel.clearUiState()
            }
            else -> {}
        }
    }

    val hasMinLength = password.length >= 8
    val hasUpperAndLower = password.any { it.isUpperCase() } && password.any { it.isLowerCase() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HoneyBottomGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Compact Top Campus Hero with Modern Uni Campus Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                CampusEnvironmentBackground(
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = Spacing.gutter, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "CampusHive Logo",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = "CampusHive",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal,
                                letterSpacing = (-0.3).sp
                            )
                        }

                        CampusLocationIndicator(campusName = "HELP Subang 2 Campus")
                    }

                    Text(
                        text = "Create your\nstudent account",
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // 2. Anchored White Bottom Form Sheet (hugs content cleanly — zero bottom gap)
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, Hairline),
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    // Subtle Sheet Handle
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(4.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Hairline)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Segmented Log in | Sign up Toggle Pill
                    Surface(
                        shape = Radius.Pill,
                        color = CreamTint,
                        border = BorderStroke(1.dp, Hairline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(Radius.Pill)
                                    .bounceClick(scaleDown = 0.96f) { onNavigateToLogin() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Log in",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MutedText
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(Radius.Pill)
                                    .background(Honey),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sign up",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnHoneyAccent
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Full Name Field
                    CompactAuthField(
                        label = "Full name",
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (nameError != null) nameError = null
                        },
                        placeholder = "Ahmad Ali",
                        errorText = nameError
                    )

                    Spacer(Modifier.height(12.dp))

                    // Email Field
                    CompactAuthField(
                        label = "Email",
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) emailError = null
                        },
                        placeholder = "@helplive.edu.my or @gmail.com",
                        keyboardType = KeyboardType.Email,
                        errorText = emailError
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password Field
                    CompactAuthField(
                        label = "Password",
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) passwordError = null
                        },
                        placeholder = "Min. 8 characters",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        isPasswordVisible = isPasswordVisible,
                        onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                        errorText = passwordError
                    )

                    Spacer(Modifier.height(8.dp))

                    // Live Password Checklist Pills
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PasswordCheckPill(label = "8+ chars", met = hasMinLength)
                        PasswordCheckPill(label = "Upper & lower", met = hasUpperAndLower)
                        PasswordCheckPill(label = "Symbol", met = hasSymbol)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Confirm Password Field
                    CompactAuthField(
                        label = "Confirm password",
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (confirmPasswordError != null) confirmPasswordError = null
                        },
                        placeholder = "Re-enter your password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        isPasswordVisible = isConfirmPasswordVisible,
                        onTogglePasswordVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                        errorText = confirmPasswordError
                    )

                    Spacer(Modifier.height(18.dp))

                    // Primary Pill Create Account CTA
                    Button(
                        onClick = {
                            val trimmedName = fullName.trim()
                            val trimmedEmail = email.trim().lowercase()

                            nameError = if (trimmedName.isEmpty()) "Full name is required" else null
                            emailError = when {
                                trimmedEmail.isEmpty() -> "Email is required"
                                !trimmedEmail.endsWith("@helplive.edu.my") && !trimmedEmail.endsWith("@gmail.com") ->
                                    "Use a @helplive.edu.my or @gmail.com email"
                                else -> null
                            }
                            passwordError = when {
                                password.isEmpty() -> "Password is required"
                                !hasMinLength || !hasUpperAndLower || !hasSymbol ->
                                    "Meet all password checks below"
                                else -> null
                            }
                            confirmPasswordError = when {
                                confirmPassword.isEmpty() -> "Please confirm your password"
                                confirmPassword != password -> "Passwords do not match"
                                else -> null
                            }

                            if (nameError == null && emailError == null && passwordError == null && confirmPasswordError == null) {
                                authViewModel.signUp(trimmedName, trimmedEmail, password, confirmPassword) {
                                    onSignUpSuccess()
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = Radius.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Honey,
                            contentColor = OnHoneyAccent,
                            disabledContainerColor = CreamTint,
                            disabledContentColor = MutedText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .bounceClick(scaleDown = 0.97f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OnHoneyAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Clean "or" Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Hairline, thickness = 0.8.dp)
                        Text(
                            text = "or",
                            fontSize = 12.sp,
                            color = MutedText,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Hairline, thickness = 0.8.dp)
                    }

                    Spacer(Modifier.height(14.dp))

                    // Google Sign-In Pill Button
                    OutlinedButton(
                        onClick = {
                            googleSignInClient.signOut()
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .bounceClick(scaleDown = 0.97f),
                        shape = Radius.Pill,
                        border = BorderStroke(1.dp, Hairline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardWhite,
                            contentColor = Charcoal
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Charcoal
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun CompactAuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    errorText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            color = Cream,
            border = BorderStroke(
                width = if (errorText != null) 1.5.dp else 1.dp,
                color = if (errorText != null) DangerFgStrong else Hairline
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (errorText != null) DangerFgStrong else MutedText
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                color = MutedText.copy(alpha = 0.65f)
                            )
                        }
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Charcoal
                            ),
                            visualTransformation = if (isPassword && !isPasswordVisible) {
                                PasswordVisualTransformation()
                            } else {
                                VisualTransformation.None
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                            cursorBrush = SolidColor(Charcoal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (isPassword && onTogglePasswordVisibility != null) {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = MutedText,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        if (errorText != null) {
            Text(
                text = errorText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = DangerFgStrong,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun PasswordCheckPill(
    label: String,
    met: Boolean
) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        color = if (met) SuccessBg else NeutralBg,
        border = BorderStroke(1.dp, if (met) SuccessBorder else Hairline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (met) "✓" else "○",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (met) SuccessFg else MutedText
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (met) FontWeight.SemiBold else FontWeight.Medium,
                color = if (met) SuccessFg else MutedText
            )
        }
    }
}
