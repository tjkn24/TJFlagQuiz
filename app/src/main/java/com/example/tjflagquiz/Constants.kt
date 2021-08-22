package com.example.tjflagquiz

object Constants {
    const val QUESTIONTEXT = "This is the flag of..."
    fun getQuestions(): ArrayList<Question> {
        val questionList = ArrayList<Question>()

        val que1 =
            Question(1, QUESTIONTEXT, R.drawable.samoa, "Malaysia", "Benin", "Samoa", "syria", 3)
        val que2 = Question(2, QUESTIONTEXT, R.drawable.lebanon, "Lebanon", "Fiji", "Angola", "Myanmar", 1)
        val que3 = Question(3, QUESTIONTEXT, R.drawable.peru, "Slovenia", "Peru", "Tibet", "Honduras", 2)
        val que4 = Question(4, QUESTIONTEXT, R.drawable.norfolk_island, "Senegal", "Chad", "Guam", "Norfolk Island", 4)
        val que5 = Question(5, QUESTIONTEXT, R.drawable.jordan, "Jordan", "Malta", "Greenland", "Poland", 1)
        val que6 = Question(6, QUESTIONTEXT, R.drawable.finland, "Ecuador", "Jamaica", "Finland", "Lithuania", 3)
        val que7 = Question(7, QUESTIONTEXT, R.drawable.mexico, "Nepal", "Mexico", "Nauru", "Sierra Leone", 2)
        val que8 = Question(8, QUESTIONTEXT, R.drawable.tonga, "Vanuatu", "Tonga", "Cook Islands", "Djibouti", 2)
        val que9 = Question(9, QUESTIONTEXT, R.drawable.kenya, "Kenya", "Kazakhstan", "Estonia", "Cape Verde", 1)
        val que10 = Question(10, QUESTIONTEXT, R.drawable.san_marino, "Sudan", "Hong Kong", "San Marino", "Chile", 3)

        questionList.add(que1)
        questionList.add(que2)
        questionList.add(que3)
        questionList.add(que4)
        questionList.add(que5)
        questionList.add(que6)
        questionList.add(que7)
        questionList.add(que8)
        questionList.add(que9)
        questionList.add(que10)

        return questionList

    }
}