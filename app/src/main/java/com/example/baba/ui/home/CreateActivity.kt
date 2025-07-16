package com.example.baba.ui.home

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.example.baba.data.network.SessionManager.userId
import com.example.baba.data.record.WatchedDateManager
import com.example.baba.ui.theme.BABATheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WatchedDateManager.initialize(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val initialCategory = intent.getStringExtra("category") ?: "PERFORMANCE"
        val initialDate = intent.getStringExtra("selectedDate")
            ?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
            ?: LocalDate.now()

        setContent {
            BABATheme {
                var showDetail by remember { mutableStateOf(false) }
                var title by remember { mutableStateOf("") }
                var rating by remember { mutableStateOf(0.0) }
                var review by remember { mutableStateOf("") }
                var location by remember { mutableStateOf("") }
                var isPublic by remember { mutableStateOf(true) }
                var includeSpoiler by remember { mutableStateOf(false) }
                var photo by remember { mutableStateOf<Uri?>(null) }  // 단일 사진

                // userId null 체크
                val currentUserId = userId
                if (currentUserId == null) {
                    Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@BABATheme
                }

                if (!showDetail) {
                    CreateScreen(
                        selectedDate = initialDate,
                        title = title,
                        rating = rating,
                        photo = photo,  // 사진 전달
                        onBack = { finish() },
                        onTitleChange = { title = it },
                        onRatingChange = { rating = it },
                        onPhotoChange = { photo = it },  // 사진 변경 콜백
                        onNext = { showDetail = true }
                    )
                } else {
                    CreateDetailScreen(
                        userId = currentUserId,
                        category = initialCategory,
                        date = initialDate,
                        title = title,
                        rating = rating.toFloat(),
                        review = review,
                        onReviewChange = { review = it },
                        location = location,
                        onLocationChange = { location = it },
                        isPublic = isPublic,
                        onPublicChange = { isPublic = it },
                        includeSpoiler = includeSpoiler,
                        onSpoilerChange = { includeSpoiler = it },

                        photo = photo,  // 단일 사진 전달
                        onPhotoChange = { photo = it },  // 사진 변경 콜백
                        onBack = { showDetail = false },
                        onSave = {
                            // 저장 완료 후 화면 종료
                            Toast.makeText(this, "기록이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    )
                }
            }
        }
    }
}