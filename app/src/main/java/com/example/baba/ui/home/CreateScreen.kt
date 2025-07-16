package com.example.baba.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    selectedDate: LocalDate,
    title: String,
    rating: Double,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onRatingChange: (Double) -> Unit,
    onNext: () -> Unit
) {
    // 이미지 첨부 상태만 로컬로 관리합니다
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imageUri = it } }

    // 별점용 상수
    val starCount     = 5
    val starSizeDp    = 32.dp
    val starSpacingDp = 4.dp
    val starSizePx    = with(LocalDensity.current) { starSizeDp.toPx() }
    val starSpacePx   = with(LocalDensity.current) { starSpacingDp.toPx() }
    val totalStarsW   = starSizeDp * starCount + starSpacingDp * (starCount - 1)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,      // ← 여기를 ArrowBack 으로
                            contentDescription = "뒤로",
                            modifier = Modifier.size(24.dp)            // 사이즈 지정은 Modifier 에서
                        )
                    }
                },
                title = { Text("새 기록", fontSize = 18.sp) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9F9F9),
        bottomBar = {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
            ) {
                Text("다음", color = Color.White, fontSize = 16.sp)
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 날짜 표시 (원하시면 추가)
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("M월 d일", Locale.getDefault())),
                fontSize = 14.sp,
                color = Color.Gray
            )

            // 안내 문구
            Text(
                text = "기록할 문화생활의 제목과 평점을 입력해 주세요.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            // 이미지 + 제목 입력
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { pickImageLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "첨부된 이미지",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "이미지 추가", tint = Color.Gray, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    placeholder = { Text("제목을 입력하세요") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor   = Color(0xFF0D47A1),
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor          = Color(0xFF0D47A1)
                    )
                )
            }

            // 별점 표시
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = String.format(Locale.getDefault(), "%.1f", rating), fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(totalStarsW)
                        .height(starSizeDp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // .5 단위로 별점 계산
                                val block = starSizePx + starSpacePx
                                val idx = (offset.x / block).toInt().coerceIn(0, starCount - 1)
                                val within = offset.x % block
                                val newRating = idx + if (within > starSizePx / 2) 1.0 else 0.5
                                onRatingChange(newRating.coerceIn(0.0, starCount.toDouble()))
                            }
                        }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(starSpacingDp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        for (i in 0 until starCount) {
                            val icon = when {
                                rating >= i + 1.0    -> Icons.Filled.Star
                                rating >= i + 0.5 -> Icons.Filled.StarHalf
                                else               -> Icons.Outlined.StarBorder
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (icon == Icons.Outlined.StarBorder) Color.Gray else Color(0xFFFFD700),
                                modifier = Modifier.size(starSizeDp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateScreen() {
    // 예제용: 날짜, 람다만 더미로 전달
    CreateScreen(
        selectedDate   = LocalDate.now(),
        title          = "",
        rating         = 2.5,
        onBack         = {},
        onTitleChange  = {},
        onRatingChange = {},
        onNext         = {}
    )
}



