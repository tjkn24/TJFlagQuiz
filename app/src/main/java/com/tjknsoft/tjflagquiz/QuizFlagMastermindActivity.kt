package com.tjknsoft.tjflagquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class QuizFlagMastermindActivity : AppCompatActivity() {

    private var mTileList: ArrayList<Tile> = arrayListOf()
    private var mAllCountryNames: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_flag_mastermind)
    }
}