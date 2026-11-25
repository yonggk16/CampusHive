@file:Suppress("DEPRECATION")

package com.example.campushive.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.ui.text.style.TextAlign
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

private enum class AuthSheetStep {
    OPTIONS,
    EMAIL_LOGIN,
    EMAIL_SIGNUP
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigateToSignUp: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

    var sheetStep by remember { mutableStateOf(AuthSheetStep.OPTIONS) }

    // Shared / Login & Sign Up form states
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

    val hasMinLength = password.length >= 8
    val hasUpperAndLower = password.any { it.isUpperCase() } && password.any { it.isLowerCase() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }

    // System back returns from email form to the 2-option welcome sheet
    BackHandler(enabled = sheetStep != AuthSheetStep.OPTIONS) {
        sheetStep = AuthSheetStep.OPTIONS
        nameError = null
        emailError = null
        passwordError = null
        confirmPasswordError = null
    }

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
                    onLoginSuccess()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HoneyBottomGradient)
    ) {
        // Keep campus illustration full-bleed & stationary so the sheet glides smoothly upward over it
        CampusEnvironmentBackground(
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Airy Top Campus Hero Header + Headline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
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
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp)),
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

                    Column(modifier = Modifier.padding(bottom = 10.dp)) {
                        Text(
                            text = when (sheetStep) {
                                AuthSheetStep.OPTIONS -> "Campus skills,\none tap away."
                                AuthSheetStep.EMAIL_LOGIN -> "Welcome back\nto CampusHive."
                                AuthSheetStep.EMAIL_SIGNUP -> "Create your\nstudent account."
                            },
                            fontSize = if (sheetStep == AuthSheetStep.OPTIONS) 35.sp else 31.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            lineHeight = if (sheetStep == AuthSheetStep.OPTIONS) 40.sp else 36.sp,
                            letterSpacing = (-0.6).sp
                        )
                    }
                }
            }

            // 2. Anchored White Bottom Sheet (crisp single-spring rise-up between steps)
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                                .clip(CircleShape)
                                .background(Hairline)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    AnimatedContent(
                        targetState = sheetStep,
                        transitionSpec = {
                            val goingUp = targetState.ordinal > initialState.ordinal
                            val enter = slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) { fullHeight -> if (goingUp) fullHeight / 5 else -fullHeight / 8 } +
                                fadeIn(animationSpec = tween(durationMillis = 130))
                            val exit = fadeOut(animationSpec = tween(durationMillis = 80))
                            (enter togetherWith exit).using(
                                SizeTransform(clip = true) { _, _ ->
                                    spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                }
                            )
                        },
                        label = "auth_sheet_rise"
                    ) { step ->
                        when (step) {
                            AuthSheetStep.OPTIONS -> {
                                AuthOptionsStep(
                                    isLoading = isLoading,
                                    onContinueWithGoogle = {
                                        googleSignInClient.signOut()
                                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                    },
                                    onContinueWithEmail = {
                                        sheetStep = AuthSheetStep.EMAIL_LOGIN
                                    }
                                )
                            }

                            AuthSheetStep.EMAIL_LOGIN -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    EmailStepHeader(
                                        isLoginSelected = true,
                                        onBack = {
                                            sheetStep = AuthSheetStep.OPTIONS
                                            emailError = null
                                            passwordError = null
                                        },
                                        onSelectLogin = {},
                                        onSelectSignUp = {
                                            sheetStep = AuthSheetStep.EMAIL_SIGNUP
                                            emailError = null
                                            passwordError = null
                                        }
                                    )

                                    Spacer(Modifier.height(18.dp))

                                    // Email Field
                                    AuthInputField(
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

                                    Spacer(Modifier.height(14.dp))

                                    // Password Field + Forgot Password
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Password",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (passwordError != null) DangerFgStrong else Charcoal
                                        )
                                        Text(
                                            text = "Forgot password?",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Charcoal,
                                            modifier = Modifier.bounceClick {
                                                if (email.isBlank()) {
                                                    emailError = "Enter your email first to reset your password"
                                                } else {
                                                    emailError = null
                                                    authViewModel.sendPasswordReset(email)
                                                }
                                            }
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))

                                    AuthPasswordField(
                                        value = password,
                                        onValueChange = {
                                            password = it
                                            if (passwordError != null) passwordError = null
                                        },
                                        placeholder = "Min. 8 characters",
                                        isPasswordVisible = isPasswordVisible,
                                        onToggleVisibility = { isPasswordVisible = !isPasswordVisible },
                                        errorText = passwordError
                                    )

                                    Spacer(Modifier.height(20.dp))

                                    // Primary Pill Login CTA
                                    AuthPrimaryButton(
                                        label = "Log in",
                                        isLoading = isLoading,
                                        onClick = {
                                            val trimmedEmail = email.trim()
                                            emailError = when {
                                                trimmedEmail.isEmpty() -> "Email is required"
                                                !trimmedEmail.contains("@") || !trimmedEmail.contains(".") ->
                                                    "Enter a valid email address"
                                                else -> null
                                            }
                                            passwordError = if (password.isEmpty()) "Password is required" else null
                                            if (emailError == null && passwordError == null) {
                                                authViewModel.login(trimmedEmail, password) {
                                                    onLoginSuccess()
                                                }
                                            }
                                        }
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    Text(
                                        text = "Campus email (@helplive.edu.my) or Gmail required",
                                        fontSize = 11.sp,
                                        color = MutedText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(Modifier.height(10.dp))
                                }
                            }

                            AuthSheetStep.EMAIL_SIGNUP -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    EmailStepHeader(
                                        isLoginSelected = false,
                                        onBack = {
                                            sheetStep = AuthSheetStep.OPTIONS
                                            nameError = null
                                            emailError = null
                                            passwordError = null
                                            confirmPasswordError = null
                                        },
                                        onSelectLogin = {
                                            sheetStep = AuthSheetStep.EMAIL_LOGIN
                                            nameError = null
                                            emailError = null
                                            passwordError = null
                                            confirmPasswordError = null
                                        },
                                        onSelectSignUp = {}
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    AuthInputField(
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

                                    AuthInputField(
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

                                    Text(
                                        text = "Password",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (passwordError != null) DangerFgStrong else Charcoal
                                    )
                                    Spacer(Modifier.height(6.dp))

                                    AuthPasswordField(
                                        value = password,
                                        onValueChange = {
                                            password = it
                                            if (passwordError != null) passwordError = null
                                        },
                                        placeholder = "Min. 8 characters",
                                        isPasswordVisible = isPasswordVisible,
                                        onToggleVisibility = { isPasswordVisible = !isPasswordVisible },
                                        errorText = passwordError
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        AuthPasswordCheckPill(label = "8+ chars", met = hasMinLength)
                                        AuthPasswordCheckPill(label = "Upper & lower", met = hasUpperAndLower)
                                        AuthPasswordCheckPill(label = "Symbol", met = hasSymbol)
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Text(
                                        text = "Confirm password",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (confirmPasswordError != null) DangerFgStrong else Charcoal
                                    )
                                    Spacer(Modifier.height(6.dp))

                                    AuthPasswordField(
                                        value = confirmPassword,
                                        onValueChange = {
                                            confirmPassword = it
                                            if (confirmPasswordError != null) confirmPasswordError = null
                                        },
                                        placeholder = "Re-enter your password",
                                        isPasswordVisible = isConfirmPasswordVisible,
                                        onToggleVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                                        errorText = confirmPasswordError
                                    )

                                    Spacer(Modifier.height(18.dp))

                                    AuthPrimaryButton(
                                        label = "Create account",
                                        isLoading = isLoading,
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
                                                    onLoginSuccess()
                                                }
                                            }
                                        }
                                    )

                                    Spacer(Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthOptionsStep(
    isLoading: Boolean,
    onContinueWithGoogle: () -> Unit,
    onContinueWithEmail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Get started",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            letterSpacing = (-0.3).sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Log in or create an account to browse and offer campus services.",
            fontSize = 13.sp,
            color = MutedText,
            lineHeight = 19.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(26.dp))

        // Option 1: Continue with Google
        OutlinedButton(
            onClick = onContinueWithGoogle,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(scaleDown = 0.97f),
            shape = Radius.Pill,
            border = BorderStroke(1.dp, Hairline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CardWhite,
                contentColor = Charcoal
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Charcoal,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Continue with Google",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Charcoal
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Option 2: Continue with email
        Button(
            onClick = onContinueWithEmail,
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
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Continue with email",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun EmailStepHeader(
    isLoginSelected: Boolean,
    onBack: () -> Unit,
    onSelectLogin: () -> Unit,
    onSelectSignUp: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = CreamTint,
            border = BorderStroke(1.dp, Hairline),
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .bounceClick(scaleDown = 0.94f) { onBack() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to sign-in options",
                    tint = Charcoal,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Surface(
            shape = Radius.Pill,
            color = CreamTint,
            border = BorderStroke(1.dp, Hairline),
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
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
                        .background(if (isLoginSelected) Honey else Color.Transparent)
                        .then(
                            if (!isLoginSelected) {
                                Modifier.bounceClick(scaleDown = 0.96f) { onSelectLogin() }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log in",
                        fontSize = 14.sp,
                        fontWeight = if (isLoginSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isLoginSelected) OnHoneyAccent else MutedText
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(Radius.Pill)
                        .background(if (!isLoginSelected) Honey else Color.Transparent)
                        .then(
                            if (isLoginSelected) {
                                Modifier.bounceClick(scaleDown = 0.96f) { onSelectSignUp() }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign up",
                        fontSize = 14.sp,
                        fontWeight = if (!isLoginSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (!isLoginSelected) OnHoneyAccent else MutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (errorText != null) DangerFgStrong else Charcoal
        )
        Spacer(Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Cream,
            border = BorderStroke(
                width = if (errorText != null) 1.5.dp else 1.dp,
                color = if (errorText != null) DangerFgStrong else Hairline
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = MutedText.copy(alpha = 0.7f)
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
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    cursorBrush = SolidColor(Charcoal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    errorText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Cream,
            border = BorderStroke(
                width = if (errorText != null) 1.5.dp else 1.dp,
                color = if (errorText != null) DangerFgStrong else Hairline
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 10.dp, top = 11.dp, bottom = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = MutedText.copy(alpha = 0.7f)
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
                        visualTransformation = if (isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        cursorBrush = SolidColor(Charcoal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(
                    onClick = onToggleVisibility,
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
private fun AuthPrimaryButton(
    label: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
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
                text = label,
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
}

@Composable
private fun AuthPasswordCheckPill(
    label: String,
    met: Boolean
) {
    Surface(
        shape = RoundedCornerShape(50),
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
