package com.tjknsoft.tjflagquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import kotlinx.android.synthetic.main.activity_quiz_flag_memory.*
import kotlinx.android.synthetic.main.toast_image_layout.*
import kotlin.math.floor
import kotlin.random.Random
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate


class QuizFlagMemoryActivity : AppCompatActivity(), View.OnClickListener {

    private var mTileList: ArrayList<Tile> = arrayListOf()
    private var mAllFlagsResID: ArrayList<Int> = arrayListOf()

    // private var mAllCountryCodes: ArrayList<String> = arrayListOf()
    private var mAllCountryNames: ArrayList<String> = arrayListOf()
    private var mAllShortenedCountryNames: ArrayList<String> = arrayListOf()
    private val mNumberOfTiles: Int = 50
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
    val mINSTRUCTION_ACTIVITY_REQUEST_CODE = 0
    private var mIsSoundOn = true
    private lateinit var mMenu: Menu
    private var mIsLightTheme = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_flag_memory)

        onCreateHelper()
    }

    private fun onCreateHelper() {

        // todo: tablet?
        // todo: dark theme
        // todo: easy medium hard layouts


        // clearSharedPreferences()

        setVariables()

        if (!getCheckBoxStatus()) {
            displayInstruction()
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

    // @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            this.mMenu = menu
        }
        menuInflater.inflate(R.menu.options_menu, menu)
//
//        if (menu is MenuBuilder) {
//            menu.setOptionalIconsVisible(true)
//        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // todo: tap effect on menu in app bar
        return when (item.itemId) {

            R.id.menu_theme -> {
                // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                mIsLightTheme = !mIsLightTheme
                drawTiles()
                true
            }

            R.id.menu_mute -> {

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
            }

            R.id.menu_help -> {
                displayInstruction()
                true
            }

            R.id.menu_restart -> {
                restartGame()
                true
            }

            R.id.menu_quit -> {
                quitGame()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun toggleMenuItem(menuItem: Int) {
        val menuItem: MenuItem = mMenu.findItem(menuItem)
        if (mIsSoundOn) {
            menuItem.title = "Mute"
            menuItem.setIcon(R.drawable.ic_mute)
        } else {
            menuItem.title = "Unmute"
            menuItem.setIcon(R.drawable.ic_unmute)
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

    private fun displayInstruction() {

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


        val intent = Intent(this, InstructionActivity::class.java)
        startActivityForResult(intent, mINSTRUCTION_ACTIVITY_REQUEST_CODE)
    }

    // This method is called when the InstructionActivity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(
            "PANJUTA",
            "Back to FlagMemoryActivity; inside onActivityResult(), requestCode: $requestCode, resultCode: $resultCode, data: ${data.toString()} "
        )

        // Check that it is the SecondActivity with an OK result
        if (requestCode == mINSTRUCTION_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                // Get String data from Intent
                val checkBoxStatus = data!!.getBooleanExtra("KeyCheckBox", false)

                Log.i(
                    "PANJUTA",
                    "Back to FlagMemoryActivity; checkBoxStatus: $checkBoxStatus"
                )

                storeCheckBoxStatus(checkBoxStatus)

            } else {
                Log.i(
                    "PANJUTA",
                    "resultCode != Activity.RESULT_OK; resultCode: $resultCode"
                )
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
                    // btn_restart3.performClick()
                    restartGame()
                }

                val intMinutes = floor(minutes)
                val seconds = (minutes - intMinutes) * 60.0
                val intSeconds = floor(seconds)
                val hundreths = (seconds - intSeconds) * 100.0

                // tv_current_timer.setText("${minutes.toString()}:${seconds.toString()}")
                tv_current_timer.text = String.format(
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
        mOnResumeTime = System.currentTimeMillis().toDouble()
        mTimerHandler.postDelayed(mTimerRunnable, 0)
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
        // todo: dark theme

        // draw tiles
        for ((index, tile) in mTileList.withIndex()) {
            if (tile.shortenedCountryName != "") {
                // tile displays shortened country name
                mFlagTiles[index].setVisibility(View.GONE)
                mCountryTiles[index].setVisibility(View.VISIBLE)
                if (tile.isFaceUp) {
                    mCountryTiles[index].text = tile.shortenedCountryName
                }

                // closed text tile cannot be long-pressed:
                mCountryTiles[index].setOnLongClickListener {
                    false
                }

                // set text tiles' onClickListener
                mCountryTiles[index].setOnClickListener {

                    // save click time of a country tile
                    mCountryLastClickTime = SystemClock.elapsedRealtime()

                    // if country is clicked less than 1.5 second of last time a pair clicked -> ignore
                    if (SystemClock.elapsedRealtime() - mLastPairClickTime < 1500) {
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

                    // if a text tile was tapped before this one, play error sound
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

                    // if flag is clicked less than 1.5 second of last time a pair clicked -> ignore
                    if (SystemClock.elapsedRealtime() - mLastPairClickTime < 1500) {
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

                    // if a text tile was tapped before this one, play error sound
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
        tv_current_taps.text = mCurrentTaps.toString()
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

                // store to prefs that user has completed first game
                val prefs = getSharedPreferences("mIsFirstGameCompletedKey", Context.MODE_PRIVATE)
                val editor: SharedPreferences.Editor = prefs.edit()
                editor.putBoolean("first_game_completed", true)
                editor.commit()

                checkBestTime()
                checkBestTaps(mCurrentTaps)

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

            // the tapped index should be stored, so that in timer's onFinish() different indexes generated from user's fast tap can be avoided
            val ivIndex = mTappedFlagTileIndex
            val tvIndex = mTappedCountryTileIndex

            // make both tiles face-down after n seconds:
            val timer = object : CountDownTimer(1500, 250) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    // display text and flag tiles in close position
                    if (mIsLightTheme){
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
        val sharedPreferences = getSharedPreferences("mBestTapsKey", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("best_taps")
        editor.commit()

        val sharedPreferences2 = getSharedPreferences("mBestTimeKey", MODE_PRIVATE)
        val editor2 = sharedPreferences2.edit()
        editor2.remove("best_time")
        editor2.commit()

        val sharedPreferences3 = getSharedPreferences("mIsFirstGameCompletedKey", MODE_PRIVATE)
        val editor3 = sharedPreferences3.edit()
        editor3.remove("first_game_completed")
        editor3.commit()

        val sharedPreferences4 = getSharedPreferences("CheckItem", MODE_PRIVATE)
        val editor4 = sharedPreferences4.edit()
        editor4.remove("item")
        editor4.commit()

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
            val prefs = getSharedPreferences("mBestTimeKey", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = prefs.edit()

            // editor.putInt("best_time", mCurrentTime.toInt())
            // sharedPreferences cannot store double values (mCurrentTime) so use this function
            // from https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
            putDoubleToPrefs(editor, "best_time", mCurrentTime)
            Log.i(
                "PANJUTA",
                "putDoubleToPrefs mCurrentTime: $mCurrentTime"
            )
            editor.commit()

            dimTextView(tv_best_timer)
            blinkView(tv_current_timer, true)

            mIsBestTime = true

        }
        // updateBestTimeTexView()
    }

    private fun updateBestTimeTexView() {
        // getting preferences
        val prefs = getSharedPreferences("mBestTimeKey", MODE_PRIVATE)

        // mBestTime = prefs.getInt("best_time", 0) //0 is the default value
        // sharedPreferences cannot store double values (mCurrentTime) so use this function
        // from https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
        mBestTime = getDoubleFromPrefs(prefs, "best_time", 0.0)

        val minutes = mBestTime / 60000
        val intMinutes = floor(minutes.toDouble())
        val seconds = (minutes - intMinutes) * 60.0
        val intSeconds = floor(seconds)
        val hundredths = (seconds - intSeconds) * 100.0

        tv_best_timer.text = String.format(
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
            val prefs = getSharedPreferences("mBestTapsKey", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putInt("best_taps", score)
            editor.commit()

            dimTextView(tv_best_taps)
            blinkView(tv_current_taps, true)

            mIsBestTaps = true

        }
        // updateBestTapsTexView()
    }

    private fun dimTextView(tv: TextView) {
        tv.setBackgroundColor(resources.getColor(R.color.medium_grey))
    }

    private fun updateBestTapsTexView() {
        // getting preferences
        val prefs = getSharedPreferences("mBestTapsKey", MODE_PRIVATE)
        mBestTaps = prefs.getInt("best_taps", 0) //0 is the default value
        tv_best_taps.text = mBestTaps.toString()
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
            animation.repeatCount = 2
            animation.duration = 350
            animation.repeatMode = Animation.RESTART
            view.startAnimation(animation)
        }

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
        mFlagTiles.add(30, iv_tile_41)
        mFlagTiles.add(31, iv_tile_42)
        mFlagTiles.add(32, iv_tile_43)
        mFlagTiles.add(33, iv_tile_44)
        mFlagTiles.add(34, iv_tile_45)
        mFlagTiles.add(35, iv_tile_46)
        mFlagTiles.add(36, iv_tile_47)
        mFlagTiles.add(37, iv_tile_48)
        mFlagTiles.add(38, iv_tile_49)
        mFlagTiles.add(39, iv_tile_50)

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
        mCountryTiles.add(30, tv_tile_41)
        mCountryTiles.add(31, tv_tile_42)
        mCountryTiles.add(32, tv_tile_43)
        mCountryTiles.add(33, tv_tile_44)
        mCountryTiles.add(34, tv_tile_45)
        mCountryTiles.add(35, tv_tile_46)
        mCountryTiles.add(36, tv_tile_47)
        mCountryTiles.add(37, tv_tile_48)
        mCountryTiles.add(38, tv_tile_49)
        mCountryTiles.add(39, tv_tile_50)
    }


}