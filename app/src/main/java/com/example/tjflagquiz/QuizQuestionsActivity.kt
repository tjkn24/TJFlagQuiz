package com.example.tjflagquiz

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_quiz_questions.*

class QuizQuestionsActivity : AppCompatActivity(), View.OnClickListener {

    private var mCurrentQuestionNumber = 1
    private lateinit var mQuestionList: ArrayList<Question>
    private var mSelectedOptionPosition: Int = 0 //answer tv 1 to 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_questions)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        setQuestion()

        tv_option_one.setOnClickListener(this)
        tv_option_two.setOnClickListener(this)
        tv_option_three.setOnClickListener(this)
        tv_option_four.setOnClickListener(this)
        btn_submit.setOnClickListener(this)
    }

    private fun setQuestion() {
        setDefaultOptionsView()

        mQuestionList = Constants.getQuestions()

        if(mCurrentQuestionNumber == mQuestionList.size){
            btn_submit.text = "FINISH"
        } else {
            btn_submit.text = "SUBMIT"
        }

        var currentQuestion: Question = mQuestionList[mCurrentQuestionNumber - 1]

        my_progress_bar.progress = mCurrentQuestionNumber
        tv_progress.text = mCurrentQuestionNumber.toString() + "/${mQuestionList.size}"
        tv_question.text = currentQuestion.question
        iv_image.setImageResource(currentQuestion.image)
        tv_option_one.text = currentQuestion.optionOne
        tv_option_two.text = currentQuestion.optionTwo
        tv_option_three.text = currentQuestion.optionThree
        tv_option_four.text = currentQuestion.optionFour
    }

    private fun setDefaultOptionsView() {
        val options = ArrayList<TextView>()
        options.add(0, tv_option_one)
        options.add(1, tv_option_two)
        options.add(2, tv_option_three)
        options.add(3, tv_option_four)

        for (option in options) {
            option.setTextColor(Color.parseColor("#808080"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(this, R.drawable.tv_border)
        }
    }

    private fun optionViewSelected(tv: TextView, optionNumber: Int) {
        setDefaultOptionsView()
        mSelectedOptionPosition = optionNumber
        tv.setTextColor(Color.parseColor("#3634a3"))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.background = ContextCompat.getDrawable(this, R.drawable.tv_border_selected)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_option_one -> {
                optionViewSelected(tv_option_one, 1)
            }
            R.id.tv_option_two -> {
                optionViewSelected(tv_option_two, 2)
            }
            R.id.tv_option_three -> {
                optionViewSelected(tv_option_three, 3)
            }
            R.id.tv_option_four -> {
                optionViewSelected(tv_option_four, 4)
            }
            R.id.btn_submit -> {
                if (mSelectedOptionPosition == 0) {
                    mCurrentQuestionNumber++
                    if (mCurrentQuestionNumber <= mQuestionList.size) {
                        setQuestion()
                    } else {
                        Toast.makeText(this, "You have finished this quiz", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    val question = mQuestionList[mCurrentQuestionNumber - 1]
                    if(mSelectedOptionPosition != question.correctAnswer){
                        setAnswerColor(mSelectedOptionPosition, R.drawable.tv_border_wrong)
                    }
                    setAnswerColor(question.correctAnswer, R.drawable.tv_border_correct)
                    if(mCurrentQuestionNumber == mQuestionList.size){
                        btn_submit.text = "FINISH"
                    } else {
                        btn_submit.text = "GO TO THE NEXT QUESTION"
                    }
                    mSelectedOptionPosition = 0
                }
            }
        }
    }

    private fun setAnswerColor(answer: Int, optionViewResID: Int) {
        when (answer) {
            1 -> tv_option_one.background = ContextCompat.getDrawable(this, optionViewResID)
            2 -> tv_option_two.background = ContextCompat.getDrawable(this, optionViewResID)
            3 -> tv_option_three.background = ContextCompat.getDrawable(this, optionViewResID)
            4 -> tv_option_four.background = ContextCompat.getDrawable(this, optionViewResID)
        }
    }


}