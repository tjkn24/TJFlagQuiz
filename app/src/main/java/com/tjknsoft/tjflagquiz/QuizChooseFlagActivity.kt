package com.tjknsoft.tjflagquiz

import androidx.appcompat.app.AppCompatActivity

class QuizChooseFlagActivity: AppCompatActivity() {

    private var mCurrentQuestionNumber = 1
    private var mQuestionFlagList: ArrayList<QuestionName> = arrayListOf()
    private var mSelectedOptionPosition: Int = 0 //0 = no options selected
    private var mScoreAbsolute: Int  = 0
    private var mScorePercentage: Float = 0.0F
    private val mAllFlags: ArrayList<Int> = arrayListOf()
    private var mSelectedFlags: ArrayList<Int> = arrayListOf()
    private var mQuestionSize: Int = 10
    private var mAnswerSize: Int = 4
    private var mMapFlagToCountry = mutableMapOf<Int, String>()
}