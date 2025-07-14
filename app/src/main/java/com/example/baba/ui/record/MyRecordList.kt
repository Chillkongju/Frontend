package com.example.baba.ui.record

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecordListScreen(category: String) {
    val categoryName = listOf("전체", "도서", "영화", "공연")
    val initialIndex = categoryName.indexOf(category).takeIf { it >= 0 } ?: 0
    var selected by rememberSaveable { mutableStateOf(initialIndex) }
    var isGridView by rememberSaveable { mutableStateOf(false) }

    val records = listOf(
        RecordData("하데스타운", "공연", "4.5", "2025.07.09.", "추천해요", R.drawable.ic_launcher_background),
        RecordData("모순", "도서", "5.0", "2025.06.29.", "꼭 읽어야 할책", R.drawable.ic_nav_record),
        RecordData("쥬라기 월드: 새로운 시작", "공연", "3.0", "2025.07.09.", "", R.drawable.recommend_book),
        RecordData("8월의 크리스마스", "영화", "4.0", "2025.07.05.", "", R.drawable.recommend_show),
        RecordData("디어 에반 핸슨", "영화", "4.7", "2025.07.01.", "", R.drawable.ic_nav_friend),
        RecordData("이웃집 토토로", "영화", "4.8", "2025.06.25.", "", R.drawable.main_logo),
    )

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
                Text("칠공주's", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = { isGridView = false }) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "List View",
                            tint = if (!isGridView) Color(0xFF3366FF) else Color.Gray
                        )
                    }
                    IconButton(onClick = { isGridView = true }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Grid View",
                            tint = if (isGridView) Color(0xFF3366FF) else Color.Gray
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 카테고리 탭
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryName.forEachIndexed { index, name ->
                    val isSelected = index == selected
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.Black,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF3366FF) else Color(0xFFE0E0E0))
                            .clickable { selected = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isGridView) {
                // 🔲 Grid View
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRecords.size) { index ->
                        val record = filteredRecords[index]
                        Image(
                            painter = painterResource(id = record.imageRes),
                            contentDescription = record.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            } else {
                // 📃 List View
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRecords.size) { index ->
                        val record = filteredRecords[index]
                        RecordItem(
                            title = record.title,
                            category = record.category,
                            rating = record.rating,
                            date = record.date,
                            comment = record.comment,
                            imageRes = record.imageRes
                        )
                    }
                }
            }
        }
    }
}

data class RecordData(
    val title: String,
    val category: String,
    val rating: String,
    val date: String,
    val comment: String,
    val imageRes: Int
)

@Composable
fun RecordItem(
    title: String,
    category: String,
    rating: String,
    date: String,
    comment: String,
    imageRes: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Thumbnail",
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = category, fontSize = 12.sp, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(16.dp)
                )
                Text(text = rating, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = date, fontSize = 12.sp)
            }

            if (comment.isNotBlank()) {
                Text(text = comment, fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyRecordListPreview() {
    MyRecordListScreen(category = "전체")
}
