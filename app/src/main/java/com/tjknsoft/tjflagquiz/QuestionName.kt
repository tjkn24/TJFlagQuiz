package com.tjknsoft.tjflagquiz

data class QuestionName(
    val id: Int,
    val question: String,
    val image: Int,
    val options: ArrayList<String>,
//    val optionOne: String,
//    val optionTwo: String,
//    val optionThree: String,
//    val optionFour: String,
    val correctPosition: Int,
)