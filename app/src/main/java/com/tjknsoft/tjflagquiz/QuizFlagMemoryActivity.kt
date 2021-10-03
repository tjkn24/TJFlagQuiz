package com.tjknsoft.tjflagquiz

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_quiz_flag_memory.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import kotlinx.android.synthetic.main.toast_image_layout.*
import android.content.SharedPreferences
import android.os.Handler
import android.os.SystemClock
import kotlin.math.floor


class QuizFlagMemoryActivity : AppCompatActivity(), View.OnClickListener {

    private var mTileList: ArrayList<Tile> = arrayListOf()
    private var mAllFlagsResID: ArrayList<Int> = arrayListOf()

    // private var mAllCountryCodes: ArrayList<String> = arrayListOf()
    private var mAllCountryNames: ArrayList<String> = arrayListOf()
    private var mAllShortenedCountryNames: ArrayList<String> = arrayListOf()
    private val mNumberOfTiles: Int = 40
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
    private var mCurrentMoves = 0
    private var mBestMoves = 0
    private var mBestTime = 0.0
    private var mMatchedPairs = 0
    private var mOnResumeTime = 0.0
    private var mCurrentTime = 0.0
    private var mPreviousDuration = 0.0
    private lateinit var mTimerHandler: Handler
    private lateinit var mTimerRunnable: Runnable
    private var mLastFlagClickTime: Long = 0 // variable to track event time for flag tile
    private var mLastCountryClickTime: Long = 0 // variable to track event time for country name tile
    private var mLastPairClickTime: Long = 0 // event time when second member of a pair (flag or country) is clicked


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_flag_memory)

        onCreateHelper()
    }

    private fun onCreateHelper() {

        clearSharedPreferences()

        setTimer()

        updateBestMovesTexView()

        updateBestTimeTexView()

        getAndMapData()

        selectAndShuffleTileContent()

        populateTileList()

        addViewsToViewList()

        drawTiles()

        setOnClickListenerBottomButtons()

        mSound = SoundPoolPlayer(this)

    }

    private fun setTimer() {
        mTimerHandler = Handler()
        mTimerRunnable = object : Runnable {
            override fun run() {
              mCurrentTime = System.currentTimeMillis() - mOnResumeTime + mPreviousDuration

                val minutes = mCurrentTime / 60000

                // if game time reach 60 minutes -> restart the game
                if (minutes > 60){
                    mTimerHandler.removeCallbacks(mTimerRunnable)
                    btn_restart3.performClick()
                }

                val intMinutes = floor(minutes)
                val seconds = (minutes - intMinutes) * 60.0
                val intSeconds = floor(seconds)
                val hundreths = (seconds- intSeconds) * 100.0

                // tv_current_timer.setText("${minutes.toString()}:${seconds.toString()}")
                tv_current_timer.text = String.format("%02d:%02d:%02d", intMinutes.toInt(), intSeconds.toInt(), hundreths.toInt())
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

        // draw tiles
        for ((index, tile) in mTileList.withIndex()) {
            if (tile.shortenedCountryName != "") {
                // tile displays shortened country name
                mFlagTiles[index].setVisibility(View.GONE)
                mCountryTiles[index].setVisibility(View.VISIBLE)
                if (tile.isFaceUp == true) {
                    mCountryTiles[index].text = tile.shortenedCountryName
                }

                // closed text tile cannot be long-pressed:
                mCountryTiles[index].setOnLongClickListener {
                    false
                }

                // set text tiles' onClickListener
                mCountryTiles[index].setOnClickListener {

                    // save click time of a country tile
                    mLastCountryClickTime = SystemClock.elapsedRealtime()

                    // if country is clicked less than 1.5 second of last time a pair clicked -> ignore
                    if (SystemClock.elapsedRealtime() - mLastPairClickTime < 1500 ) {
                        mSound.playShortResource(R.raw.tap)
                        return@setOnClickListener
                    }

                    // if a text tile was tapped before this one, play error sound
                    if (mTappedShortenedCountryName != "-1") {
                        mSound.playShortResource(R.raw.tap)
                    } else {
                        mCountryTiles[index].text = tile.shortenedCountryName
                        mTappedCountryTileIndex = index

                        updateMoves()

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

                                updateMoves()

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
                    mFlagTiles[index].setImageResource(tile.flagResId)
                } else {
                    mFlagTiles[index].setImageResource(R.drawable.tv_background_primary)
                }

                // closed flag tile cannot be long-pressed:
                mFlagTiles[index].setOnLongClickListener {
                    false
                }

                // set flag tiles' onClickListener
                mFlagTiles[index].setOnClickListener {

                    // save click time of a flag tile
                    mLastFlagClickTime = SystemClock.elapsedRealtime()

                    // if flag is clicked less than 1.5 second of last time a pair clicked -> ignore
                    if (SystemClock.elapsedRealtime() - mLastPairClickTime < 1500) {
                        mSound.playShortResource(R.raw.tap)
                        return@setOnClickListener
                    }

                    // if a text tile was tapped before this one, play error sound
                    if (mTappedFlagResID != -1) {
                        mSound.playShortResource(R.raw.tap)
                    } else {
                        // if user tap on face-down flag imageview, it will reveal face-up flag side If tapped tile flag doesn't match with the tapped text tile, after 2 seconds, both tiles are face-down.
                        if (!tile.isFaceUp) mFlagTiles[index].setImageResource(tile.flagResId)
                        mTappedFlagTileIndex = index

                        updateMoves()

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
                                updateMoves()
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

    private fun updateMoves() {
        mCurrentMoves++
        tv_current_moves.text = mCurrentMoves.toString()
    }


    private fun compareTappedTiles(
        tappedShortenedCountryName: String,
        tappedFlagResID: Int
    ) {
        Log.i(
            "PANJUTA",
            "inside Compare, tappedShortenedCountryName: $tappedShortenedCountryName, tappedFlagResID: $tappedFlagResID, mMapFlagResIDtoShortenedCountryName[tappedFlagResID]: ${mMapFlagResIDtoShortenedCountryName[tappedFlagResID]}"
        )
        if (mMapFlagResIDtoShortenedCountryName[tappedFlagResID] == tappedShortenedCountryName) //correct pairs
        { // matched pair

            // play correct sound:
            mSound.playShortResource(R.raw.correct)

            // make both tiles blink:
            blinkView(mFlagTiles[mTappedFlagTileIndex], false)
            blinkView(mCountryTiles[mTappedCountryTileIndex], false)

            // both tiles must not be able to selected anymore; just play sound when tapped
            mFlagTiles[mTappedFlagTileIndex].setOnClickListener {
                mSound.playShortResource(
                    R.raw.tap
                )
            }
            mCountryTiles[mTappedCountryTileIndex].setOnClickListener {
                mSound.playShortResource(
                    R.raw.tap
                )
            }

            // disable long-click on text and flag tiles after they are matched and opened
            mFlagTiles[mTappedFlagTileIndex].setOnLongClickListener { false }
            mCountryTiles[mTappedCountryTileIndex].setOnLongClickListener { false }

            mMatchedPairs++

            if (mMatchedPairs == 20) {
                // game ends
                checkBestMoves(mCurrentMoves)

                checkBestTime()
            }

        } else { // wrong pairs

            // Todo: User can tap any other tile only after the wrong pair is closed

            // the tapped index should be stored, so that in timer's onFinish() different indexes generated from user's fast tap can be avoided
            val ivIndex = mTappedFlagTileIndex
            val tvIndex = mTappedCountryTileIndex

            // make both tiles face-down after 2 seconds:
            val timer = object : CountDownTimer(1500, 250) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    // display text and flag tiles in close position
                    mFlagTiles[ivIndex].setImageResource(R.drawable.tv_background_primary)
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
        val sharedPreferences = getSharedPreferences("mBestMovesKey", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("best_moves")
        editor.commit()

        val sharedPreferences2 = getSharedPreferences("mBestTimeKey", MODE_PRIVATE)
        val editor2 = sharedPreferences.edit()
        editor2.remove("best_tinme")
        editor2.commit()

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

            blinkView(tv_best_timer, true)
            blinkView(tv_current_timer, true)
            mSound.playShortResource(
                R.raw.perfect
            )
            Toast.makeText(applicationContext, "You set a Best Time record!!", Toast.LENGTH_LONG)
                .show()
        } else {
            Toast.makeText(this,"Game Over; try breaking the record next time!",Toast.LENGTH_LONG).show()
        }
        updateBestTimeTexView()
    }

    private fun updateBestTimeTexView() {
        // getting preferences
        val prefs = getSharedPreferences("mBestTimeKey", MODE_PRIVATE)

        // mBestTime = prefs.getInt("best_time", 0) //0 is the default value
        // sharedPreferences cannot store double values (mCurrentTime) so use this function
        // from https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
        mBestTime = getDoubleFromPrefs(prefs, "best_time", 0.0)
        Log.i(
            "PANJUTA",
            "getDoubleFromPrefs mBestTime: $mBestTime"
        )

        // todo: tv_best_timer string format min:sec:millis

        val minutes = mBestTime / 60000
        val intMinutes = floor(minutes.toDouble())
        val seconds = (minutes - intMinutes) * 60.0
        val intSeconds = floor(seconds)
        val hundreths = (seconds- intSeconds) * 100.0

        tv_best_timer.text = String.format("%02d:%02d:%02d", intMinutes.toInt(), intSeconds.toInt(), hundreths.toInt())

    }

    private fun putDoubleToPrefs(edit: SharedPreferences.Editor, key: String?, value: Double): SharedPreferences.Editor? {
        return edit.putLong(key, java.lang.Double.doubleToRawLongBits(value))
    }

    private fun getDoubleFromPrefs(prefs: SharedPreferences, key: String?, defaultValue: Double): Double {
        return java.lang.Double.longBitsToDouble(
            prefs.getLong(
                key,
                java.lang.Double.doubleToLongBits(defaultValue)
            )
        )
    }


    private fun checkBestMoves(score: Int) {
        // this function is called when game ends
        // set currentMoves as bestMoves if no bestMoves yet (0) or currentMoves smaller than best Moves
        if (mBestMoves == 0 || mCurrentMoves < mBestMoves) {

            //setting preferences
            val prefs = getSharedPreferences("mBestMovesKey", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putInt("best_moves", score)
            editor.commit()

            blinkView(tv_best_moves, true)
            blinkView(tv_current_moves, true)
            mSound.playShortResource(
                R.raw.perfect
            )
            Toast.makeText(applicationContext, "You set a Best Moves record!!", Toast.LENGTH_LONG)
                .show()
        } else {
            Toast.makeText(this,"Game Over; try breaking the record next time!",Toast.LENGTH_LONG).show()
        }
        updateBestMovesTexView()
    }

    private fun updateBestMovesTexView() {
        // getting preferences
        val prefs = getSharedPreferences("mBestMovesKey", MODE_PRIVATE)
        mBestMoves = prefs.getInt("best_moves", 0) //0 is the default value
        tv_best_moves.text = mBestMoves.toString()
    }


    private fun selectAndShuffleTileContent() {

        mSelectedFlagsResID = selectRandomFlags(mAllFlagsResID, mNumberOfTiles / 2, null)

        findSelectedShortenedCountryNames(mSelectedFlagsResID) // for toast that appears above tile

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

    private fun setOnClickListenerBottomButtons() {
        btn_quit3.setOnClickListener(this)
        btn_restart3.setOnClickListener(this)
    }

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
        when (v?.id) {
            R.id.btn_quit3 -> {
                mSound.release()
                finish()
            }
            R.id.btn_restart3 -> { // onCreateHelper()
                mSound.release()
                val intent = Intent(this, SplashScreenActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.iv_tile_01 -> {
                Log.i("PANJUTA", "imageview 01 clicked")

            }
            R.id.tv_tile_01 -> {
                Log.i("PANJUTA", "textview 01 clicked")
            }
        }

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
            animation.repeatMode = Animation.REVERSE //animation will start from end point once ended
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
    }
}