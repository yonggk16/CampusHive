@file:Suppress("DEPRECATION")

package com.example.campushive.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
        // Campus Environment Background Vector Illustration
        Image(
            painter = painterResource(id = R.drawable.campus_environment),
            contentDescription = "Campus Environment",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
        Spacer(Modifier.weight(1f))

        // Top Header Row: Logo on Left + HELP University Subang 2 Location Tag on Top Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "CampusHive Logo",
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Fit
            )

            // Location Tag: HELP University Subang 2
            Surface(
                color = CardWhite,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Charcoal,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "HELP University Subang 2",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Small Brand Header
        Text(
            text = "WELCOME TO THE HIVE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MutedText,
            letterSpacing = 0.8.sp
        )

        Spacer(Modifier.height(6.dp))

        // Headline
        Text(
            text = "Campus skills,\none tap away.",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            lineHeight = 34.sp
        )

        Spacer(Modifier.height(32.dp))

        // UNIVERSITY EMAIL Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "EMAIL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (email.isEmpty()) {
                        Text(
                            text = "@helplive.edu.my or @gmail.com",
                            fontSize = 15.sp,
                            color = MutedText.copy(alpha = 0.5f)
                        )
                    }
                    BasicTextField(
                        value = email,
                        onValueChange = { email = it },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Charcoal
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        cursorBrush = SolidColor(Honey),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // PASSWORD Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PASSWORD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 0.8.sp
                    )

                    Text(
                        text = "Forgot password?",
                        fontSize = 12.sp,
                        color = MutedText,
                        modifier = Modifier.clickable {
                            authViewModel.sendPasswordReset(email)
                        }
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (password.isEmpty()) {
                            Text(
                                text = "Min. 8 characters",
                                fontSize = 15.sp,
                                color = MutedText.copy(alpha = 0.5f)
                            )
                        }
                        BasicTextField(
                            value = password,
                            onValueChange = { password = it },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Charcoal
                            ),
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            cursorBrush = SolidColor(Honey),
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // Full-width Login Button
        Button(
            onClick = {
                authViewModel.login(email, password) {
                    onLoginSuccess()
                }
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .bounceClick(scaleDown = 0.96f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = CardWhite,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CardWhite
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = CardWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Create an account Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                fontSize = 13.sp,
                color = MutedText
            )
            Text(
                text = "Sign up",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }

        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = {
                googleSignInClient.signOut()
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .bounceClick(scaleDown = 0.96f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Hairline),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = CardWhite, contentColor = Charcoal)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text("Continue with Google", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Charcoal)
        }

        Spacer(Modifier.weight(1f))

        // Footer Notice
        Text(
            text = "Campus email or Gmail required • Privacy notice",
            fontSize = 11.sp,
            color = MutedText,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}
}
