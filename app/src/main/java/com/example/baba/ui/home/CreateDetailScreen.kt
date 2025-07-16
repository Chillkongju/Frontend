package com.example.baba.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.network.SessionManager
import com.example.baba.data.record.WatchedDateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDetailScreen(
    userId: Long,
    category: String,
    date: LocalDate,
    title: String,
    rating: Double,
    review: String,
    onReviewChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    isPublic: Boolean,
    onPublicChange: (Boolean) -> Unit,
    includeSpoiler: Boolean,
    onSpoilerChange: (Boolean) -> Unit,
    photo: Uri?,  // 단일 사진
    onPhotoChange: (Uri?) -> Unit,  // 사진 변경 콜백
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        WatchedDateManager.initialize(context)
    }

    // 갤러리 런처 (단일 이미지)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onPhotoChange(uri)
    }

    fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream?.copyTo(output)
            }
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("imageFile", tempFile.name, requestFile)
        } catch (e: Exception) {
            Log.e("CreateDetail", "이미지 Multipart 변환 실패: ${e.message}")
            null
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
                    TextButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    Log.d("CreateDetail", "현재 userId: $userId")

                                    val imagePart = photo?.let { uriToMultipart(it) }

                                    val response = RetrofitInstance.diaryApi.createDiary(
                                        userId = userId,
                                        title = title,
                                        content = review,
                                        category = category,
                                        rating = rating.toDouble(),
                                        watchedAt = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                        imageFile = imagePart
                                    )

                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful) {
                                            response.body()?.let { diaryResponse ->
                                                WatchedDateManager.setWatchedDate(diaryResponse.id, date)
                                            }
                                            SessionManager.needsRefresh = true
                                            Toast.makeText(context, "기록 저장 성공", Toast.LENGTH_SHORT).show()
                                            onSave()
                                        } else {
                                            Toast.makeText(context, "저장 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                            Log.e("CreateDetail", "Error: ${response.errorBody()?.string()}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "에러: ${e.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("CreateDetail", "Exception: ${e.message}", e)
                                    }
                                }
                            }
                        }
                    ) {
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
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.getDefault())),
                color = Color.Gray
            )
            Text(text = getCategoryLabel(category), fontSize = 12.sp, color = Color.Gray)
            Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            // 별점
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", rating),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
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
                placeholder = { Text("'$title'에 대해 어떻게 생각하시나요?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("전체 공개", fontSize = 16.sp)
                    Switch(checked = isPublic, onCheckedChange = onPublicChange)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("스포일러 포함", fontSize = 16.sp)
                    Switch(checked = includeSpoiler, onCheckedChange = onSpoilerChange)
                }
            }

            // 사진 (단일)
            Text(text = "사진", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 사진 추가 버튼
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "사진 추가")
                }

                // 선택된 사진 표시
                photo?.let { uri ->
                    Box(modifier = Modifier.size(80.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                        IconButton(
                            onClick = { onPhotoChange(null) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "삭제", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

// 이미지 크기 조정 함수 (용량 최적화)
fun resizeBitmap(bitmap: Bitmap, maxWidth: Int = 400, maxHeight: Int = 300): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    val ratioBitmap = width.toFloat() / height.toFloat()
    val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

    var finalWidth = maxWidth
    var finalHeight = maxHeight

    if (ratioMax > ratioBitmap) {
        finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
    } else {
        finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
    }

    return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
}

fun getCategoryLabel(code: String): String {
    return when (code) {
        "BOOK" -> "도서"
        "MOVIE" -> "영화"
        "PERFORMANCE" -> "공연"
        else -> "기타"
    }
}