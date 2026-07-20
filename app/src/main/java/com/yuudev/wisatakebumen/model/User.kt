package com.yuudev.wisatakebumen.model

data class User(
    val success: Boolean = false,
    val id: String = "",
    val nama: String = "",
    val username: String = "",
    val role: String = "",
    val message: String = ""
)