package com.tjknsoft.tjflagquiz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import kotlinx.android.synthetic.main.activity_quiz_chooseflag.*
import java.text.DecimalFormat
import android.graphics.ColorMatrixColorFilter

import android.graphics.ColorMatrix


class QuizChooseFlagActivity : AppCompatActivity(), View.OnClickListener {

    private var mCurrentQuestionNumber = 1
    private var mQuestionList: ArrayList<Question> = arrayListOf()
    private var mSelectedOptionPosition: Int = 0 //0 = no options selected
    private var mScoreAbsolute: Int = 0
    private var mScorePercentage: Float = 0.0F
    private var mAllNames: ArrayList<String> = arrayListOf()
    private var mSelectedNames: ArrayList<String> = arrayListOf()
    private var mQuestionSize: Int = 10
    private var mAnswerSize: Int = 4
    private var mMapCountryToFlag = mutableMapOf<String, Int>()
    private var mOptionImageViews: ArrayList<ImageView> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_chooseflag)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        onCreateHelper() // when user restart, call this (do not call onCreate() according to documentation)
    }

    private fun onCreateHelper() {

        mCurrentQuestionNumber = 1
        mAllNames = Constants.CountryNames
        mSelectedNames = selectRandomNames(mAllNames, mQuestionSize, null)
        Log.i("PANJUTA", "mSelectedNames size: ${mSelectedNames.size}")
        mapCountryNameToFlagImage(mAllNames)
        createQuestionsData(mSelectedNames)
        mScoreAbsolute = 0
        mScorePercentage = 0.0F
        score_progress_bar2.progress = mScoreAbsolute
        tv_title2.text = "What is the flag of this country?"

        setQuestion()

        for (optionimg in mOptionImageViews) {
            optionimg.setOnClickListener(this)
        }
        btn_submit2.setOnClickListener(this)
        btn_quit2.setOnClickListener(this)
        btn_restart2.setOnClickListener(this)

    }

    private fun setQuestion() {

        setDefaultOptionsView()
        enableOptions(true)
        for (iv in mOptionImageViews) {
            clearGreyTint(iv)
        }

        ll_restartOrQuit2.visibility = View.GONE
        btn_submit2.visibility = View.VISIBLE
        btn_submit2.text = "CHECK ANSWER"

        var currentQuestion: Question = mQuestionList[mCurrentQuestionNumber - 1]

        question_progress_bar2.progress = mCurrentQuestionNumber
        tv_question_progress2.text =
            mCurrentQuestionNumber.toString() + " of ${mQuestionList.size}"
        tv_score_progress2.text =
            "$mScoreAbsolute of ${mCurrentQuestionNumber - 1}"

        tv_question_countryname.text = currentQuestion.countryName
        iv_option_1.setImageResource(currentQuestion.optionImages[0])
        iv_option_2.setImageResource(currentQuestion.optionImages[1])
        iv_option_3.setImageResource(currentQuestion.optionImages[2])
        iv_option_4.setImageResource(currentQuestion.optionImages[3])

    }

    private fun setDefaultOptionsView() {

        mOptionImageViews.clear()
        mOptionImageViews.add(0, iv_option_1)
        mOptionImageViews.add(1, iv_option_2)
        mOptionImageViews.add(2, iv_option_3)
        mOptionImageViews.add(3, iv_option_4)

        for (optionimg in mOptionImageViews) {
            optionimg.setPadding(0)
            optionimg.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }
    }

    private fun optionImageViewSelected(iv: ImageView, optionNumber: Int) {
        setDefaultOptionsView()
        mSelectedOptionPosition = optionNumber
        iv.setPadding(16)
        iv.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_option_1 -> {
                Log.i(
                    "PANJUTA",
                    "R.id.iv_option_1 clicked"
                )
                optionImageViewSelected(iv_option_1, 1)
            }
            R.id.iv_option_2 -> {
                optionImageViewSelected(iv_option_2, 2)
            }
            R.id.iv_option_3 -> {
                optionImageViewSelected(iv_option_3, 3)
            }
            R.id.iv_option_4 -> {
                optionImageViewSelected(iv_option_4, 4)
            }
            R.id.btn_submit2 -> {
                Log.i(
                    "PANJUTA",
                    "btn_submit2 clicked"
                )
                if (mSelectedOptionPosition == 999) {
                    finish()
                } else if (mSelectedOptionPosition == 0) {
                    Toast.makeText(this, "Please choose your answer", Toast.LENGTH_SHORT)
                        .show()
                } else if (mSelectedOptionPosition == 99) { // corect & wrong answer have been shown
                    mCurrentQuestionNumber++
                    if (mCurrentQuestionNumber <= mQuestionList.size) {
                        setQuestion()
                    } else {
                        Toast.makeText(this, "You have finished this quiz", Toast.LENGTH_SHORT)
                            .show()
                    }
                    mSelectedOptionPosition = 0
                } else { // user has selected one of the options
                    val question = mQuestionList[mCurrentQuestionNumber - 1]
                    if (mSelectedOptionPosition != question.correctPosition) {
                        // setAnswerText(mSelectedOptionPosition, false)
                        setAnswerColor(
                            mSelectedOptionPosition,
                            android.R.color.holo_red_light
                        ) // wrong answer color
                    } else {
                        mScoreAbsolute++
                        score_progress_bar2.progress = mScoreAbsolute
                    }

                    mScorePercentage =
                        (mScoreAbsolute.toFloat() / mCurrentQuestionNumber.toFloat()) * 100

                    tv_title2.text = "Your Score: "

                    val myDecimalFormat = DecimalFormat("#.##")
                    tv_title2.text = "Your Score: ${myDecimalFormat.format(mScorePercentage)}%"
                    tv_score_progress2.text =
                        "$mScoreAbsolute of $mCurrentQuestionNumber"

                    Log.i(
                        "PANJUTA",
                        "mScoreAbsolute: $mScoreAbsolute,  mCurrentQuestionNumber: $mCurrentQuestionNumber, mScorePercentage: $mScorePercentage"
                    )

                    Log.i(
                        "PANJUTA",
                        "question.correctPosition: ${question.correctPosition}"
                    )
                    setAnswerColor(
                        question.correctPosition,
                        android.R.color.holo_green_light
                    ) // correct answer color
                    // setAnswerText(question.correctPosition, true)
                    enableOptions(false)

                    for ((index, optionImage) in mOptionImageViews.withIndex()) {
                        Log.i(
                            "PANJUTA",
                            "index: $index"
                        )
                        if (index != (question.correctPosition - 1)) {
                            setGreyTint(mOptionImageViews[index])
                        }
                    }

                    if (mCurrentQuestionNumber == mQuestionList.size) {
                        btn_submit2.visibility = View.GONE
                        ll_restartOrQuit2.visibility = View.VISIBLE
                        // btn_submit.text = "FINISH"    `
                        // mSelectedOptionPosition = 999 // quit the app
                    } else {
                        btn_submit2.text = "NEXT QUESTION"
                        mSelectedOptionPosition = 99 // user have submitted his decision

                    }
                }
            }
            R.id.btn_quit2 -> finish()
            R.id.btn_restart2 -> {// onCreateHelper()
                mQuestionList.clear()
                val intent = Intent(this, SplashScreenActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setAnswerColor(answer: Int, optionBackgroundResID: Int) {

        when (answer) {
            1 -> {
                iv_option_1.background = ContextCompat.getDrawable(this, optionBackgroundResID)
                iv_option_1.setPadding(16)
            }
            2 -> {
                iv_option_2.background = ContextCompat.getDrawable(this, optionBackgroundResID)
                iv_option_2.setPadding(16)
            }

            3 -> {
                iv_option_3.background = ContextCompat.getDrawable(this, optionBackgroundResID)
                iv_option_3.setPadding(16)

            }
            4 -> {
                iv_option_4.background = ContextCompat.getDrawable(this, optionBackgroundResID)
                iv_option_4.setPadding(16)
            }
        }
    }

    private fun setGreyTint(v: ImageView) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f) //0 means grayscale
        val cf = ColorMatrixColorFilter(matrix)
        v.colorFilter = cf
        v.imageAlpha = 64 // 128 = 0.5
    }

    fun clearGreyTint(v: ImageView) {
        v.colorFilter = null
        v.imageAlpha = 255
    }

    private fun enableOptions(clickable: Boolean) {
        Log.i(
            "PANJUTA",
            "inside enableOptions(), mOptionImageViews size: ${mOptionImageViews.size}"
        )
        for (optionimg in mOptionImageViews) {
            optionimg.isClickable = clickable
            /*
            optionimg.setOnClickListener(
                View.OnClickListener
                //@Override
                { Log.v("PANJUTA", " click") })
                */
        }
    }


    private fun createQuestionsData(selectedNames: ArrayList<String>) {

        selectedNames.forEachIndexed { index, countryName ->
            // Log.i("PANJUTA", "forEachIndexed index: $index")
            val (correctPosition, answers) = createAnswers(countryName)
            mQuestionList.add(
                Question(
                    index, "Your Score: $mScorePercentage", 0, countryName,
                    arrayListOf<String>(), answers, correctPosition
                )
            )

            for ((index, question) in mQuestionList.withIndex()) {
                Log.i(
                    "PANJUTA",
                    "question: ${mQuestionList[index]}, Question Name: ${mMapCountryToFlag[selectedNames[index]]}"
                )
            }

        }
    }

    private fun createAnswers(questionCountryName: String): Pair<Int, ArrayList<Int>> {
        // get correct Flag image resource ID from its country name
        val correctInt = mMapCountryToFlag[questionCountryName]
        val answersBeforeShuffle: ArrayList<Int> = arrayListOf()

        // pick 3 unique country names from AllNames;
        // check that this 3 is different from the correct name, ie. wrong answers
        val wrongAnswersString = selectRandomNames(mAllNames, mAnswerSize - 1, questionCountryName)

        // add 3 wrong answers to an array of answers
        for (wrongAnswerString in wrongAnswersString) {
            answersBeforeShuffle.add(mMapCountryToFlag[wrongAnswerString]!!)
        }

        // afterwards, add 1 correct country name into the array
        if (correctInt != null) {
            answersBeforeShuffle.add(correctInt)
        }

        // check answers array before shuffled
        for (answer in answersBeforeShuffle) {
            Log.i("PANJUTA", "answer before shuffle: $answer")
        }

        // then, shuffle the array
        val answersAfterShuffle: ArrayList<Int> =
            answersBeforeShuffle.shuffled() as ArrayList<Int>

        // check answers array after shuffled
        // afterwards, check the position number (index + 1) of the correct answer
        var correctPosition = 0
        for ((index, answer) in answersAfterShuffle.withIndex()) {
            Log.i("PANJUTA", "answer after shuffle: $answer")
            if (answer == correctInt) correctPosition = index + 1
        }
        Log.i("PANJUTA", "correctPosition: $correctPosition")

        return Pair(correctPosition, answersAfterShuffle)

    }

    private fun selectRandomNames(
        allNames: ArrayList<String>,
        questionSize: Int,
        excludedName: String?
    ): ArrayList<String> {
        // Avoid a deadlock
        if (questionSize >= allNames.size) {
            return allNames
        }
        val selectedNames: ArrayList<String> = arrayListOf()
        // Get a random item until we got the requested amount
        while (selectedNames.size < questionSize) {
            val randomIndex = (0..allNames.size - 1).random()
            val element = allNames[randomIndex]
            if (!selectedNames.contains(element) && (!element.equals(excludedName))) {
                selectedNames.add(element)
            }
        }
        Log.i("PANJUTA", "inside selectRandomNames(), selectedNames size: ${selectedNames.size}")

        // display selected country names in logcat
        val value = TypedValue()
        for (name in selectedNames) {
            Log.i("PANJUTA", "Selected country names: $name")
        }

        return selectedNames
    }

    private fun mapCountryNameToFlagImage(mAllNames: ArrayList<String>) {
        for ((index, name) in mAllNames.withIndex()) {
            mMapCountryToFlag.put(name, SplashScreenActivity.mAllFlags[index])
            Log.i("PANJUTA", "country name: $name, ID: ${SplashScreenActivity.mAllFlags[index]}")
        }
    }


}