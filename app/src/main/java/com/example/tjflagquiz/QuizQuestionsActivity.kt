package com.example.tjflagquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_quiz_questions.*

class QuizQuestionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_questions)

        val questionList = Constants.getQuestions()
        Log.i("Panjuta", "How many question do we have? ${questionList.size}")

        val currentPosition = 1
        val question : Question? = questionList[currentPosition - 1]
        my_progress_bar.progress = currentPosition
        tv_progress.text = currentPosition.toString() + "/${my_progress_bar.max}"
    }
}