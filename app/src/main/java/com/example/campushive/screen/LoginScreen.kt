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
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Airy Top Campus Hero with Modern Uni Campus Illustration
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
                                    .size(42.dp)
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

                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "Campus skills,\none tap away.",
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            lineHeight = 40.sp,
                            letterSpacing = (-0.6).sp
                        )
                    }
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

                    Spacer(Modifier.height(16.dp))

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
                                    .background(Honey),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Log in",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnHoneyAccent
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(Radius.Pill)
                                    .bounceClick(scaleDown = 0.96f) { onNavigateToSignUp() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sign up",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MutedText
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Email Field
                    Text(
                        text = "Email",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (emailError != null) DangerFgStrong else Charcoal
                    )
                    Spacer(Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        color = Cream,
                        border = BorderStroke(
                            width = if (emailError != null) 1.5.dp else 1.dp,
                            color = if (emailError != null) DangerFgStrong else Hairline
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            if (email.isEmpty()) {
                                Text(
                                    text = "@helplive.edu.my or @gmail.com",
                                    fontSize = 14.sp,
                                    color = MutedText.copy(alpha = 0.7f)
                                )
                            }
                            BasicTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    if (emailError != null) emailError = null
                                },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Charcoal
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                cursorBrush = SolidColor(Charcoal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    if (emailError != null) {
                        Text(
                            text = emailError!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = DangerFgStrong,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

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

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        color = Cream,
                        border = BorderStroke(
                            width = if (passwordError != null) 1.5.dp else 1.dp,
                            color = if (passwordError != null) DangerFgStrong else Hairline
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 10.dp, top = 11.dp, bottom = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (password.isEmpty()) {
                                    Text(
                                        text = "Min. 8 characters",
                                        fontSize = 14.sp,
                                        color = MutedText.copy(alpha = 0.7f)
                                    )
                                }
                                BasicTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        if (passwordError != null) passwordError = null
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Charcoal
                                    ),
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    cursorBrush = SolidColor(Charcoal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
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
                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = DangerFgStrong,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    // Primary Pill Login CTA
                    Button(
                        onClick = {
                            val trimmedEmail = email.trim()
                            emailError = when {
                                trimmedEmail.isEmpty() -> "Email is required"
                                !trimmedEmail.contains("@") || !trimmedEmail.contains(".") -> "Enter a valid email address"
                                else -> null
                            }
                            passwordError = when {
                                password.isEmpty() -> "Password is required"
                                else -> null
                            }
                            if (emailError == null && passwordError == null) {
                                authViewModel.login(trimmedEmail, password) {
                                    onLoginSuccess()
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
                                text = "Login",
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

                    Spacer(Modifier.height(16.dp))

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

                    Spacer(Modifier.height(16.dp))

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

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "Campus email or Gmail required",
                        fontSize = 11.sp,
                        color = MutedText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
