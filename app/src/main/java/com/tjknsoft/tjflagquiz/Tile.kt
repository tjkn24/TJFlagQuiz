package com.tjknsoft.tjflagquiz

data class Tile(
    val position: Int,
    val isFlagImage: Boolean,
    val isFaceUp: Boolean,
    val flagResId: Int,
    val countryCode: String,
    val countryName: String,
    )

