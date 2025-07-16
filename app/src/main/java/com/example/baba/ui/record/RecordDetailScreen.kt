package com.example.baba.ui.record

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.baba.R
import com.example.baba.ui.theme.CoolGray700
import com.example.baba.ui.common.CommentBottomSheet
import com.example.baba.ui.common.Comment
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
    var showCommentBottomSheet by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(8) }

    // 댓글 관리
    val comments = remember { mutableListOf<Comment>().toMutableStateList() }
    var commentCount by remember { mutableStateOf(6) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 추천 작품 관련 상태
    var recommendations by remember { mutableStateOf<List<RecommendationResponse>>(emptyList()) }
    var isLoadingRecommendations by remember { mutableStateOf(false) }
    var recommendationError by remember { mutableStateOf<String?>(null) }

    // 더미 댓글 데이터 초기화
    LaunchedEffect(Unit) {
        comments.addAll(getDummyCommentsForRecord())
    }

    // 추천 작품 로드
    LaunchedEffect(record.id) {
        isLoadingRecommendations = true
        recommendationError = null

        try {
            val response = RetrofitInstance.recommendationApi.getRecommendationsByDiary(record.id)

            if (response.isSuccessful) {
                recommendations = response.body() ?: emptyList()
            } else {
                recommendationError = "추천 작품을 불러오는데 실패했습니다"
            }
        } catch (e: Exception) {
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
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
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
                            painter = painterResource(id = R.drawable.ic_rating_button_star),
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

            // 추천 작품 섹션
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
                                text = "추천을 불러올 수 없습니다",
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            recommendations.take(3).forEach { recommendation ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = recommendation.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = recommendation.genre.ifEmpty { record.category },
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 하단 좋아요/댓글 영역
        Column {
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좋아요 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
                        Icon(
                            painter = painterResource(
                                id = if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_outline
                            ),
                            contentDescription = "좋아요",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    isLiked = !isLiked
                                    likeCount += if (isLiked) 1 else -1
                                }
                        )
                    }

                    Text(
                        text = likeCount.toString(),
                        fontSize = 20.sp,
                        color = CoolGray700
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 댓글 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_comment),
                            contentDescription = "댓글",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    showCommentBottomSheet = true
                                }
                        )
                    }
                    Text(
                        text = commentCount.toString(),
                        fontSize = 20.sp,
                        color = CoolGray700
                    )
                }
            }
        }
    }

    if (showCommentBottomSheet) {
        val currentUserName = SessionManager.userName ?: "사용자"
        val currentUserProfileImage = R.drawable.ic_default_profile

        CommentBottomSheet(
            comments = comments,
            onDismiss = { showCommentBottomSheet = false },
            onCommentAdded = { comment ->
                comments.add(comment)
                commentCount += 1
            },
            onCommentDeleted = {
                commentCount -= 1
            },
            currentUserName = currentUserName,
            currentUserProfileImage = currentUserProfileImage
        )
    }
}

// RecordDetailScreen용 더미 댓글 데이터
fun getDummyCommentsForRecord(): List<Comment> {
    return listOf(
        Comment(
            id = 1,
            userName = "호두왕자",
            userProfileImage = R.drawable.ic_default_profile,
            content = "정말 감동적인 작품이었어요! 저도 봤는데 눈물이 났습니다.",
            timeAgo = "2분 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = false,
            likeCount = 3
        ),
        Comment(
            id = 2,
            userName = "쑤인",
            userProfileImage = R.drawable.ic_default_profile,
            content = "디어 에반 핸슨 진짜 명작이죠ㅠㅠ 'You Will Be Found' 들으면서 울었어요",
            timeAgo = "15분 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = true,
            likeCount = 5
        ),
        Comment(
            id = 3,
            userName = "시새린",
            userProfileImage = R.drawable.ic_default_profile,
            content = "공연 리뷰 잘 읽었어요. 저도 꼭 보러 가야겠네요!",
            timeAgo = "1시간 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = false,
            likeCount = 1
        ),
        Comment(
            id = 4,
            userName = "6812",
            userProfileImage = R.drawable.ic_default_profile,
            content = "네! 정말 좋은 공연이에요",
            timeAgo = "50분 전",
            isReply = true,
            parentCommentId = 3,
            mentionedUser = "시새린",
            isLiked = false,
            likeCount = 0
        ),
        Comment(
            id = 5,
            userName = "mmmuuu",
            userProfileImage = R.drawable.ic_default_profile,
            content = "4.5점이면 정말 좋았나보네요. 추천해주셔서 감사해요!",
            timeAgo = "2시간 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = false,
            likeCount = 2
        ),
        Comment(
            id = 6,
            userName = "바바유저",
            userProfileImage = R.drawable.ic_default_profile,
            content = "같은 공연 봤는데 정말 공감돼요. 리뷰 잘 써주셨네요👏",
            timeAgo = "3시간 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = true,
            likeCount = 4
        )
    )
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