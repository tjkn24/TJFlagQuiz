package com.tjknsoft.tjflagquiz

data class Tile(
    val position: Int, // start with 1
    var isFaceUp: Boolean,
    // val isFlagImage: Boolean,
    val flagResId: Int,
    // val tileBackResID: Int,
    // val countryCode: String,
    val shortenedCountryName: String,
    val countryName: String,
)

