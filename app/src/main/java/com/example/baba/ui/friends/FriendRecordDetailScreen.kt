package com.example.baba.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.baba.R
import com.example.baba.data.member.MemberInfoResponse
import com.example.baba.ui.common.CommentBottomSheet
import com.example.baba.ui.common.Comment
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 친구 기록 상세 더미 데이터
val friendRecordDetails = mapOf(
    1L to FriendRecordData(
        id = 1L,
        title = "알라딘",
        category = "영화",
        rating = "4.0",
        date = "2025.07.14",
        comment = "4dx로 봤는데 영화관 향기가 좋았음 디즈니 실사화 중 제일 맘에 들었음. 윌 스미스의 지니 연기가 인상적이었고, 음악도 좋았다. 아이들과 함께 보기 좋은 영화였다.",
        image = R.drawable.friend_movie_poster_1
    ),
    2L to FriendRecordData(
        id = 2L,
        title = "해피엔드",
        category = "영화",
        rating = "4.5",
        date = "2025.07.08",
        comment = "재밌다.. 연출 넘 좋음. 스토리가 탄탄하고 배우들의 연기가 인상적이었다. 특히 마지막 장면에서의 반전이 정말 놀라웠다.",
        image = R.drawable.friend_movie_poster_2
    ),
    3L to FriendRecordData(
        id = 3L,
        title = "마침내 멸망하는 여름(스페셜 에디션)",
        category = "도서",
        rating = "4.5",
        date = "2025.07.01",
        comment = "읽는 내내 꿈속에서 살고 있는 듯한 느낌이 들었다. 작가의 섬세한 문체와 상상력이 인상적이었고, 여름의 분위기가 생생하게 느껴졌다.",
        image = R.drawable.friend_book_poster_1
    ),
    4L to FriendRecordData(
        id = 4L,
        title = "데스노트",
        category = "공연",
        rating = "4.5",
        date = "2025.06.29",
        comment = "인생작!!! 또 보고 싶당. 배우들의 연기가 정말 훌륭했고, 무대 연출도 완벽했다. 특히 라이토 역할의 배우가 인상적이었다.",
        image = R.drawable.friend_show_poster_1
    )
)

// 친구별 댓글 더미 데이터
val friendComments = mapOf(
    1L to listOf(
        Comment(
            id = 1,
            userName = "김민지",
            userProfileImage = R.drawable.ic_default_profile,
            content = "저도 이 영화 너무 좋아해요!",
            timeAgo = "2시간 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = false,
            likeCount = 3
        ),
        Comment(
            id = 2,
            userName = "양서영",
            userProfileImage = R.drawable.ic_default_profile,
            content = "4dx로 보면 정말 재미있겠네요 ㅎㅎ",
            timeAgo = "1시간 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = true,
            likeCount = 1
        )
    ),
    2L to listOf(
        Comment(
            id = 3,
            userName = "김민지",
            userProfileImage = R.drawable.ic_default_profile,
            content = "연출이 정말 좋은 영화죠!",
            timeAgo = "3시간 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = false,
            likeCount = 2
        )
    ),
    3L to listOf(
        Comment(
            id = 4,
            userName = "정윤희",
            userProfileImage = R.drawable.ic_default_profile,
            content = "이 책 저도 읽어봐야겠어요!",
            timeAgo = "4시간 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = false,
            likeCount = 1
        )
    ),
    4L to listOf(
        Comment(
            id = 5,
            userName = "정윤희",
            userProfileImage = R.drawable.ic_default_profile,
            content = "데스노트 뮤지컬 정말 최고예요!",
            timeAgo = "5시간 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = true,
            likeCount = 4
        ),
        Comment(
            id = 6,
            userName = "김민지",
            userProfileImage = R.drawable.ic_default_profile,
            content = "저도 다시 보고 싶네요 ㅠㅠ",
            timeAgo = "3시간 전",
            isReply = false,
            parentCommentId = null,
            mentionedUser = null,
            isLiked = false,
            likeCount = 2
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRecordDetailScreen(
    recordId: Long,
    friendName: String,
    navController: NavController? = null,
    onBackClick: () -> Unit = {}
) {
    val record = friendRecordDetails[recordId]
    val comments = remember { (friendComments[recordId] ?: emptyList()).toMutableStateList() }

    var showCommentBottomSheet by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (record == null) {
        // 기록이 없는 경우 처리
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "기록을 찾을 수 없습니다",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
        return
    }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. (E)")
    val date = try {
        LocalDate.parse(record.date.replace(".", "-"))
    } catch (e: Exception) {
        LocalDate.now()
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
            // 상단 헤더 (뒤로가기 버튼)
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
                            if (navController != null) {
                                navController.popBackStack()
                            } else {
                                onBackClick()
                            }
                        }
                )

                // 친구 이름 표시
                Text(
                    text = "$friendName's",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // 빈 공간 (대칭을 위해)
                Spacer(modifier = Modifier.size(28.dp))
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
                    if (record.image != null) {
                        Image(
                            painter = painterResource(id = record.image),
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
                text = record.comment,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 20.dp)
            )

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
                CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
                    Icon(
                        painter = painterResource(
                            id = if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_outline
                        ),
                        contentDescription = "좋아요",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { isLiked = !isLiked }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 댓글 버튼
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
            }
        }
    }

    if (showCommentBottomSheet) {
        CommentBottomSheet(
            diaryId = recordId,
            comments = comments,
            onDismiss = { showCommentBottomSheet = false },
            onCommentAdded = { newComment ->
                // 친구 기록의 댓글이므로 실제 작성은 안 되고 UI만 표시
                comments.add(newComment)
            },
            onCommentDeleted = { comment ->
                comments.removeIf { it.id == comment.id }
            },
            currentUserName = "김민지",
            currentUserProfileImage = R.drawable.ic_default_profile
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FriendRecordDetailScreenPreview() {
    FriendRecordDetailScreen(
        recordId = 1L,
        friendName = "정윤희",
        onBackClick = {}
    )
}