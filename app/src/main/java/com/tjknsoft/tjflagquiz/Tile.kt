package com.tjknsoft.tjflagquiz

data class Tile(
    val position: Int, // start with 1
    val isFaceUp: Boolean,
    // val isFlagImage: Boolean,
    val flagResId: Int,
    // val tileBackResID: Int,
    // val countryCode: String,
    val shortenedCountryName: String,
    val countryName: String,
)

