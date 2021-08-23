package com.example.tjflagquiz

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_quiz_questions.*
import org.w3c.dom.Text

class QuizQuestionsActivity : AppCompatActivity(), View.OnClickListener {

    private var mCurrentPosition = 1
    private lateinit var mQuestionList: ArrayList<Question>
    private var selectedOptionPosition: Int = 0 //answer tv 1 to 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_questions)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        setQuestion()

        tv_option_one.setOnClickListener(this)
        tv_option_two.setOnClickListener(this)
        tv_option_three.setOnClickListener(this)
        tv_option_four.setOnClickListener(this)
    }

    private fun setQuestion(){
        setDefaultOptionsView()

        mQuestionList = Constants.getQuestions()
        var currentQuestion : Question = mQuestionList[mCurrentPosition - 1]

        my_progress_bar.progress = mCurrentPosition
        tv_progress.text = mCurrentPosition.toString() + "/${my_progress_bar.max}"
        tv_question.text = currentQuestion.question
        iv_image.setImageResource(currentQuestion.image)
        tv_option_one.text = currentQuestion.optionOne
        tv_option_two.text = currentQuestion.optionTwo
        tv_option_three.text = currentQuestion.optionThree
        tv_option_four.text = currentQuestion.optionFour
    }

    private fun setDefaultOptionsView(){
        val options = ArrayList<TextView>()
        options.add(0, tv_option_one)
        options.add(1, tv_option_two)
        options.add(2, tv_option_three)
        options.add(3, tv_option_four)

        for (option in options){
            option.setTextColor(Color.parseColor("#808080"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(this, R.drawable.tv_border)
        }
    }

    override fun onClick(p0: View?) {

    }

    private fun OptionViewSelected (tv: TextView, optionNumber: Int){

    }
}