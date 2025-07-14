package com.example.baba.ui.auth

import androidx.compose.foundation.background
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

@Composable
fun SignupScreen(
    onSignupComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

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
        SignupTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "이름을 입력해주세요"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 아이디
        Label("아이디", Blue2)
        SignupTextField(
            value = userId,
            onValueChange = { userId = it },
            placeholder = "아이디를 입력해주세요"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호
        Label("비밀번호", Blue2)
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "비밀번호를 입력해주세요",
            visible = passwordVisible,
            onToggle = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 확인
        Label("비밀번호 확인", Blue2)
        PasswordTextField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            placeholder = "비밀번호를 다시 입력해주세요",
            visible = passwordConfirmVisible,
            onToggle = { passwordConfirmVisible = !passwordConfirmVisible }
        )

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = { onSignupComplete() }, // 회원가입 완료 후 로그인으로 돌아감
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
        fontSize = 12.sp,
        color = color,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, fontSize = 12.sp, color = CoolGray500)
        },
        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Blue2,
            unfocusedBorderColor = Blue2,
            focusedPlaceholderColor = Blue2,
            unfocusedPlaceholderColor = Blue2,
            focusedTextColor = TextBlack,
            unfocusedTextColor = CoolGray500,
            cursorColor = Blue2
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggle: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, fontSize = 12.sp, color = CoolGray500)
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onToggle) {
                val icon = if (visible) R.drawable.eye_on else R.drawable.eye_off
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Blue2,
            unfocusedBorderColor = Blue2,
            focusedPlaceholderColor = Blue2,
            unfocusedPlaceholderColor = Blue2,
            focusedTextColor = TextBlack,
            unfocusedTextColor = CoolGray500,
            cursorColor = Blue2
        )
    )
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
