package com.tjknsoft.tjflagquiz

data class Tile(
    val position: Int,
    val isFaceUp: Boolean,
    val isFlagImage: Boolean,
    val flagResId: Int,
    // val tileBackResID: Int,
    val countryCode: String,
    val countryName: String,
)

