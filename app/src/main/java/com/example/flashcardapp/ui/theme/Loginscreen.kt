package com.example.flashcardapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcardapp.util.FirebaseSync

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, Color(0xFF12122A)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Text("⚡", fontSize = 56.sp)
            Spacer(Modifier.height(8.dp))
            Text("FlashCard", fontSize = 32.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Text(
                if (isRegister) "Tạo tài khoản mới" else "Đăng nhập để đồng bộ",
                color = TextSecondary, fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMsg = "" },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = AccentPurple) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = Color(0xFF333355),
                    focusedLabelColor = AccentPurple,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = AccentPurple
                )
            )

            Spacer(Modifier.height(12.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMsg = "" },
                label = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = AccentPurple) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = Color(0xFF333355),
                    focusedLabelColor = AccentPurple,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary,
                    cursorColor = AccentPurple
                )
            )

            // Error message
            if (errorMsg.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, color = ColorAgain, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(24.dp))

            // Main button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMsg = "Vui lòng nhập đầy đủ email và mật khẩu!"
                        return@Button
                    }
                    isLoading = true
                    if (isRegister) {
                        FirebaseSync.signUp(email, password,
                            onSuccess = { isLoading = false; onLoginSuccess() },
                            onError = { isLoading = false; errorMsg = it }
                        )
                    } else {
                        FirebaseSync.signIn(email, password,
                            onSuccess = { isLoading = false; onLoginSuccess() },
                            onError = { isLoading = false; errorMsg = it }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (isRegister) "Đăng ký" else "Đăng nhập",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Switch mode
            TextButton(onClick = { isRegister = !isRegister; errorMsg = "" }) {
                Text(
                    if (isRegister) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký",
                    color = AccentPurple, fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Skip (dùng offline)
            TextButton(onClick = onLoginSuccess) {
                Text("Dùng offline (không đồng bộ)", color = TextHint, fontSize = 13.sp)
            }
        }
    }
}