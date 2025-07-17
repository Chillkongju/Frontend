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

    var records by remember { mutableStateOf<List<RecordData>>(emptyList()) }
    val context = LocalContext.current

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

    LaunchedEffect(Unit) {
        WatchedDateManager.initialize(context)
        try {
            // TODO: 실제 친구의 기록 데이터를 가져오는 API 호출
            // 현재는 빈 리스트로 설정
            records = emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val filteredRecords = if (selected == 0) records else records.filter { it.category == categoryName[selected] }

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
                    text = targetMember?.let { getUserDisplayName(it.username) + "'s" } ?: "친구's",
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
                            imageBase64 = record.image,
                            title = record.title,
                            category = record.category,  // 카테고리 전달
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
                        RecordItem(
                            id = record.id,
                            title = record.title,
                            category = record.category,
                            rating = record.rating,
                            date = record.date,
                            comment = record.comment,
                            imageBase64 = record.image
                        )
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
    imageBase64: String?,
    title: String,
    category: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val imageBitmap = rememberBase64Image(imageBase64)

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            // 이미지가 있을 때는 이미지만 표시
            Image(
                bitmap = imageBitmap,
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

data class RecordData(
    val id: Long,
    val title: String,
    val category: String,
    val rating: String,
    val date: String,
    val comment: String,
    val image: String?
)

@Composable
fun RecordItem(
    id: Long,
    title: String,
    category: String,
    rating: String,
    date: String,
    comment: String,
    imageBase64: String?,
    onClick: () -> Unit = {}
) {
    val imageBitmap = rememberBase64Image(imageBase64)

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
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = title,
                    color = Color.Gray,
                    fontSize = 12.sp,
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

        Spacer(modifier = Modifier.width(12.dp))

        // 텍스트 영역
        Column(modifier = Modifier.weight(1f)) {
            // 제목 + 카테고리
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = category,
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
                Text(text = rating, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = date, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 코멘트
            Text(
                text = comment,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
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