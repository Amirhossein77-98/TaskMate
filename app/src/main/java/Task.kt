package com.amirsteinbeck.taskmate

import java.time.LocalDateTime

data class Task(
    val title: String,
    val description: String,
    var isDone: Boolean = false,
    var isArchived: Boolean = false,
    var id: Long = System.currentTimeMillis(),
    var due: Long = System.currentTimeMillis()
)
