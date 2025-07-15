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