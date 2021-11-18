package com.tjknsoft.tjflagquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_quiz_flag_memory_hard.*
import kotlinx.android.synthetic.main.toast_image_layout.*
import kotlin.math.floor
import kotlin.random.Random
import android.view.MenuItem
import androidx.core.content.ContextCompat
import android.widget.Toast


class QuizFlagMemoryActivity : AppCompatActivity(), View.OnClickListener {

    private var mTileList: ArrayList<Tile> = arrayListOf()
    private var mAllFlagsResID: ArrayList<Int> = arrayListOf()

    // private var mAllCountryCodes: ArrayList<String> = arrayListOf()
    private var mAllCountryNames: ArrayList<String> = arrayListOf()
    private var mAllShortenedCountryNames: ArrayList<String> = arrayListOf()
    private var mNumberOfTiles = 0 // easy = 30, medium = 40, hard = 50
    private var mMapFlagResIDtoShortenedCountryName = mutableMapOf<Int, String>()
    private var mMapShortenedCountryNameToFlagResID = mutableMapOf<String, Int>()
    private var mMapShortenedCountryNameToCountryName = mutableMapOf<String, String>()
    private var mSelectedFlagsResID: ArrayList<Int> = arrayListOf()
    private var mSelectedShortenedCountryNames: ArrayList<String> = arrayListOf()
    private var mSelectedFlagResIDandShortenedCountryName: MutableList<Any> = arrayListOf()
    private var mShuffledSelectedFlagResIDandShortenedCountryName: ArrayList<Any> = arrayListOf()
    private var mFlagTiles: ArrayList<ImageView> = arrayListOf()
    private var mCountryTiles: ArrayList<TextView> = arrayListOf()
    private var mTappedFlagResID: Int = -1
    private var mTappedShortenedCountryName: String = "-1"
    private var mTappedCountryTileIndex: Int = -1
    private var mTappedFlagTileIndex: Int = -1
    private lateinit var mSound: SoundPoolPlayer
    private var mCurrentTaps = 0
    private var mBestTaps = 0
    private var mBestTime = 0.0
    private var mMatchedPairs = 0
    private var mOnResumeTime = 0.0
    private var mCurrentTime = 0.0
    private var mPreviousDuration = 0.0
    private lateinit var mTimerHandler: Handler
    private lateinit var mTimerRunnable: Runnable
    private var mFlagLastClickTime: Long = 0 // variable to track event time for flag tile
    private var mCountryLastClickTime: Long =
        0 // variable to track event time for country name tile
    private var mLastPairClickTime: Long =
        0 // event time when second member of a pair (flag or country) is clicked
    private var mIsBestTime = false
    private var mIsBestTaps = false
    private var mIsFirstGameCompleted = false
    var mIntentRequestCode = 0
    private var mIsSoundOn = true
    private lateinit var mMenu: Menu
    private var mIsLightTheme = true
    private var mIsGameRunning = true
    private var mIsMenuComplete = true
    private var mIsPairMatched = false
    private val mIdleDelayMinutes = 10
    private lateinit var mIdleHandler: Handler
    private lateinit var mIdleRunnable: Runnable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras // intent from splash activity
        if (bundle != null) {
            mNumberOfTiles = bundle.getInt("memory");
        }
        Log.i(
            "PANJUTA",
            "onCreate(): mNumberOfTiles: $mNumberOfTiles"
        )

        when (mNumberOfTiles) {
            30 -> setContentView(R.layout.activity_quiz_flag_memory_easy)
            40 -> setContentView(R.layout.activity_quiz_flag_memory_medium)
            50 -> setContentView(R.layout.activity_quiz_flag_memory_hard)
        }

        onCreateHelper()
    }

    private fun onCreateHelper() {

        // todo: easy medium hard layouts


        // clearSharedPreferences()

        setAppBarTitle()

        setBackgroundColor()

        setVariables()

        if (!getCheckBoxStatus()) {
            displayActivityAsDialog("menu_help")
        }

        setTimer()

        updateBestTapsTexView()

        updateBestTimeTexView()

        getAndMapData()

        selectAndShuffleTileContent()

        populateTileList()

        addViewsToViewList()

        drawTiles()

        // setOnClickListenerBottomButtons()

        mSound = SoundPoolPlayer(this)

    }

    private fun setAppBarTitle() {
        var title = ""
        when (mNumberOfTiles) {
            30 -> title = "Flag Memory - EASY"
            40 -> title = "Flag Memory - MEDIUM"
            50 -> title = "Flag Memory - HARD"
        }
        setTitle(title)
    }

    private fun setBackgroundColor() {
//        val gameBoard = findViewById<LinearLayout>(R.id.ll_game_board)
//
//        val timeAppBar = findViewById<LinearLayout>(R.id.ll_time)
//        val tapsAppBar = findViewById<LinearLayout>(R.id.ll_taps)

        if (!mIsLightTheme) {
            ll_game_board.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))

            ll_time.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme))
            tv_time_label.setTextColor(getResources().getColor(R.color.backgroundPage2))
            ll_time_best.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundPage3))
            tv_time_best_label.setTextColor(getResources().getColor(R.color.backgroundPage2))
            tv_time_best_timer.setTextColor(getResources().getColor(R.color.backgroundPage2))
            ll_time_current.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.backgroundPage3
                )
            )
            tv_time_current_label.setTextColor(getResources().getColor(R.color.backgroundPage2))
            tv_time_current_timer.setTextColor(getResources().getColor(R.color.backgroundPage2))

            ll_taps.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme))
            tv_taps_label.setTextColor(getResources().getColor(R.color.backgroundPage2))
            ll_taps_best.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundPage3))
            tv_taps_best_label.setTextColor(getResources().getColor(R.color.backgroundPage2))
            tv_taps_best_amount.setTextColor(getResources().getColor(R.color.backgroundPage2))
            ll_taps_current.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.backgroundPage3
                )
            )
            tv_taps_current_label.setTextColor(getResources().getColor(R.color.backgroundPage2))
            tv_taps_current_amount.setTextColor(getResources().getColor(R.color.backgroundPage2))
        } else {
            ll_game_board.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundPage2))

            ll_time.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            tv_time_label.setTextColor(getResources().getColor(R.color.grey))
            ll_time_best.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            tv_time_best_label.setTextColor(getResources().getColor(R.color.grey))
            tv_time_best_timer.setTextColor(getResources().getColor(R.color.grey))
            ll_time_current.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            tv_time_current_label.setTextColor(getResources().getColor(R.color.grey))
            tv_time_current_timer.setTextColor(getResources().getColor(R.color.grey))

            ll_taps.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            tv_taps_label.setTextColor(getResources().getColor(R.color.grey))
            ll_taps_best.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            tv_taps_best_label.setTextColor(getResources().getColor(R.color.grey))
            tv_taps_best_amount.setTextColor(getResources().getColor(R.color.grey))
            ll_taps_current.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            tv_taps_current_label.setTextColor(getResources().getColor(R.color.grey))
            tv_taps_current_amount.setTextColor(getResources().getColor(R.color.grey))
        }

    }

    // @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            this.mMenu = menu
        }
        menuInflater.inflate(R.menu.options_menu, menu)

        if (!mIsMenuComplete) {
            mMenu.findItem(R.id.menu_theme).isVisible = false
            mMenu.findItem(R.id.menu_mute).isVisible = false
            mMenu.findItem(R.id.menu_help).isVisible = false
        }
