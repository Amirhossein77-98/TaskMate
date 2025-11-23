package com.amirsteinbeck.focusmate

data class Task(
    val title: String,
    val description: String,
    var isDone: Boolean = false,
    var isArchived: Boolean = false
)
