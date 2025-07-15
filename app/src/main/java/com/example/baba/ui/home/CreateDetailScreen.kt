package com.example.baba.ui.home

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDetailScreen(
    category: String,
    date: LocalDate,
    title: String,
    rating: Float,
    review: String,
    onReviewChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    isPublic: Boolean,
    onPublicChange: (Boolean) -> Unit,
    includeSpoiler: Boolean,
    onSpoilerChange: (Boolean) -> Unit,
    photos: List<Uri>,
    onPhotosChange: (List<Uri>) -> Unit,  // ✅ 새로운 사진 리스트 반영용 콜백
    onRemovePhoto: (Int) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    // ✅ 갤러리 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onPhotosChange(photos + it) // 새 Uri 추가
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                title = { Text("새 기록", fontSize = 18.sp) },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("저장")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 날짜, 카테고리, 제목
            Text(text = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.getDefault())), color = Color.Gray)
            Text(text = category, fontSize = 12.sp, color = Color.Gray)
            Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            // 별점
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = String.format(Locale.getDefault(), "%.1f", rating), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                repeat(5) { i ->
                    val icon = when {
                        rating >= i + 1f -> Icons.Filled.Star
                        rating >= i + 0.5f -> Icons.Filled.StarHalf
                        else -> Icons.Outlined.StarBorder
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (rating >= i + 0.5f) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Divider()

            // 리뷰
            Text(text = "리뷰", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            TextField(
                value = review,
                onValueChange = onReviewChange,
                placeholder = { Text("‘$title’에 대해 어떻게 생각하시나요?") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            // 장소
            TextField(
                value = location,
                onValueChange = onLocationChange,
                label = { Text("장소") },
                placeholder = { Text("장소를 입력하세요") },
                modifier = Modifier.fillMaxWidth()
            )

            // 공개 / 스포일러
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("전체 공개", fontSize = 16.sp)
                    Switch(checked = isPublic, onCheckedChange = onPublicChange)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("스포일러 포함", fontSize = 16.sp)
                    Switch(checked = includeSpoiler, onCheckedChange = onSpoilerChange)
                }
            }

            // 사진
            Text(text = "사진", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.LightGray)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "사진 추가")
                    }
                }
                itemsIndexed(photos) { idx, uri ->
                    Box(modifier = Modifier.size(80.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                        IconButton(
                            onClick = {
                                onPhotosChange(photos.toMutableList().apply { removeAt(idx) })
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "삭제")
                        }
                    }
                }
            }
        }
    }
}

