package com.example.baba.ui.friends

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.R
import com.example.baba.data.member.MemberInfoResponse
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.record.WatchedDateManager
import com.example.baba.ui.theme.*
import java.time.format.DateTimeFormatter

// 더미 데이터 - 사용자별 기록 데이터
data class FriendRecordData(
    val id: Long,
    val title: String,
    val category: String,
    val rating: String,
    val date: String,
    val comment: String,
    val image: Int? = null // 이미지 리소스 ID
)

// 사용자별 기록 더미 데이터
val friendsRecordData = mapOf(
    "정윤희" to listOf(
        FriendRecordData(
            id = 1L,
            title = "알라딘",
            category = "영화",
            rating = "4.0",
            date = "2025.07.14",
            comment = "4dx로 봤는데 영화관 향기가 좋았음 디즈니 실사화 중 제일 맘에 들었음",
            image = R.drawable.friend_movie_poster_1
        ),
        FriendRecordData(
            id = 2L,
            title = "해피엔드",
            category = "영화",
            rating = "4.5",
            date = "2025.07.08",
            comment = "재밌다.. 연출 넘 좋음",
            image = R.drawable.friend_movie_poster_2
        )
    ),
    "양서영" to listOf(
        FriendRecordData(
            id = 3L,
            title = "마침내 멸망하는 여름(스페셜 에디션)",
            category = "도서",
            rating = "4.5",
            date = "2025.07.01",
            comment = "읽는 내내 꿈속에서 살고 있는 듯한 느낌이 들었다",
            image = R.drawable.friend_book_poster_1
        ),
        FriendRecordData(
            id = 4L,
            title = "데스노트",
            category = "공연",
            rating = "4.5",
            date = "2025.06.29",
            comment = "인생작!!! 또 보고 싶당",
            image = R.drawable.friend_show_poster_1
        )
    ),
    "김민지" to listOf(
        // 김민지 더미 데이터가 없으므로 빈 리스트
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsRecordListScreen(
    category: String,
    targetMember: MemberInfoResponse? = null
) {
    val categoryName = listOf("전체", "도서", "영화", "공연")
    val initialIndex = categoryName.indexOf(category).takeIf { it >= 0 } ?: 0
    var selected by rememberSaveable { mutableStateOf(initialIndex) }
    var isGridView by rememberSaveable { mutableStateOf(false) }

    // username을 실제 이름으로 매핑하는 함수
    fun getUserDisplayName(username: String): String {
        return when (username) {
            "user1" -> "김민지"
            "user2" -> "정윤희"
            "user3" -> "양서영"
            "admin" -> "관리자"
            else -> username
        }
    }

    // 사용자별 더미 데이터 가져오기
    val displayName = targetMember?.let { getUserDisplayName(it.username) } ?: "사용자"
    val userRecords = friendsRecordData[displayName] ?: emptyList()

    // 카테고리별 필터링
    val filteredRecords = if (selected == 0) {
        userRecords
    } else {
        userRecords.filter { it.category == categoryName[selected] }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$displayName's",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 카테고리 탭
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoryName.forEachIndexed { index, name ->
                        val isSelected = index == selected
                        Text(
                            text = name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) White else CoolGray700,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Blue2 else CoolGray150)
                                .clickable { selected = index }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // 리스트/그리드 버튼
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isGridView = false },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_list),
                            contentDescription = "리스트 보기",
                            modifier = Modifier.size(20.dp),
                            tint = if (!isGridView) Blue2 else CoolGray500
                        )
                    }
                    IconButton(
                        onClick = { isGridView = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_grid),
                            contentDescription = "그리드 보기",
                            modifier = Modifier.size(20.dp),
                            tint = if (isGridView) Blue2 else CoolGray500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "기록이 없습니다",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                if (isGridView) {
                    // Grid View
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredRecords.size) { index ->
                            val record = filteredRecords[index]
                            GridImageItem(
                                imageRes = record.image,
                                title = record.title,
                                category = record.category,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.7f)
                                    .clip(RoundedCornerShape(8.dp)),
                                onClick = {}
                            )
                        }
                    }
                } else {
                    // List View
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredRecords.size) { index ->
                            val record = filteredRecords[index]
                            FriendRecordItem(
                                record = record,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberBase64Image(base64String: String?): androidx.compose.ui.graphics.ImageBitmap? {
    return remember(base64String) {
        try {
            if (base64String.isNullOrEmpty()) return@remember null
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

// Grid View용 이미지 아이템
@Composable
fun GridImageItem(
    imageRes: Int?,
    title: String,
    category: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageRes != null) {
            // 이미지가 있을 때는 이미지만 표시
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FriendRecordItem(
    record: FriendRecordData,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 썸네일 이미지
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (record.image != null) {
                Image(
                    painter = painterResource(id = record.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 카테고리 아이콘
                Image(
                    painter = painterResource(
                        id = when (record.category) {
                            "도서" -> R.drawable.recommend_book
                            "영화" -> R.drawable.recommend_movie
                            "공연" -> R.drawable.recommend_show
                            else -> R.drawable.ic_add
                        }
                    ),
                    contentDescription = "카테고리 아이콘",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 텍스트 영역
        Column(modifier = Modifier.weight(1f)) {
            // 제목 + 카테고리
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = record.category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 별점 + 날짜
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = record.rating, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = record.date, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 코멘트
            Text(
                text = record.comment,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 더보기 아이콘
        IconButton(
            onClick = { /* TODO: Show options */ },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "더보기",
                tint = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyRecordListPreview() {
    FriendsRecordListScreen(category = "전체")
}