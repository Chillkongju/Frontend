package com.example.baba.ui.record

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun RecordDetailScreen(
    title: String,
    date: LocalDate,
    rating: Float,
    content: String,
    isPublic: Boolean,
    category: String,
    photoUri: Uri?,
    navController: NavController
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. (E)")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 상단 나가기 화살표
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .size(28.dp)
                .clickable {
                    navController.popBackStack() // MyRecordScreen으로 이동
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 공개 여부
        Text(
            text = if (isPublic) "전체 공개" else "비공개",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.Start)
        )

        // 이미지
        photoUri?.let {
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
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp)
        )

        // 카테고리
        Text(
            text = category,
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
            Text("  $rating", fontSize = 14.sp)
        }

        // 내용
        Text(
            text = content,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally  // ✅ 전체 가운데 정렬
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