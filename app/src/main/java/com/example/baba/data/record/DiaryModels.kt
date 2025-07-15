package com.example.baba.data.record

data class DiaryCreateRequest(
    val title: String,
    val content: String,
    val category: String,
    val rating: Int,
    val watchedAt: String
    // val public: Boolean,
    // val spoiler: Boolean,
)

data class DiaryResponse(
    val id: Long,
    val title: String,
    val content: String,
    val category: String,
    val categoryLabel: String,
    val createdDate: String
)
