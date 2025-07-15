package com.example.baba.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.example.baba.ui.theme.*
import kotlinx.coroutines.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.baba.data.auth.LoginRequest
import com.example.baba.data.network.RetrofitInstance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit
) {
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var saveId by remember { mutableStateOf(false) }
    var autoLogin by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("로그인", fontSize = 30.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(60.dp))

        OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            placeholder = {
                Text(
                    text = "아이디를 입력해주세요",
                    fontSize = 12.sp,
                    color = CoolGray500
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = CoolGray500,
                unfocusedBorderColor = CoolGray500,
                focusedPlaceholderColor = CoolGray500,
                unfocusedPlaceholderColor = CoolGray500,
                focusedTextColor = TextBlack,
                unfocusedTextColor = TextBlack,
                cursorColor = CoolGray500
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = {
                Text(
                    text = "비밀번호를 입력해주세요",
                    fontSize = 12.sp,
                    color = CoolGray500
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = CoolGray500,
                unfocusedBorderColor = CoolGray500,
                focusedPlaceholderColor = CoolGray500,
                unfocusedPlaceholderColor = CoolGray500,
                focusedTextColor = TextBlack,
                unfocusedTextColor = TextBlack,
                cursorColor = CoolGray500
            )

        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                "아이디를 잊으셨나요?",
                fontSize = 12.sp,
                color = Blue3,
                textDecoration = TextDecoration.Underline
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.authApi.login(LoginRequest(id, password))
                        if (response.isSuccessful && response.body() != null) {
                            val message = response.body() ?: "응답 없음"
                            if (message == "로그인 성공") {
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "로그인 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "서버 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue2,
                contentColor = White
            )
        ) {
            Text(
                text = "로그인하기",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { onSignupClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.dp, Blue2),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Blue2
            )
        ) {
            Text(
                text = "회원가입하기",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveId,
                    onCheckedChange = { saveId = it },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "아이디 저장",
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = autoLogin,
                    onCheckedChange = { autoLogin = it },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "자동 로그인",
                    fontSize = 12.sp
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            onLoginSuccess = {},
            onSignupClick = {}
        )
    }
}