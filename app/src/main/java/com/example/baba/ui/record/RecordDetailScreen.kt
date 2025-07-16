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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.network.SessionManager
import com.example.baba.data.recommendation.RecommendationResponse
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.ui.res.painterResource
import com.example.baba.R
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
    var showDropdownMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 추천 작품 관련 상태
    var recommendations by remember { mutableStateOf<List<RecommendationResponse>>(emptyList()) }
    var isLoadingRecommendations by remember { mutableStateOf(false) }
    var recommendationError by remember { mutableStateOf<String?>(null) }

    // 추천 작품 로드
    LaunchedEffect(record.id) {
        isLoadingRecommendations = true
        recommendationError = null

        try {
            Log.d("Recommendation", "추천 조회 시작: diaryId=${record.id}")
            val response = RetrofitInstance.recommendationApi.getRecommendationsByDiary(record.id)

            Log.d("Recommendation", "응답 코드: ${response.code()}")
            Log.d("Recommendation", "응답 성공: ${response.isSuccessful}")
            Log.d("Recommendation", "응답 데이터: ${response.body()}")

            if (response.isSuccessful) {
                val data = response.body() ?: emptyList()
                Log.d("Recommendation", "추천 개수: ${data.size}")
                recommendations = data
            } else {
                Log.e("Recommendation", "에러 응답: ${response.errorBody()?.string()}")
                recommendationError = "추천 작품을 불러오는데 실패했습니다"
            }
        } catch (e: Exception) {
            Log.e("Recommendation", "예외 발생: ${e.message}", e)
            recommendationError = "네트워크 오류가 발생했습니다"
        } finally {
            isLoadingRecommendations = false
        }
    }

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

                                Log.d("Delete", "삭제 요청: diaryId=${record.id}, memberId=$userId")
                                val response = RetrofitInstance.diaryApi.deleteDiary(
                                    memberId = userId,
                                    diaryId = record.id
                                )
                                Log.d("Delete", "삭제 응답: ${response.isSuccessful}, ${response.code()}")
                                if (response.isSuccessful) {
                                    navController.popBackStack()
                                } else {
                                    Log.e("Delete", "삭제 실패: ${response.errorBody()?.string()}")
                                }
                            } catch (e: Exception) {
                                Log.e("Delete", "삭제 오류: ${e.message}", e)
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
        // 상단 헤더 (뒤로가기 버튼과 더보기 버튼)
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

            // 더보기 버튼과 드롭다운
            Box {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            showDropdownMenu = true
                        }
                )

                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false },
                    modifier = Modifier
                        .width(120.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "수정",
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        },
                        onClick = {
                            showDropdownMenu = false
                            // TODO: 수정 기능 구현
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "삭제",
                                    fontSize = 14.sp,
                                    color = Color.Red
                                )
                            }
                        },
                        onClick = {
                            showDropdownMenu = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 이미지와 텍스트 정보를 가로로 배치
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 이미지 또는 아이콘
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (record.photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(record.photoUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 카테고리별 아이콘 표시
                    Image(
                        painter = painterResource(
                            id = when (record.category) {
                                "영화" -> R.drawable.recommend_movie
                                "도서" -> R.drawable.recommend_book
                                "공연" -> R.drawable.recommend_show
                                else -> R.drawable.ic_add
                            }
                        ),
                        contentDescription = "카테고리 아이콘",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // 텍스트 정보
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 제목
                Text(
                    text = record.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // 카테고리
                Text(
                    text = record.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 날짜와 별점을 세로 선으로 구분
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 별점
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Blue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " ${record.rating} ",
                        fontSize = 14.sp
                    )

                    // 구분선
                    Text(
                        text = "|",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // 날짜
                    Text(
                        text = date.format(dateFormatter),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // 내용
        Text(
            text = record.content,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 20.dp)
        )

        // 추천 작품 섹션 (기존 위치와 스타일 유지)
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

            when {
                isLoadingRecommendations -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }

                recommendationError != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "추천 작품을 생성 중입니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                recommendations.isEmpty() -> {
                    Text(
                        text = "추천 작품이 없습니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        recommendations.forEach { recommendation ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = recommendation.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = recommendation.genre,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecordDetailScreenPreview() {
    val sampleRecord = Record(
        id = 1L,
        title = "디어 에반 핸슨",
        date = "2025-07-10",
        category = "공연",
        rating = 4.5f,
        content = "외로움 속에서 소통을 갈망하는 한 소년의 거짓말과 예상치 못한 방향으로 퍼져나가며, 따뜻하면서도 아픈 울림을 남긴다. 섬세한 감정선과 현실적인 캐릭터들이 내 이야기처럼 느껴졌고, 특히 'You Will Be Found'는 눈물 없이 들을 수 없었다. 진실 또한 왜곡될 수 있고, 그럼에도 누군가는 그 진실에 구원받을 수 있다는 메시지가 오래도록 마음에 남는다.",
        isPublic = true,
        photoUri = null
    )

    RecordDetailScreen(
        record = sampleRecord,
        navController = androidx.navigation.compose.rememberNavController()
    )
}