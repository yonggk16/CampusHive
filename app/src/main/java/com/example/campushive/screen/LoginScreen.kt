package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.R
import com.example.campushive.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("you@student.uni.edu.my") }
    var password by remember { mutableStateOf("••••••••") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(10.dp))

        // App Logo
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "CampusHive Logo",
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Fit
        )

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
                    text = "UNIVERSITY EMAIL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(6.dp))
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
                            Toast.makeText(context, "Password reset link sent to your email", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Spacer(Modifier.height(6.dp))

                BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Charcoal
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    cursorBrush = SolidColor(Honey),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Login Button (Honey pill with arrow ->)
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please enter your student email and password", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            modifier = Modifier.wrapContentWidth()
        ) {
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

        Spacer(Modifier.height(20.dp))

        // Create an account Link
        Text(
            text = "Create an account",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            modifier = Modifier.clickable { onNavigateToSignUp() }
        )

        Spacer(Modifier.weight(1f))

        // Footer Notice
        Text(
            text = "New accounts require a campus email • Privacy notice",
            fontSize = 11.sp,
            color = MutedText,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}
