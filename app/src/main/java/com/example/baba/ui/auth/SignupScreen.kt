package com.example.baba.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.baba.R
import com.example.baba.ui.theme.*
import kotlinx.coroutines.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import com.example.baba.data.network.RetrofitInstance
import androidx.compose.foundation.clickable


@Composable
fun SignupScreen(
    onSignupComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var userIdCheckResult by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "정보를 입력해주세요",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(70.dp))

        // 이름
        Label("이름", Blue2)
        CustomField(
            value = name,
            onValueChange = { name = it },
            placeholder = "이름을 입력해주세요"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 이메일
        Label("이메일", Blue2)
        CustomField(
            value = email,
            onValueChange = { email = it },
            placeholder = "이메일을 입력해주세요"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 아이디
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "아이디",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Blue2
            )

            Spacer(modifier = Modifier.width(8.dp))

            userIdCheckResult?.let {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    color = if (it.contains("사용 가능")) Pink80 else Color.Red
                )
            }
        }

        CustomField(
            value = userId,
            onValueChange = {
                userId = it
                userIdCheckResult = null // 입력값 바뀌면 중복결과 리셋
            },
            placeholder = "아이디를 입력해주세요",
            trailingContent = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = RetrofitInstance.api.checkUsername(userId)
                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful && response.body() != null) {
                                        val isDuplicate = response.body()!!
                                        userIdCheckResult = if (isDuplicate) {
                                            "이미 사용 중인 아이디입니다."
                                        } else {
                                            "사용 가능한 아이디입니다."
                                        }
                                    } else {
                                        userIdCheckResult = "중복 확인 실패 (${response.code()})"
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    userIdCheckResult = "서버 오류: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue2,
                        contentColor = White
                    )
                ) {
                    Text("중복 확인", fontSize = 11.sp)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호
        Label("비밀번호", Blue2)
        CustomPWField(
            value = password,
            onValueChange = { password = it },
            placeholder = "비밀번호를 입력해주세요",
            visible = passwordVisible,
            onToggle = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 확인
        Label("비밀번호 확인", Blue2)
        CustomPWField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            placeholder = "비밀번호를 다시 입력해주세요",
            visible = passwordConfirmVisible,
            onToggle = { passwordConfirmVisible = !passwordConfirmVisible }
        )

        Spacer(modifier = Modifier.height(50.dp))

        val context = LocalContext.current

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.api.signup(
                            username = userId,
                            name = name,
                            password = password,
                            confirmPassword = passwordConfirm
                        )
                        if (response.isSuccessful && response.body() != null) {
                            val message = response.body()!!
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                onSignupComplete()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "회원가입 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
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
                text = "회원가입",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

    }
}

@Composable
fun Label(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = color,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun CustomField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable (() -> Unit))? = null
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 12.sp,
                            color = CoolGray500
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            color = TextBlack
                        ),
                        cursorBrush = SolidColor(Blue2),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = if (trailingContent != null) 80.dp else 0.dp)
                    )
                }
                trailingContent?.let {
                    Spacer(modifier = Modifier.width(4.dp))
                    it()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Blue2)
        )
    }
}

@Composable
fun CustomPWField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 12.sp,
                            color = CoolGray500
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            color = TextBlack
                        ),
                        cursorBrush = SolidColor(Blue2),
                        singleLine = true,
                        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 28.dp)
                    )
                }

                Icon(
                    painter = painterResource(id = if (visible) R.drawable.eye_on else R.drawable.eye_off),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onToggle() }
                        .padding(start = 4.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Blue2)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    MaterialTheme {
        SignupScreen(
            onSignupComplete = {}
        )
    }
}