//
//        if (menu is MenuBuilder) {
//            menu.setOptionalIconsVisible(true)
//        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.menu_theme -> {
                // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                if (mIsGameRunning) {
                    toggleMenuItem(R.id.menu_theme)
                    mIsLightTheme = !mIsLightTheme
                    setBackgroundColor()
                    drawTiles()
                    true
                } else false
            }

            R.id.menu_mute -> {
                if (mIsGameRunning) {
                    toggleMenuItem(R.id.menu_mute)
                    if (mIsSoundOn) {
                        Toast.makeText(this, "Audio: OFF", Toast.LENGTH_SHORT).show()
                        for (flag in mFlagTiles) {
                            flag.isSoundEffectsEnabled = false
                        }

                        for (country in mCountryTiles) {
                            country.isSoundEffectsEnabled = false
                        }

                        mIsSoundOn = false
                    } else {
                        Toast.makeText(this, "Audio: ON", Toast.LENGTH_SHORT).show()
                        for (flag in mFlagTiles) {
                            flag.isSoundEffectsEnabled = true
                        }

                        for (country in mCountryTiles) {
                            country.isSoundEffectsEnabled = true
                        }
                        mIsSoundOn = true
                    }
                    true
                } else false
            }

            R.id.menu_help -> {
                displayActivityAsDialog("menu_help")
                true
            }

            R.id.menu_restart -> {
                if (mIsGameRunning) {
//                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//                    builder.setMessage("The game is in progress. Are you sure to restart?")
//                        .setCancelable(false)
//                        .setPositiveButton("Yes") { dialog, id ->
//                            restartGame()
//                        }
//                        .setNegativeButton(
//                            "No"
//                        ) { dialog, id -> //  Action for 'NO' Button
//                            dialog.cancel()
//                        }
//                    //Creating dialog box
//                    val alert = builder.create()
//                    //Setting the title manually
//                    alert.setTitle("Restart")
//                    alert.show()
                    displayActivityAsDialog("menu_restart")
                    true
                } else {
                    restartGame()
                    true
                }
            }

            R.id.menu_quit -> {
                if (mIsGameRunning) {
//                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//                    builder.setMessage("The game is in pogress. Are you sure to quit?")
//                        .setCancelable(false)
//                        .setPositiveButton("Yes") { dialog, id ->
//                            quitGame()
//                        }
//                        .setNegativeButton(
//                            "No"
//                        ) { dialog, id -> //  Action for 'NO' Button
//                            dialog.cancel()
//                        }
//                    //Creating dialog box
//                    val alert = builder.create()
//                    //Setting the title manually
//                    alert.setTitle("Quit")
//                    alert.show()
                    displayActivityAsDialog("menu_quit")
                    true
                } else {
                    quitGame()
                    true
                }
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun toggleMenuItem(menu: Int) {
        val menuItem: MenuItem = mMenu.findItem(menu)
        if (menu == R.id.menu_mute) {
            if (mIsSoundOn) {
                menuItem.title = "Sound ON"
                menuItem.setIcon(R.drawable.ic_audio_off)
            } else {
                menuItem.title = "Sound OFF"
                menuItem.setIcon(R.drawable.ic_audio_on)
            }
        } else if (menu == R.id.menu_theme) {
            if (mIsLightTheme) {
                menuItem.title = "Dark"
                menuItem.setIcon(R.drawable.ic_dark)
            } else {
                menuItem.title = "Light"
                menuItem.setIcon(R.drawable.ic_light)
            }
        }
    }

    private fun setVariables() {
        mIsBestTaps = false
        mIsBestTime = false
        mIsFirstGameCompleted = getSharedPreferences(
            "mIsFirstGameCompletedKey",
            Context.MODE_PRIVATE
        ).getBoolean("first_game_completed", false) // false is the default value
        Log.i(
            "PANJUTA",
            "resetVariables: mIsFirstGameCompleted: $mIsFirstGameCompleted"
        )
        mIsLightTheme = true
        mIsGameRunning = true
        mIsMenuComplete = true
        mIsPairMatched = false

        mIdleHandler = Handler()
        mIdleRunnable = object : Runnable {
            override fun run() {
                Toast.makeText(
                    applicationContext,
                    "Do you want to continue the game?",
                    Toast.LENGTH_LONG
                ).show()

                displayActivityAsDialog("user_idle")
            }
        }
    }

    private fun storeCheckBoxStatus(isChecked: Boolean) {
        val prefs = getSharedPreferences("CheckBox", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("cb_status", isChecked)
        editor.apply()
    }

    private fun getCheckBoxStatus(): Boolean {
        val prefs = getSharedPreferences("CheckBox", MODE_PRIVATE)
        return prefs.getBoolean("cb_status", false)
    }

    private fun displayActivityAsDialog(dialog: String) {

//        // below code is from https://www.codingdemos.com/android-custom-alertdialog/
//        val myBuilder: android.app.AlertDialog.Builder =
//            android.app.AlertDialog.Builder(this@QuizFlagMemoryActivity)
//        val myView: View = layoutInflater.inflate(R.layout.dialog_instruction, null)
//        val myCheckBox: CheckBox = myView.findViewById(R.id.cb_do_not_show_again)
//        myBuilder.setView(myView)
//        myBuilder.setPositiveButton("OK",
//            DialogInterface.OnClickListener { dialogInterface, _ -> dialogInterface.dismiss() })
//        val myDialog: android.app.AlertDialog? = myBuilder.create()
//        myDialog?.show()
//
//        myCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
//            if (compoundButton.isChecked) {
//                storeCheckBoxStatus(true)
//            } else {
//                storeCheckBoxStatus(false)
//            }
//        })

        var myIntent = Intent()

        when (dialog) {
            "menu_help" -> {
                myIntent = Intent(this, InstructionActivity::class.java)
                mIntentRequestCode = 0
            }
            "menu_restart" -> {
                myIntent = Intent(this, RestartActivity::class.java)
                mIntentRequestCode = 1
            }
            "menu_quit" -> {
                myIntent = Intent(this, QuitActivity::class.java)
                mIntentRequestCode = 2
            }
            "user_idle" -> {
                myIntent = Intent(this, IdleActivity::class.java)
                myIntent.putExtra("idleDelayedMinutes", mIdleDelayMinutes)
                mIntentRequestCode = 3
            }

        }

        startActivityForResult(myIntent, mIntentRequestCode)
    }

    // This method is called when the InstructionActivity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(
            "PANJUTA",
            "Back to FlagMemoryActivity; inside onActivityResult(), requestCode: $requestCode, resultCode: $resultCode, data: ${data.toString()} "
        )

        // Check that it is the SecondActivity with an OK result
        if (requestCode == mIntentRequestCode && resultCode == Activity.RESULT_OK) {
            if (mIntentRequestCode == 0) {
                // Get String data from Intent
                val checkBoxStatus = data!!.getBooleanExtra("KeyCheckBox", false)
//                Log.i(
//                    "PANJUTA",
//                    "Back to FlagMemoryActivity; checkBoxStatus: $checkBoxStatus"
//                )
                storeCheckBoxStatus(checkBoxStatus)

            } else if (mIntentRequestCode == 1) {
                val restartStatus = data!!.getStringExtra("KeyRestart")
                if (restartStatus == "Restart") {
                    mIdleHandler.removeCallbacks(mIdleRunnable)
                    restartGame()
                }
            } else if (mIntentRequestCode == 2) {
                val quitStatus = data!!.getStringExtra("KeyQuit")
                if (quitStatus == "Quit") {
                    mIdleHandler.removeCallbacks(mIdleRunnable)
                    quitGame()
                }
            } else if (mIntentRequestCode == 3) {
                val idleStatus = data!!.getStringExtra("KeyIdle")
                if (idleStatus == "TimeIsUp") {
                    mIdleHandler.removeCallbacks(mIdleRunnable)
                    quitGame()
                }
            }
        }
    }

    private fun setTimer() {
        mTimerHandler = Handler()
        mTimerRunnable = object : Runnable {
            override fun run() {
                mCurrentTime = System.currentTimeMillis() - mOnResumeTime + mPreviousDuration

                val minutes = mCurrentTime / 60000

                // if game time reach 60 minutes -> restart the game
                if (minutes > 60) {
                    mTimerHandler.removeCallbacks(mTimerRunnable)
                    mIdleHandler.removeCallbacks(mIdleRunnable)
                    // btn_restart3.performClick()
                    restartGame()
                }

                val intMinutes = floor(minutes)
                val seconds = (minutes - intMinutes) * 60.0
                val intSeconds = floor(seconds)
                val hundreths = (seconds - intSeconds) * 100.0

                // tv_current_timer.setText("${minutes.toString()}:${seconds.toString()}")
                tv_time_current_timer.text = String.format(
                    "%02d:%02d:%02d",
                    intMinutes.toInt(),
                    intSeconds.toInt(),
                    hundreths.toInt()
                )
                mTimerHandler.postDelayed(this, 100)
            }
        }
        mTimerHandler.postDelayed(mTimerRunnable, 0)
    }

    override fun onPause() {
        super.onPause()
        mTimerHandler.removeCallbacks(mTimerRunnable) // stop the timer
        mPreviousDuration = mCurrentTime // save the game duration before paused
        // this duration will be added to the next game time after resumed
        // 'added', because mStartTime is reset when game resumed
        Log.i(
            "PANJUTA",
            "onPause() called"
        )
    }

    override fun onResume() {
        super.onResume()
        if (mIsGameRunning) {
            mOnResumeTime = System.currentTimeMillis().toDouble()
            mTimerHandler.postDelayed(mTimerRunnable, 0)

            delayedIdle()
        }
    }

    private fun getAndMapData() {

        mAllCountryNames = Constants.allCountryNames
        mAllShortenedCountryNames = Constants.allShortenedCountryNames
        mAllFlagsResID = SplashScreenActivity.mAllFlagsResID
        mapFlagResIDToShortenedCountryName(mAllFlagsResID)
        mapShortenedCountryNameToFlagResID(mAllShortenedCountryNames)
        mapShortenedCountryNameToCountryName(mAllShortenedCountryNames)
    }

    private fun drawTiles() {

        // draw tiles
        for ((index, tile) in mTileList.withIndex()) {
            if (tile.shortenedCountryName != "") {
                // tile displays shortened country name
                mFlagTiles[index].setVisibility(View.GONE)
                mCountryTiles[index].setVisibility(View.VISIBLE)

                if (mIsLightTheme) {
                    mCountryTiles[index].setBackgroundColor(getResources().getColor(R.color.white))
                    mCountryTiles[index].setTextColor(getResources().getColor(R.color.grey))
                } else {
                    mCountryTiles[index].setBackground(getResources().getDrawable(R.drawable.round_corner))
                    mCountryTiles[index].setTextColor(getResources().getColor(R.color.light_grey))
                }

                if (tile.isFaceUp) {
                    mCountryTiles[index].text = tile.shortenedCountryName
                }

                // closed text tile cannot be long-pressed:
                mCountryTiles[index].setOnLongClickListener {
                    false
                }

                // set country tiles' onClickListener
                mCountryTiles[index].setOnClickListener {

                    // save click time of a country tile
                    mCountryLastClickTime = SystemClock.elapsedRealtime()

                    var tapsWaitTime = 0
                    if (mIsPairMatched) {
                        tapsWaitTime = 1000 // if last pair is matched, wait 1 second
                    } else {
                        tapsWaitTime = 1250 // if last pair is not matched, wait 1.25 second
                    }

                    // if country is clicked less than n second of last time a pair clicked -> ignore
                    if (SystemClock.elapsedRealtime() - mLastPairClickTime < tapsWaitTime) {
                        Log.i(
                            "PANJUTA",
                            "tap too fast"
                        )
                        if (mIsSoundOn) {
                            mSound.playShortResource(R.raw.tap)
                            return@setOnClickListener
                        } else {
                            // playDummyResource -> play zero volume audio resource
                            // if this 'else' missing -> user can fast tap i.e more than 2 tiles can open at the same time
                            // why -> ?
                            mSound.playDummyResource(R.raw.tap)
                            return@setOnClickListener
                        }
                    }

                    // if another text tile was tapped before this one, play error sound
                    if (mTappedShortenedCountryName != "-1") {
                        if (mIsSoundOn) {
                            mSound.playShortResource(R.raw.tap)
                        }
                    } else {
                        mCountryTiles[index].text = tile.shortenedCountryName
                        mTappedCountryTileIndex = index

                        updateTaps()

                        // only text tile that has been opened can be long-pressed:
                        // condition: only when flag tile has not been opened
                        // in other words: this is the first tile of the tile pair that
                        // is going to be compared
                        if (mTappedFlagResID == -1) {
                            mCountryTiles[mTappedCountryTileIndex].setOnLongClickListener {

                                displayToastAboveButton(
                                    mCountryTiles[index],
                                    mMapShortenedCountryNameToFlagResID[tile.shortenedCountryName]!!,
                                    tile.shortenedCountryName
                                )

                                updateTaps()

                                return@setOnLongClickListener true
                            }
                        }

                        // if user tap on a tile containing country code, show a toast above that tile cbout its country name
                        // displayToastAboveButton(mTileTextViews[index], tile.countryName)

                        // disable click on other text tiles
//                    val otherTileTextViews =
//                        mTileTextViews.filterNot { it == mTileTextViews[index] }
//                    otherTileTextViews.forEach { it -> it.setClickable(false) }

                        // disable click on this text tile
//                    mTileTextViews[index].setClickable(false)

                        // disable click on all text tiles after a text tile is clicked
                        // mTileTextViews.forEach { it -> it.setClickable(false) }

                        if (mTappedShortenedCountryName == "-1") { // register tile only if the variable is empty (ie. "-1")
                            mTappedShortenedCountryName =
                                tile.shortenedCountryName // needs to be compared with tapped flag image after this one

                            if (mTappedFlagResID != -1)  // if flag image has been tapped too
                            {
                                mLastPairClickTime = SystemClock.elapsedRealtime()
                                compareTappedTiles(
                                    mTappedShortenedCountryName,
                                    mTappedFlagResID,
                                )
                            }
                        }
                    }
                }
            } else { // tile displays flag image

                mCountryTiles[index].setVisibility(View.GONE)
                mFlagTiles[index].setVisibility(View.VISIBLE)

                if (tile.isFaceUp) {
                    Log.i(
                        "PANJUTA",
                        "drawTiles(), index of mFlagTiles[index]: $index, tile.flagResId: ${tile.flagResId}"
                    )
                    mFlagTiles[index].setImageResource(tile.flagResId)
                } else {

                    if (mIsLightTheme) {
                        mFlagTiles[index].setImageResource(R.drawable.tv_background_primary)
                    } else {
                        mFlagTiles[index].setImageResource(R.drawable.tv_background_primary_dark)
                    }

                }

                // closed flag tile cannot be long-pressed:
                mFlagTiles[index].setOnLongClickListener {
                    false
                }

                // set flag tiles' onClickListener
                mFlagTiles[index].setOnClickListener {

                    // save click time of a flag tile
                    mFlagLastClickTime = SystemClock.elapsedRealtime()

                    var tapsWaitTime = 0
                    if (mIsPairMatched) {
                        tapsWaitTime = 1000 // if last pair is matched, wait 1 second
                    } else {
                        tapsWaitTime = 1250 // if last pair is not matched, wait 1.25 second
                    }

                    // if flag is clicked less than 1.5 second of last time a pair clicked -> ignore
                    if (SystemClock.elapsedRealtime() - mLastPairClickTime < tapsWaitTime) {
                        Log.i(
                            "PANJUTA",
                            "tap too fast"
                        )
                        if (mIsSoundOn) {
                            mSound.playShortResource(R.raw.tap)
                            return@setOnClickListener
                        } else {
                            // playDummyResource -> play zero volume audio resource
                            // if this 'else' missing -> user can fast tap i.e more than 2 tiles can open at the same time
                            // why -> ?
                            mSound.playDummyResource(R.raw.tap)
                            return@setOnClickListener
                        }
                    }

                    // if another flag tile was tapped before this one, play error sound
                    if (mTappedFlagResID != -1) {
                        if (mIsSoundOn) {
                            mSound.playShortResource(R.raw.tap)
                        }
                    } else {
                        // if user tap on face-down flag imageview, it will reveal face-up flag side If tapped tile flag doesn't match with the tapped text tile, after 2 seconds, both tiles are face-down.
                        if (!tile.isFaceUp) mFlagTiles[index].setImageResource(tile.flagResId)
                        mTappedFlagTileIndex = index

                        updateTaps()

                        // only flag tile that has been opened can be long-pressed:
                        // condition: only when text tile has not been opened
                        // in other words: this is the first tile of the tile pair that
                        // is going to be compared
                        if (mTappedShortenedCountryName == "-1") {
                            mFlagTiles[mTappedFlagTileIndex].setOnLongClickListener {
                                displayToastAboveButton(
                                    mFlagTiles[index],
                                    tile.flagResId,
                                    mMapFlagResIDtoShortenedCountryName[tile.flagResId]!!
                                )
                                updateTaps()
                                return@setOnLongClickListener true
                            }
                        }

                        // disable other flag tiles
//                        val otherTileImageViews =
//                            mTileImageViews.filterNot { it == mTileImageViews[index] }
//                        otherTileImageViews.forEach { it -> it.setClickable(false) }

                        // disable this flag tile
//                        mTileImageViews[index].setClickable(false)

                        if (mTappedFlagResID == -1) { // register tile only if the variable is empty
                            mTappedFlagResID =
                                tile.flagResId // needs to be compared with tapped name tile
                            if (mTappedShortenedCountryName != "-1") { // if text image has been tapped too
                                mLastPairClickTime = SystemClock.elapsedRealtime()
                                compareTappedTiles(
                                    mTappedShortenedCountryName,
                                    mTappedFlagResID,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateTaps() {
        mCurrentTaps++
        tv_taps_current_amount.text = mCurrentTaps.toString()
    }


    private fun compareTappedTiles(
        tappedShortenedCountryName: String,
        tappedFlagResID: Int,
    ) {
        Log.i(
            "PANJUTA",
            "inside Compare, mTappedFlagTileIndex: $mTappedFlagTileIndex, tappedShortenedCountryName: $tappedShortenedCountryName, tappedFlagResID: $tappedFlagResID, mMapFlagResIDtoShortenedCountryName[tappedFlagResID]: ${mMapFlagResIDtoShortenedCountryName[tappedFlagResID]}"
        )
        if (mMapFlagResIDtoShortenedCountryName[tappedFlagResID] == tappedShortenedCountryName) //correct pairs
        { // matched pair

            mTileList[mTappedFlagTileIndex].isFaceUp = true
            Log.i(
                "PANJUTA",
                "mTileList[$mTappedFlagTileIndex].isFaceUp: ${mTileList[mTappedFlagTileIndex].isFaceUp}"
            )
            mIsPairMatched = true

            // play correct sound:
            if (mIsSoundOn) {
                mSound.playShortResource(R.raw.correct)
            }


            // make both tiles blink:
            blinkView(mFlagTiles[mTappedFlagTileIndex], false)
            blinkView(mCountryTiles[mTappedCountryTileIndex], false)

            // both tiles must not be able to selected anymore; just play sound when tapped
            mFlagTiles[mTappedFlagTileIndex].setOnClickListener {
                if (mIsSoundOn) {
                    mSound.playShortResource(
                        R.raw.tap
                    )
                }
            }
            mCountryTiles[mTappedCountryTileIndex].setOnClickListener {
                if (mIsSoundOn) {
                    mSound.playShortResource(
                        R.raw.tap
                    )
                }
            }

            // disable long-click on text and flag tiles after they are matched and opened
            mFlagTiles[mTappedFlagTileIndex].setOnLongClickListener { false }
            mCountryTiles[mTappedCountryTileIndex].setOnLongClickListener { false }

            mMatchedPairs++

            if (mMatchedPairs == mNumberOfTiles / 2) { // game ends

                mIsGameRunning = false
                mIdleHandler.removeCallbacks(mIdleRunnable)

                // disable all tiles
                for (flag in mFlagTiles) {
                    // flag.isClickable = false
                    flag.setOnClickListener(null)
                    setGreyTint(flag)
                }
                for (country in mCountryTiles) {
                    // country.isClickable = false
                    country.setOnClickListener(null)
                    // country.setBackgroundColor(getResources().getColor(R.color.grey))
                }

                // store to prefs that user has completed first game
                val prefs = getSharedPreferences("mIsFirstGameCompletedKey", Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = prefs.edit()
                editor.putBoolean("first_game_completed", true)
                editor.commit()

                checkBestTime()
                checkBestTaps(mCurrentTaps)

                // todo: when game ends, change the menu order -> restart and quit should be in app bar
                // only show 'restart' and 'quit' menu item
                invalidateOptionsMenu()
                mIsMenuComplete = false

                Log.i(
                    "PANJUTA",
                    "game ends: mIsFirstGameCompleted: $mIsFirstGameCompleted"
                )

                when {
                    !mIsFirstGameCompleted -> {
                        Toast.makeText(
                            applicationContext,
                            "Congratulation! You completed your first game!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    !mIsBestTime && !mIsBestTaps -> {
                        // neither best taps nor best time is achieved
                        // and this is not the first game completed
                        Toast.makeText(
                            applicationContext,
                            "Nice try! You'll keep getting better next time.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    mIsBestTime && !mIsBestTaps -> {
                        // user set a new best time
                        Toast.makeText(
                            applicationContext,
                            "Congratulation! You set a new Best Time!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    !mIsBestTime && mIsBestTaps -> {
                        // user set a new best taps
                        Toast.makeText(
                            applicationContext,
                            "Congratulation! You set a new Best Taps!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    mIsBestTime && mIsBestTaps -> {
                        // set a new best time and taps
                        Toast.makeText(
                            applicationContext,
                            "Excellent! You set BOTH new Best Time and Best Taps!!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        } else { // wrong pairs

            mIsPairMatched = false

            // the tapped index should be stored, so that in timer's onFinish() different indexes generated from user's fast tap can be avoided
            val ivIndex = mTappedFlagTileIndex
            val tvIndex = mTappedCountryTileIndex

            // make both tiles face-down after n seconds:
            val timer = object : CountDownTimer(1250, 250) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    // display text and flag tiles in close position
                    if (mIsLightTheme) {
                        mFlagTiles[ivIndex].setImageResource(R.drawable.tv_background_primary)
                    } else {
                        mFlagTiles[ivIndex].setImageResource(R.drawable.tv_background_primary_dark)
                    }

                    mCountryTiles[tvIndex].text = ""

                    // disable long-click on text and flag tiles after they are closed
                    mFlagTiles[ivIndex].setOnLongClickListener { false }
                    mCountryTiles[tvIndex].setOnLongClickListener { false }
                }
            }
            timer.start()
        }

        // reset tapped tiles
        mTappedFlagResID = -1
        mTappedShortenedCountryName = "-1"

        // enable all tiles
        for (tv in mCountryTiles) tv.setClickable(true)
        for (iv in mFlagTiles) iv.setClickable(true)
    }


    private fun clearSharedPreferences() {
        val sharedPreferences_bestTap = getSharedPreferences("mBestTapsKey", MODE_PRIVATE)
        val editor_bestTap = sharedPreferences_bestTap.edit()
        editor_bestTap.remove("best_taps")
        editor_bestTap.commit()

        val sharedPreferences_bestTap_Easy = getSharedPreferences("mBestTapsKey_Easy", MODE_PRIVATE)
        val editor_bestTap_Easy = sharedPreferences_bestTap_Easy.edit()
        editor_bestTap_Easy.remove("best_taps")
        editor_bestTap_Easy.commit()

        val sharedPreferences_bestTap_Medium =
            getSharedPreferences("mBestTapsKey_Medium", MODE_PRIVATE)
        val editor_bestTap_Medium = sharedPreferences_bestTap_Medium.edit()
        editor_bestTap_Medium.remove("best_taps")
        editor_bestTap_Medium.commit()

        val sharedPreferences_bestTap_Hard = getSharedPreferences("mBestTapsKey_Hard", MODE_PRIVATE)
        val editor_bestTap_Hard = sharedPreferences_bestTap_Hard.edit()
        editor_bestTap_Hard.remove("best_taps")
        editor_bestTap_Hard.commit()


        val sharedPreferences_bestTime = getSharedPreferences("mBestTimeKey", MODE_PRIVATE)
        val editor_bestTime = sharedPreferences_bestTime.edit()
        editor_bestTime.remove("best_time")
        editor_bestTime.commit()

        val sharedPreferences_bestTime_Easy =
            getSharedPreferences("mBestTimeKey_Easy", MODE_PRIVATE)
        val editor_bestTime_Easy = sharedPreferences_bestTime_Easy.edit()
        editor_bestTime_Easy.remove("best_time")
        editor_bestTime_Easy.commit()

        val sharedPreferences_bestTime_Medium =
            getSharedPreferences("mBestTimeKey_Medium", MODE_PRIVATE)
        val editor_bestTime_Medium = sharedPreferences_bestTime_Medium.edit()
        editor_bestTime_Medium.remove("best_time")
        editor_bestTime_Medium.commit()

        val sharedPreferences_bestTime_Hard =
            getSharedPreferences("mBestTimeKey_Hard", MODE_PRIVATE)
        val editor_bestTime_Hard = sharedPreferences_bestTime_Hard.edit()
        editor_bestTime_Hard.remove("best_time")
        editor_bestTime_Hard.commit()


        val sharedPreferences_firstGame =
            getSharedPreferences("mIsFirstGameCompletedKey", MODE_PRIVATE)
        val editor_firstGame = sharedPreferences_firstGame.edit()
        editor_firstGame.remove("first_game_completed")
        editor_firstGame.commit()

        val sharedPreferences_checkBoxInstruction = getSharedPreferences("CheckItem", MODE_PRIVATE)
        val editor__checkBoxInstruction = sharedPreferences_checkBoxInstruction.edit()
        editor__checkBoxInstruction.remove("item")
        editor__checkBoxInstruction.commit()

    }

    private fun putDoubleToPrefs(
        edit: SharedPreferences.Editor,
        key: String?,
        value: Double
    ): SharedPreferences.Editor? {
        return edit.putLong(key, java.lang.Double.doubleToRawLongBits(value))
    }

    private fun getDoubleFromPrefs(
        prefs: SharedPreferences,
        key: String?,
        defaultValue: Double
    ): Double {
        return java.lang.Double.longBitsToDouble(
            prefs.getLong(
                key,
                java.lang.Double.doubleToLongBits(defaultValue)
            )
        )
    }

    private fun checkBestTime() {
        // stop the timer
        mTimerHandler.removeCallbacks(mTimerRunnable)

        if (mBestTime == 0.0 || mCurrentTime < mBestTime) {

            //setting preferences

            var prefs: SharedPreferences? = null
            when (mNumberOfTiles) {
                30 -> {
                    prefs = getSharedPreferences("mBestTimeKey_Easy", MODE_PRIVATE)
                }
                40 -> {
                    prefs = getSharedPreferences("mBestTimeKey_Medium", MODE_PRIVATE)
                }
                50 -> {
                    prefs = getSharedPreferences("mBestTimeKey_Hard", MODE_PRIVATE)
                }
            }
            val editor: SharedPreferences.Editor = prefs!!.edit()

            // sharedPreferences cannot store double values (mCurrentTime) so use this function
            // from https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
            putDoubleToPrefs(editor, "best_time", mCurrentTime)
            Log.i(
                "PANJUTA",
                "putDoubleToPrefs mCurrentTime: $mCurrentTime"
            )
            editor.commit()

            dimTextView(tv_time_best_timer)
            blinkView(tv_time_current_timer, true)

            mIsBestTime = true

        }
        // updateBestTimeTexView()
    }

    private fun updateBestTimeTexView() {

        var prefs: SharedPreferences? = null
        when (mNumberOfTiles) {
            30 -> {
                prefs = getSharedPreferences("mBestTimeKey_Easy", MODE_PRIVATE)
            }
            40 -> {
                prefs = getSharedPreferences("mBestTimeKey_Medium", MODE_PRIVATE)
            }
            50 -> {
                prefs = getSharedPreferences("mBestTimeKey_Hard", MODE_PRIVATE)
            }
        }

        // sharedPreferences cannot store double values (mCurrentTime) so use this function
        // from https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
        mBestTime = getDoubleFromPrefs(prefs!!, "best_time", 0.0)

        val minutes = mBestTime / 60000
        val intMinutes = floor(minutes.toDouble())
        val seconds = (minutes - intMinutes) * 60.0
        val intSeconds = floor(seconds)
        val hundredths = (seconds - intSeconds) * 100.0

        tv_time_best_timer.text = String.format(
            "%02d:%02d:%02d",
            intMinutes.toInt(),
            intSeconds.toInt(),
            hundredths.toInt()
        )
    }

    private fun checkBestTaps(score: Int) {
        // this function is called when game ends

        if (mBestTaps == 0 || mCurrentTaps < mBestTaps) { // set a new best taps

            //setting preferences
            var prefs: SharedPreferences? = null
            when (mNumberOfTiles) {
                30 -> {
                    prefs = getSharedPreferences("mBestTapsKey_Easy", MODE_PRIVATE)
                }
                40 -> {
                    prefs = getSharedPreferences("mBestTapsKey_Medium", MODE_PRIVATE)
                }
                50 -> {
                    prefs = getSharedPreferences("mBestTapsKey_Hard", MODE_PRIVATE)
                }
            }
            val editor: SharedPreferences.Editor = prefs!!.edit()
            editor.putInt("best_taps", score)
            editor.commit()

            dimTextView(tv_taps_best_amount)
            blinkView(tv_taps_current_amount, true)

            mIsBestTaps = true

        }
        // updateBestTapsTexView()
    }

    private fun dimTextView(tv: TextView) {
        tv.setBackgroundColor(resources.getColor(R.color.medium_grey))
    }

    private fun updateBestTapsTexView() {
        var prefs: SharedPreferences? = null
        when (mNumberOfTiles) {
            30 -> {
                prefs = getSharedPreferences("mBestTapsKey_Easy", MODE_PRIVATE)
            }
            40 -> {
                prefs = getSharedPreferences("mBestTapsKey_Medium", MODE_PRIVATE)
            }
            50 -> {
                prefs = getSharedPreferences("mBestTapsKey_Hard", MODE_PRIVATE)
            }
        }

        mBestTaps = prefs!!.getInt("best_taps", 0) //0 is the default value
        tv_taps_best_amount.text = mBestTaps.toString()
    }


    private fun selectAndShuffleTileContent() {
        mSelectedFlagsResID = selectRandomFlags(mAllFlagsResID, mNumberOfTiles / 2, null)

        findSelectedShortenedCountryNames(mSelectedFlagsResID) // get country names's of selected flags

        mSelectedFlagResIDandShortenedCountryName =
            combineArrayListOfDifferentTypes(mSelectedFlagsResID, mSelectedShortenedCountryNames)
        Log.i(
            "PANJUTA",
            "mSelectedFlagResIDandShortenedCountryName size: ${mSelectedFlagResIDandShortenedCountryName.size}"
        )

        //  mShuffledSelectedFlagResIDandCountryCode =
        //  mSelectedFlagResIDandCountryCode.shuffled() as ArrayList<Any>

        // shuffle
        mShuffledSelectedFlagResIDandShortenedCountryName =
            fisherYatesShuffle(mSelectedFlagResIDandShortenedCountryName) as ArrayList<Any>
    }

    private fun populateTileList() { // create tiles based on what is inside displayed tile on the screen
        for ((index, tileContent) in mShuffledSelectedFlagResIDandShortenedCountryName.withIndex()) {
            Log.i(
                "PANJUTA",
                "tile position ${index + 1}: $tileContent"
            )

            //check variable types:
            when (tileContent) {
                // tile is flag image; face down
                is Int -> mTileList.add(Tile(index + 1, false, tileContent.toInt(), "", ""))
                // tile is country name; face down
                is String -> mTileList.add(
                    Tile(
                        index + 1,
                        false,
                        -1,
                        tileContent.toString(),
                        mMapShortenedCountryNameToCountryName[tileContent].toString()
                    )

                )
                else -> Log.i(
                    "PANJUTA",
                    "$tileContent is neither Int or String"
                )
            }
            Log.i(
                "PANJUTA",
                "${mTileList[index]}"
            )
        }
        Log.i(
            "PANJUTA",
            "mTileList size: ${mTileList.size}"
        )
    }

//    private fun setOnClickListenerBottomButtons() {
//        btn_quit3.setOnClickListener(this)
//        btn_restart3.setOnClickListener(this)
//    }

    private fun mapFlagResIDToShortenedCountryName(allFlagsResID: ArrayList<Int>) {
        for ((index, flagResID) in allFlagsResID.withIndex()) {
            mMapFlagResIDtoShortenedCountryName.put(
                flagResID,
                Constants.allShortenedCountryNames[index]
            )
        }
    }

    private fun mapShortenedCountryNameToFlagResID(allShortenedCountryName: ArrayList<String>) {
        for ((index, shortenedCountryName) in allShortenedCountryName.withIndex()) {
            mMapShortenedCountryNameToFlagResID.put(
                shortenedCountryName,
                mAllFlagsResID[index]
            )
        }
    }

    private fun mapShortenedCountryNameToCountryName(allShortenedCountryName: ArrayList<String>) {
        for ((index, shortenedCountryName) in allShortenedCountryName.withIndex()) {
            mMapShortenedCountryNameToCountryName.put(
                shortenedCountryName,
                Constants.allCountryNames[index]
            )
        }
    }

    private fun selectRandomFlags(
        allFlagsResID: ArrayList<Int>,
        halfNumberOfTiles: Int, excludedFlagResID: Int?

    ): ArrayList<Int> {
        // Avoid a deadlock
        if (halfNumberOfTiles >= allFlagsResID.size) {
            return allFlagsResID
        }

        val selectedFlagsResID: ArrayList<Int> = arrayListOf()
        // Get a random item until we got the requested amount
        while (selectedFlagsResID.size < halfNumberOfTiles) {
            val randomIndex = (0..allFlagsResID.size - 1).random()
            Log.i("PANJUTA", "randomIndex: $randomIndex")
            val flagResID = allFlagsResID[randomIndex]
            if (!selectedFlagsResID.contains(flagResID)) {
                selectedFlagsResID.add(flagResID)
            }
        }
        for (flagResID in selectedFlagsResID) {
            Log.i("PANJUTA", "Selected flag resource IDs: $flagResID")
        }

        return selectedFlagsResID
    }

    private fun findSelectedShortenedCountryNames(selectedFlagsResID: ArrayList<Int>) {
        for ((index, flagResID) in selectedFlagsResID.withIndex()) {
            val shortenedCountryName = mMapFlagResIDtoShortenedCountryName[flagResID]
            if (shortenedCountryName != null) {
                mSelectedShortenedCountryNames.add(shortenedCountryName)
            }
        }
        Log.i(
            "PANJUTA",
            "mSelectedShortenedCountryNames size: ${mSelectedShortenedCountryNames.size}"
        )
    }

    // combine 2 arraylist of different types
// from https://www.techiedelight.com/combine-two-arrays-different-types-kotlin/
    private fun <T, U> combineArrayListOfDifferentTypes(
        first: ArrayList<T>,
        second: ArrayList<U>
    ): MutableList<Any> {
        val list: MutableList<Any> = ArrayList()
        list.addAll(first.map { i -> i as Any })
        list.addAll(second.map { i -> i as Any })
        return list
    }

// Fisher–Yates Shuffle Algorithm
// from https://www.techiedelight.com/shuffle-list-kotlin/

    private fun <T> fisherYatesShuffle(list: MutableList<T>): MutableList<T> {
        // start from the end of the list
        for (i in list.size - 1 downTo 1) {
            // get a random index `j` such that `0 <= j <= i`
            val j = Random.nextInt(i + 1)

            // swap element at i'th position in the list with the element at j'th position
            val temp = list[i]
            list[i] = list[j]
            list[j] = temp
        }
        return list
    }

    override fun onClick(v: View?) {
//        when (v?.id) {
//            R.id.btn_quit3 -> {
//                quitGame()
//            }
//            R.id.btn_restart3 -> { // onCreateHelper()
//                restartGame()
//            }
//            R.id.iv_tile_01 -> {
//                Log.i("PANJUTA", "imageview 01 clicked")
//
//            }
//            R.id.tv_tile_01 -> {
//                Log.i("PANJUTA", "textview 01 clicked")
//            }
//        }
//
    }

    private fun quitGame() {
        if (mIsSoundOn) {
            mSound.release()
        }
        finish()
    }

    private fun restartGame() {
        if (mIsSoundOn) {
            mSound.release()
        }
        val intent = Intent(this, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

// v is the Button view that you want the Toast to appear above
// and messageId is the id of your string resource for the message

    // v is the Button view that you want the Toast to appear above
// and messageId is the id of your string resource for the message
    private fun displayToastAboveButton(v: View, flagResId: Int, shortenedCountryName: String) {
        var xOffset = 0
        var yOffset = 0
        val gvr = Rect()
        val parent = v.parent as View
        val parentHeight = parent.height
        if (v.getGlobalVisibleRect(gvr)) {
            val root = v.rootView
            val halfWidth = root.right / 2
            val halfHeight = root.bottom / 2
            val parentCenterX: Int = (gvr.right - gvr.left) / 2 + gvr.left
            val parentCenterY: Int = (gvr.bottom - gvr.top) / 2 + gvr.top
            yOffset = if (parentCenterY <= halfHeight) {
                -(halfHeight - parentCenterY) - parentHeight
            } else {
                parentCenterY - halfHeight - parentHeight
            }
            if (parentCenterX < halfWidth) {
                xOffset = -(halfWidth - parentCenterX)
            }
            if (parentCenterX >= halfWidth) {
                xOffset = parentCenterX - halfWidth
            }
        }
        val layout = layoutInflater.inflate(R.layout.toast_image_layout, ll_toast)

        val myToast = Toast(applicationContext)
        myToast.duration = Toast.LENGTH_SHORT
        myToast.setGravity(Gravity.CENTER, xOffset, yOffset)

        val flag = layout.findViewById<ImageView>(R.id.iv_toast)
        val country = layout.findViewById<TextView>(R.id.tv_toast)

        flag.setImageResource(flagResId)
        country.text = shortenedCountryName

        myToast.view = layout //setting the view of custom toast layout
        myToast.show()
    }


    private fun blinkView(view: View, isInfinite: Boolean) {

        if (isInfinite) {
            val animation: Animation = AlphaAnimation(
                1.0F,
                0.0F
            ) // to change visibility from visible (1.0) to invisible (0.0)
            animation.repeatCount = Animation.INFINITE
            animation.duration = 400 // miliseconds duration for each animation cycle
            animation.repeatMode =
                Animation.REVERSE //animation will start from end point once ended
            view.startAnimation(animation) //to start animation
        } else {
            val animation: Animation = AlphaAnimation(
                1.0F,
                0.25F
            )
            animation.repeatCount = 1
            animation.duration = 350
            animation.repeatMode = Animation.RESTART
            view.startAnimation(animation)
        }
    }

    private fun setGreyTint(v: ImageView) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f) //0 means grayscale
        val cf = ColorMatrixColorFilter(matrix)
        v.colorFilter = cf
        v.imageAlpha = 128 // 128 = 0.5
    }

    fun clearGreyTint(v: ImageView) {
        v.colorFilter = null
        v.imageAlpha = 255
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        delayedIdle()
    }

    private fun delayedIdle() {
        mIdleHandler.removeCallbacks(mIdleRunnable);
        mIdleHandler.postDelayed(mIdleRunnable, ((mIdleDelayMinutes * 1000 * 60).toLong()));
    }

    private fun addViewsToViewList() {
        mFlagTiles.add(0, iv_tile_01)
        mFlagTiles.add(1, iv_tile_02)
        mFlagTiles.add(2, iv_tile_03)
        mFlagTiles.add(3, iv_tile_04)
        mFlagTiles.add(4, iv_tile_05)
        mFlagTiles.add(5, iv_tile_06)
        mFlagTiles.add(6, iv_tile_07)
        mFlagTiles.add(7, iv_tile_08)
        mFlagTiles.add(8, iv_tile_09)
        mFlagTiles.add(9, iv_tile_10)
        mFlagTiles.add(10, iv_tile_11)
        mFlagTiles.add(11, iv_tile_12)
        mFlagTiles.add(12, iv_tile_13)
        mFlagTiles.add(13, iv_tile_14)
        mFlagTiles.add(14, iv_tile_15)
        mFlagTiles.add(15, iv_tile_16)
        mFlagTiles.add(16, iv_tile_17)
        mFlagTiles.add(17, iv_tile_18)
        mFlagTiles.add(18, iv_tile_19)
        mFlagTiles.add(19, iv_tile_20)
        mFlagTiles.add(20, iv_tile_21)
        mFlagTiles.add(21, iv_tile_22)
        mFlagTiles.add(22, iv_tile_23)
        mFlagTiles.add(23, iv_tile_24)
        mFlagTiles.add(24, iv_tile_25)
        mFlagTiles.add(25, iv_tile_26)
        mFlagTiles.add(26, iv_tile_27)
        mFlagTiles.add(27, iv_tile_28)
        mFlagTiles.add(28, iv_tile_29)
        mFlagTiles.add(29, iv_tile_30)

        mCountryTiles.add(0, tv_tile_01)
        mCountryTiles.add(1, tv_tile_02)
        mCountryTiles.add(2, tv_tile_03)
        mCountryTiles.add(3, tv_tile_04)
        mCountryTiles.add(4, tv_tile_05)
        mCountryTiles.add(5, tv_tile_06)
        mCountryTiles.add(6, tv_tile_07)
        mCountryTiles.add(7, tv_tile_08)
        mCountryTiles.add(8, tv_tile_09)
        mCountryTiles.add(9, tv_tile_10)
        mCountryTiles.add(10, tv_tile_11)
        mCountryTiles.add(11, tv_tile_12)
        mCountryTiles.add(12, tv_tile_13)
        mCountryTiles.add(13, tv_tile_14)
        mCountryTiles.add(14, tv_tile_15)
        mCountryTiles.add(15, tv_tile_16)
        mCountryTiles.add(16, tv_tile_17)
        mCountryTiles.add(17, tv_tile_18)
        mCountryTiles.add(18, tv_tile_19)
        mCountryTiles.add(19, tv_tile_20)
        mCountryTiles.add(20, tv_tile_21)
        mCountryTiles.add(21, tv_tile_22)
        mCountryTiles.add(22, tv_tile_23)
        mCountryTiles.add(23, tv_tile_24)
        mCountryTiles.add(24, tv_tile_25)
        mCountryTiles.add(25, tv_tile_26)
        mCountryTiles.add(26, tv_tile_27)
        mCountryTiles.add(27, tv_tile_28)
        mCountryTiles.add(28, tv_tile_29)
        mCountryTiles.add(29, tv_tile_30)

        if (mNumberOfTiles > 30) {
            mFlagTiles.add(30, iv_tile_31)
            mFlagTiles.add(31, iv_tile_32)
            mFlagTiles.add(32, iv_tile_33)
            mFlagTiles.add(33, iv_tile_34)
            mFlagTiles.add(34, iv_tile_35)
            mFlagTiles.add(35, iv_tile_36)
            mFlagTiles.add(36, iv_tile_37)
            mFlagTiles.add(37, iv_tile_38)
            mFlagTiles.add(38, iv_tile_39)
            mFlagTiles.add(39, iv_tile_40)

            mCountryTiles.add(30, tv_tile_31)
            mCountryTiles.add(31, tv_tile_32)
            mCountryTiles.add(32, tv_tile_33)
            mCountryTiles.add(33, tv_tile_34)
            mCountryTiles.add(34, tv_tile_35)
            mCountryTiles.add(35, tv_tile_36)
            mCountryTiles.add(36, tv_tile_37)
            mCountryTiles.add(37, tv_tile_38)
            mCountryTiles.add(38, tv_tile_39)
            mCountryTiles.add(39, tv_tile_40)
        }

        if (mNumberOfTiles > 40) {
            mFlagTiles.add(40, iv_tile_41)
            mFlagTiles.add(41, iv_tile_42)
            mFlagTiles.add(42, iv_tile_43)
            mFlagTiles.add(43, iv_tile_44)
            mFlagTiles.add(44, iv_tile_45)
            mFlagTiles.add(45, iv_tile_46)
            mFlagTiles.add(46, iv_tile_47)
            mFlagTiles.add(47, iv_tile_48)
            mFlagTiles.add(48, iv_tile_49)
            mFlagTiles.add(49, iv_tile_50)

            mCountryTiles.add(40, tv_tile_41)
            mCountryTiles.add(41, tv_tile_42)
            mCountryTiles.add(42, tv_tile_43)
            mCountryTiles.add(43, tv_tile_44)
            mCountryTiles.add(44, tv_tile_45)
            mCountryTiles.add(45, tv_tile_46)
            mCountryTiles.add(46, tv_tile_47)
            mCountryTiles.add(47, tv_tile_48)
            mCountryTiles.add(48, tv_tile_49)
            mCountryTiles.add(49, tv_tile_50)
        }

    }

}