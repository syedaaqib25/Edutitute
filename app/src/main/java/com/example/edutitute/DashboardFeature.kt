package com.example.edutitute

import androidx.annotation.DrawableRes

data class DashboardFeature(
    val name: String,
    @DrawableRes val iconRes: Int,
    val action: String = "" // Added: simple tag for identifying feature
)
