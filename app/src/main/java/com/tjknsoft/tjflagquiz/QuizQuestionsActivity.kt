package com.tjknsoft.tjflagquiz

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_quiz_questions.*
import java.text.DecimalFormat
import android.util.TypedValue


class QuizQuestionsActivity : AppCompatActivity(), View.OnClickListener {

    private var mCurrentQuestionNumber = 1
    private var mQuestionList: ArrayList<Question> = arrayListOf()
    private var mSelectedOptionPosition: Int = 0 //0 = no options selected
    private var mScoreAbsolute: Int  = 0
    private var mScorePercentage: Float = 0.0F
    private val mAllFlags: ArrayList<Int> = arrayListOf()
    private var mSelectedFlags: ArrayList<Int> = arrayListOf()
    private var mQuestionSize: Int = 10
    private var mAnswerSize: Int = 4
    private var mMapFlagToCountry = mutableMapOf<Int, String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_questions)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        loadDrawables()
        onCreateHelper() // when user restart, call this (do not call onCreate() according to documentation)
    }

    private fun onCreateHelper() {

        mQuestionList.clear()
        mCurrentQuestionNumber = 1
        mSelectedFlags = selectRandomFlags(mAllFlags, mQuestionSize, null)
        mapFlagImageToCountryName(mAllFlags)
        createQuestionsData(mSelectedFlags)
        mScoreAbsolute = 0
        mScorePercentage = 0.0F
        score_progress_bar.progress = mScoreAbsolute
        tv_question.text = "What country's flag is this?"

        setQuestion()

        tv_option_one.setOnClickListener(this)
        tv_option_two.setOnClickListener(this)
        tv_option_three.setOnClickListener(this)
        tv_option_four.setOnClickListener(this)
        btn_submit.setOnClickListener(this)
        btn_quit.setOnClickListener(this)
        btn_restart.setOnClickListener(this)
    }

    private fun setQuestion() {
        setDefaultOptionsView()
        enableOptions(true)

        // mQuestionList is not hardcoded anymore
        // mQuestionList = Constants.getQuestions()

        ll_restartOrQuit.setVisibility(View.GONE)
        btn_submit.setVisibility(View.VISIBLE)
        btn_submit.text = "CHECK ANSWER"

        Log.i("PANJUTA", "mScoreAbsolute: $mScoreAbsolute, mScorePercentage: $mScorePercentage")

        var currentQuestion: Question = mQuestionList[mCurrentQuestionNumber - 1]

        Log.i("PANJUTA", "mCurrentQuestionNumber: $mCurrentQuestionNumber, currentQuestion: $currentQuestion")

        question_progress_bar.progress = mCurrentQuestionNumber
        tv_question_progress.text = mCurrentQuestionNumber.toString() + " of ${mQuestionList.size}"
        tv_score_progress.text =
            "${mScoreAbsolute.toString()} of ${mCurrentQuestionNumber-1}"


        iv_image.setImageResource(currentQuestion.image)
        tv_option_one.text = currentQuestion.options[0]
        tv_option_two.text = currentQuestion.options[1]
        tv_option_three.text = currentQuestion.options[2]
        tv_option_four.text = currentQuestion.options[3]
    }

    private fun setDefaultOptionsView() {
        val optiontvs = ArrayList<TextView>()
        optiontvs.add(0, tv_option_one)
        optiontvs.add(1, tv_option_two)
        optiontvs.add(2, tv_option_three)
        optiontvs.add(3, tv_option_four)

        for (optiontv in optiontvs) {
            optiontv.setTextColor(Color.parseColor("#808080"))
            optiontv.typeface = Typeface.DEFAULT
            optiontv.background = ContextCompat.getDrawable(this, R.drawable.tv_border)
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
                if (mSelectedOptionPosition == 999) {
                    finish()
                } else if (mSelectedOptionPosition == 0) {
                    Toast.makeText(this, "Please choose your answer", Toast.LENGTH_SHORT)
                        .show()
                } else if (mSelectedOptionPosition == 99) {
                    mCurrentQuestionNumber++
                    if (mCurrentQuestionNumber <= mQuestionList.size) {
                        setQuestion()
                    } else {
                        Toast.makeText(this, "You have finished this quiz", Toast.LENGTH_SHORT)
                            .show()
                    }
                    mSelectedOptionPosition = 0
                } else {
                    val question = mQuestionList[mCurrentQuestionNumber - 1]
                    if (mSelectedOptionPosition != question.correctPosition) {
                        setAnswerText(mSelectedOptionPosition, false)
                        setAnswerColor(mSelectedOptionPosition, R.drawable.tv_border_wrong)
                    } else {
                        mScoreAbsolute++
                        score_progress_bar.progress = mScoreAbsolute
                    }

                    mScorePercentage =
                        (mScoreAbsolute.toFloat() / mCurrentQuestionNumber.toFloat()) * 100

                    tv_question.text = "Your Score: "

                    val myDecimalFormat = DecimalFormat("#.##")
                    tv_question.text = "Your Score: ${myDecimalFormat.format(mScorePercentage)}%"
                    tv_score_progress.text =
                        "${mScoreAbsolute.toString()} of $mCurrentQuestionNumber"

                    Log.i(
                        "PANJUTA",
                        "mScoreAbsolute: $mScoreAbsolute,  mCurrentQuestionNumber: $mCurrentQuestionNumber, mScorePercentage: ${mScorePercentage.toString()}"
                    )

                    setAnswerColor(question.correctPosition, R.drawable.tv_border_correct)
                    setAnswerText(question.correctPosition, true)
                    enableOptions(false)

                    if (mCurrentQuestionNumber == mQuestionList.size) {
                        btn_submit.setVisibility(View.GONE)
                        ll_restartOrQuit.setVisibility(View.VISIBLE)
                        // btn_submit.text = "FINISH"    `
                        // mSelectedOptionPosition = 999 // quit the app
                    } else {
                        btn_submit.text = "NEXT QUESTION"
                        mSelectedOptionPosition = 99 // user have submitted his decision

                    }

                }
            }
            R.id.btn_quit -> finish()
            R.id.btn_restart -> onCreateHelper()
        }
        Log.i("PANJUTA", "mSelectedOptionPosition: $mSelectedOptionPosition")
    }

    private fun setAnswerText(answer: Int, isCorrect: Boolean) {
        when (answer) {
            1 -> {
                tv_option_one.text = "" // Remove old text
                tv_option_one.customAppend(
                    mQuestionList[mCurrentQuestionNumber - 1].options[0],
                    android.R.color.black
                )
                formatAnswerText(isCorrect, tv_option_one)
            }
            2 -> {
                tv_option_two.text = "" // Remove old text
                tv_option_two.customAppend(
                    mQuestionList[mCurrentQuestionNumber - 1].options[1],
                    android.R.color.black
                )
                formatAnswerText(isCorrect, tv_option_two)
            }
            3 -> {
                tv_option_three.text = "" // Remove old text
                tv_option_three.customAppend(
                    mQuestionList[mCurrentQuestionNumber - 1].options[2],
                    android.R.color.black
                )
                formatAnswerText(isCorrect, tv_option_three)
            }
            4 -> {
                tv_option_four.text = "" // Remove old text
                tv_option_four.customAppend(
                    mQuestionList[mCurrentQuestionNumber - 1].options[3],
                    android.R.color.black
                )
                formatAnswerText(isCorrect, tv_option_four)
            }
        }
    }

    private fun formatAnswerText(isCorrect: Boolean, optionView: TextView) {
        optionView.setTypeface(optionView.getTypeface(), Typeface.BOLD)
        if (isCorrect == true) {
            optionView.customAppend("   \u2714", android.R.color.holo_green_light)
        } else {
            optionView.customAppend("   \u2716", android.R.color.holo_red_light)
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

    private fun enableOptions(clickable: Boolean) {
        tv_option_one.setClickable(clickable)
        tv_option_two.setClickable(clickable)
        tv_option_three.setClickable(clickable)
        tv_option_four.setClickable(clickable)
    }

    // load only flag pngs from drawables
    private fun loadDrawables() {
        for (identifier in R.drawable.flag_afghanistan..R.drawable.flag_zimbabwe) {
            val name = resources.getResourceEntryName(identifier.toInt())
            //name is the file name without the extension, identifier is the resource ID
            mAllFlags.add(identifier)
            // Log.i("PANJUTA", "All File name & Resource Id: $name $identifier")

        }
        // for (flagIdentifier in mAllFlags) {
            // Log.i("PANJUTA", "All resource ID: $flagIdentifier")
        // };
        Log.i("PANJUTA", "mAllFlags.size: ${mAllFlags.size}")
    }

    // get some unique random flags to be used in the game

    private fun selectRandomFlags(
        allFlags: ArrayList<Int>,
        questionSize: Int,
        excludedFlag: Int?
    ): ArrayList<Int> {

        // Avoid a deadlock
        if (questionSize >= allFlags.size) {
            return allFlags
        }
        val selectedFlags: ArrayList<Int> = arrayListOf()

        // Get a random item until we got the requested amount
        while (selectedFlags.size < questionSize) {
            val randomIndex = (0..allFlags.size - 1).random()
            // Log.i("PANJUTA", "random index: $randomIndex")
            val element = allFlags[randomIndex]
            if (!selectedFlags.contains(element) && (element != excludedFlag)) {
                selectedFlags.add(element)
            }
        }

        // Log.i("PANJUTA", "selected flags size: ${selectedFlags.size}")

        // display selected flags filename (with its directory) in logcat
        val value = TypedValue()
        for ((i, f) in selectedFlags.withIndex()) {
            resources.getValue(f, value, true)
            // Log.i("PANJUTA", "#$i: $f, filename: ${value.string}")
        }
        return selectedFlags
    }

    private fun mapFlagImageToCountryName(allFlags: MutableList<Int>) {
        // Log.i("PANJUTA", "Entering mapFlagImageToCountryName()")
        for ((i, j) in allFlags.withIndex()) {
            // Log.i("PANJUTA", "i: $i, j: $j")
            mMapFlagToCountry.put(j, Constants.CountryNames[i])
        }
        Log.i("PANJUTA", "mMapFlagToCountry size: ${mMapFlagToCountry.size}")
        // Log.i("PANJUTA", "mMapFlagToCountry[2131165288]: ${mMapFlagToCountry[2131165288]}")
        // Log.i("PANJUTA", "mMapFlagToCountry[2131165291]: ${mMapFlagToCountry[2131165291]}")
    }

    private fun createQuestionsData(selectedFlags: ArrayList<Int>) {

        selectedFlags.forEachIndexed { index, questionFlag ->
            Log.i("PANJUTA", "forEachIndexed index: $index")
            val (correctPosition, answers) = createAnswers(questionFlag)
            mQuestionList.add(
                Question(
                    index, "Your Score: $mScorePercentage", questionFlag,
                    answers, correctPosition
                )
            )
            for ((index, question) in mQuestionList.withIndex()){
                Log.i("PANJUTA", "question: ${mQuestionList[index]}, Question Flag: ${mMapFlagToCountry[selectedFlags[index]]}")
            }

        }
    }

    private fun createAnswers(questionFlag: Int): Pair<Int, ArrayList<String>> {

        // get correct Country Name from its resource ID
        val correctString = mMapFlagToCountry[questionFlag]
        Log.i("PANJUTA", "questionFlag: $questionFlag, correctString: $correctString")

        val answersBeforeShuffle: ArrayList<String> = arrayListOf()
        // pick 3 unique random flags from AllFlags;
        // check that this 3 is different from the correct flag, ie. wrong answers
        val wrongAnswersID = selectRandomFlags(mAllFlags, mAnswerSize - 1, questionFlag)

        // add 3 wrong answers to an array of answers
        for (wrongAnswerID in wrongAnswersID) {
            answersBeforeShuffle.add(mMapFlagToCountry[wrongAnswerID]!!)
        }
        // afterwards, add 1 correct flag to this into the array
        if (correctString != null) {
            answersBeforeShuffle.add(correctString)
        }

        // check answers array before shuffled
        for (answer in answersBeforeShuffle) {
            Log.i("PANJUTA", "answer before shuffle: $answer")
        }

        // then, shuffle the array
        val answersAfterShuffle: ArrayList<String> =
            answersBeforeShuffle.shuffled() as ArrayList<String>

        // check answers array after shuffled
        // afterwards, check the position number (index + 1) of the correct answer
        var correctPosition = 0
        for ((index, answer) in answersAfterShuffle.withIndex()) {
            Log.i("PANJUTA", "answer after shuffle: $answer")
            if (answer == correctString) correctPosition = index + 1
        }
        Log.i("PANJUTA", "correctPosition: $correctPosition")

        return Pair(correctPosition, answersAfterShuffle)
    }

}