package com.example.baba.ui.record

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.network.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    record: Record,
    navController: NavController
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. (E)")
    val date = try {
        LocalDate.parse(record.date)
    } catch (e: Exception) {
        LocalDate.now()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 삭제 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "기록 삭제",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "정말로 이 기록을 삭제하시겠습니까?\n삭제된 기록은 복구할 수 없습니다.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        isDeleting = true
                        coroutineScope.launch {
                            try {
                                val userId = SessionManager.userId
                                if (userId == null || userId <= 0) {
                                    println("사용자 ID가 없습니다. 다시 로그인해주세요.")
                                    return@launch
                                }

                                Log.d("Delete", "삭제 요청: diaryId=${record.id}, memberId=$userId") // ← 로그 추가
                                val response = RetrofitInstance.diaryApi.deleteDiary(
                                    memberId = userId,
                                    diaryId = record.id
                                )
                                Log.d("Delete", "삭제 응답: ${response.isSuccessful}, ${response.code()}") // ← 로그 추가
                                if (response.isSuccessful) {
                                    navController.popBackStack()
                                } else {
                                    Log.e("Delete", "삭제 실패: ${response.errorBody()?.string()}") // ← 로그 추가
                                }
                            } catch (e: Exception) {
                                Log.e("Delete", "삭제 오류: ${e.message}", e) // ← 로그 추가
                            } finally {
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text("삭제", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("취소")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 상단 헤더 (뒤로가기 버튼과 삭제 버튼)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 뒤로가기 버튼
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )

            // 삭제 버튼
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Red,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        showDeleteDialog = true
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 공개 여부
        Text(
            text = if (record.isPublic) "전체 공개" else "비공개",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.Start)
        )

        // 이미지
        record.photoUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(top = 8.dp),
                contentScale = ContentScale.Crop
            )
        }

        // 제목
        Text(
            text = record.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp)
        )

        // 카테고리
        Text(
            text = record.category,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )

        // 날짜
        Text(
            text = date.format(dateFormatter),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp)
        )

        // 별점
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null)
            Text("  ${record.rating}", fontSize = 14.sp)
        }

        // 내용
        Text(
            text = record.content,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You might also like",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("렌트", "넥스트 투 노멀", "스프링 어웨이크닝").forEach { title ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = title, style = MaterialTheme.typography.bodySmall)
                        Text(text = "공연", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}