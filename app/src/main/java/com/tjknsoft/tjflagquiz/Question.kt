package com.tjknsoft.tjflagquiz

data class Question(
    val id: Int,
    val title: String,
    val image: Int = 0,
    val countryName: String = "",
    val optionNames: ArrayList<String>,
    val optionImages: ArrayList<Int>,
    val correctPosition: Int,
)