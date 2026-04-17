package com.example.apnivehicle.models

import java.util.UUID

data class SearchHistory(
    val query: String,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String = UUID.randomUUID().toString()
)
