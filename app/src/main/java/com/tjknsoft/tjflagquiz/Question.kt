package com.tjknsoft.tjflagquiz

data class Question(
    val id: Int,
    val title: String,
    val image: Int = 0,
    val countryName: String = "",
    val optionsName: ArrayList<String>,
    val optionsImage: ArrayList<Int>,
    val correctPosition: Int,
)