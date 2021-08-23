package com.example.tjflagquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_quiz_questions.*

class QuizQuestionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_questions)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        val questionList = Constants.getQuestions()
        Log.i("Panjuta", "How many question do we have? ${questionList.size}")

        var currentPosition = 1
        var currentQuestion : Question = questionList[currentPosition - 1]

        my_progress_bar.progress = currentPosition
        tv_progress.text = currentPosition.toString() + "/${my_progress_bar.max}"
        tv_question.text = currentQuestion.question
        iv_image.setImageResource(currentQuestion.image)
        tv_option_one.text = currentQuestion.optionOne
        tv_option_two.text = currentQuestion.optionTwo
        tv_option_three.text = currentQuestion.optionThree
        tv_option_four.text = currentQuestion.optionFour

    }
}