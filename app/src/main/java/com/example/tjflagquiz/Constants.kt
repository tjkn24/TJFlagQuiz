package com.example.tjflagquiz

object Constants {
    const val QUESTIONTEXT = "This is the flag of..."
    fun getQuestions(): ArrayList<Question> {
        val questionList = ArrayList<Question>()

        val que1 =
            Question(1, QUESTIONTEXT, R.drawable.samoa, "Malaysia", "Benin", "Samoa", "syria", 3)

        questionList.add(que1)
        return questionList

    }
}