package com.tjknsoft.tjflagquiz

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
import androidx.core.content.ContextCompat


class QuizFlagMemoryActivity : AppCompatActivity(), View.OnClickListener {


    private var mTileList: ArrayList<Tile> = arrayListOf()
    private var mAllFlagsResID: ArrayList<Int> = arrayListOf()

    // private var mAllCountryCodes: ArrayList<String> = arrayListOf()
    private var mAllCountryNames: ArrayList<String> = arrayListOf()
    private var mAllShortenedCountryNames: ArrayList<String> = arrayListOf()
    private val mNumberOfTiles: Int = 40
    private var mMapFlagResIDtoCountryName = mutableMapOf<Int, String>()
    private var mMapFlagResIDtoShortenedCountryName = mutableMapOf<Int, String>()
    private var mMapShortenedCountryNameToCountryName = mutableMapOf<String, String>()
    private var mSelectedFlagsResID: ArrayList<Int> = arrayListOf()
    private var mSelectedShortenedCountryNames: ArrayList<String> = arrayListOf()
    private var mSelectedFlagResIDandShortenedCountryName: MutableList<Any> = arrayListOf()
    private var mShuffledSelectedFlagResIDandShortenedCountryName: ArrayList<Any> = arrayListOf()
    private var mTileImageViews: ArrayList<ImageView> = arrayListOf()
    private var mTileTextViews: ArrayList<TextView> = arrayListOf()
    private var mIsFlagActive: Boolean = true
    private var mIsNameActive: Boolean = true
    private var mTappedFlagResID: Int = -1
    private var mTappedShortenedCountryName: String = "-1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_flag_memory)

        onCreateHelper()
    }

    private fun onCreateHelper() {

        getAndMapData()

        selectAndShuffleTileContent()

        populateTileList()

        addViewsToViewList()

        drawTiles()

        setOnClickListenerBottomButtons()

    }


    private fun getAndMapData() {

        mAllCountryNames = Constants.allCountryNames
        mAllShortenedCountryNames = Constants.allShortenedCountryNames
        mAllFlagsResID = SplashScreenActivity.mAllFlagsResID
        mapFlagResIDToShortenedCountryName(mAllFlagsResID)
        mapShortenedCountryNameToCountryName(mAllShortenedCountryNames)
    }

    private fun drawTiles() {

        // draw tiles
        for ((index, tile) in mTileList.withIndex()) {
            if (tile.shortenedCountryName != "") {
                // tile displays shortened country name
                mTileImageViews[index].setVisibility(View.GONE)
                mTileTextViews[index].setVisibility(View.VISIBLE)
                if (tile.isFaceUp == true) {
                    Log.i(
                        "PANJUTA",
                        "entering (tile.isFaceUp == true): ${tile.isFaceUp}"
                    )
                    mTileTextViews[index].text = tile.shortenedCountryName
                } else {
                    Log.i(
                        "PANJUTA",
                        "entering (tile.isFaceUp != true): ${tile.isFaceUp},mTileTextViews[index]: ${mTileTextViews[index].text}"
                    )
                    // mTileTextViews[index].text = ""
                    // mTileTextViews[index].setBackgroundColor(null))
                    // mTileTextViews[index].setVisibility(View.INVISIBLE)
                }


                // set tiles' onClickListener
                mTileTextViews[index].setOnClickListener {
                    // mTileTextViews[index].setVisibility(View.VISIBLE)
                    // mTileTextViews[index].setBackgroundColor(getResources().getColor(R.color.white))
                    mTileTextViews[index].text = tile.shortenedCountryName
                    // if user tap on a tile containing country code, show a toast above that tile cbout its country name
                    displayToastAboveButton(mTileTextViews[index], tile.countryName)

                    // disable other text tiles
                    val otherTileTextViews =
                        mTileTextViews.filterNot { it == mTileTextViews[index] }
                    otherTileTextViews.forEach { it -> it.setClickable(false) }

                    // disable this text tile
                    mTileTextViews[index].setClickable(false)

                    // user can tap on closed flag tile after tapping country tile
                    // mIsFlagActive = true

                    if (mTappedShortenedCountryName == "-1") { // register tile only if the variable is empty
                        mTappedShortenedCountryName =
                            tile.shortenedCountryName // needs to be compared with tapped flag image after this one
                    }

                    if (mTappedShortenedCountryName != "-1" && mTappedFlagResID != -1)
                        compareTappedTiles(
                            mTappedShortenedCountryName,
                            mTappedFlagResID,
                            mTileTextViews[index],
                            mTileImageViews[index]
                        )

                }
            } else { // tile displays flag image
                // mIsFlagActive = false // user has to tap country tile for the very first time

                mTileTextViews[index].setVisibility(View.GONE)
                mTileImageViews[index].setVisibility(View.VISIBLE)

                if (tile.isFaceUp) {
                    mTileImageViews[index].setImageResource(tile.flagResId)
                } else {
                    mTileImageViews[index].setImageResource(R.drawable.tv_background_primary)
                }


                mTileImageViews[index].setOnClickListener {

                    if (mIsFlagActive) {

                        // if user tap on face-down flag imageview, it will reveal face-up flag side If tapped tile flag doesn't match with the tapped text tile, after 2 seconds, both tiles are face-down.

                        if (!tile.isFaceUp) mTileImageViews[index].setImageResource(tile.flagResId)

                        // disable other flag tiles
                        val otherTileImageViews =
                            mTileImageViews.filterNot { it == mTileImageViews[index] }
                        otherTileImageViews.forEach { it -> it.setClickable(false) }

                        // disable this flag tile
                        mTileImageViews[index].setClickable(false)

                        // user can tap on closed text tile after tapping country tile
                        // mIsNameActive = true

                        if (mTappedFlagResID == -1) { // register tile only if the variable is empty
                            mTappedFlagResID =
                                tile.flagResId // needs to be compared with tapped name tile
                        }


//                        val timer = object : CountDownTimer(2000, 1000) {
//
//                            override fun onTick(millisUntilFinished: Long) {
//                                // mTextField.setText("seconds remaining: " + millisUntilFinished / 1000)
//                            }
//
//                            override fun onFinish() {
//                                mTileImageViews[index].setImageResource(R.drawable.tv_background_primary)
//                            }
//                        }
//                        timer.start()
//                        mIsFlagActive =
//                            false // after flipping flag tile, user has to tap country tile again

                        if (mTappedShortenedCountryName != "-1" && mTappedFlagResID != -1)
                            compareTappedTiles(
                                mTappedShortenedCountryName,
                                mTappedFlagResID,
                                mTileTextViews[index],
                                mTileImageViews[index]
                            )
                    }
                }

            }
        }
    }

    private fun compareTappedTiles(
        tappedShortenedCountryName: String,
        tappedFlagResID: Int,
        tappedTextView: TextView,
        tappedImageView: ImageView
    ) {
        Log.i(
            "PANJUTA",
            "inside Compare, tappedShortenedCountryName: $tappedShortenedCountryName, tappedFlagResID: $tappedFlagResID, mMapFlagResIDtoShortenedCountryName[tappedFlagResID]: ${mMapFlagResIDtoShortenedCountryName[tappedFlagResID]}"
        )
        if (mMapFlagResIDtoShortenedCountryName[tappedFlagResID] == tappedShortenedCountryName) {
            Toast.makeText(this, "MATCHED!", Toast.LENGTH_LONG).show()
            // make both tiles disappear:
            tappedTextView.setText("")
            tappedTextView.alpha = 0.25F
            tappedTextView.isEnabled = false
            tappedImageView.alpha = 0.25F
            tappedImageView.isEnabled = false
        } else {
            Toast.makeText(this, "WRONG!", Toast.LENGTH_LONG).show()
            // make both tiles face-down:

        }

// reset tapped tiles
        mTappedFlagResID = -1
        mTappedShortenedCountryName = "-1"

        // enable all tiles
        for (tv in mTileTextViews) tv.setClickable(true)
        for (iv in mTileImageViews) iv.setClickable(true)
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
                finish()
            }
            R.id.btn_restart3 -> {// onCreateHelper()
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
    private fun displayToastAboveButton(v: View, message: String) {
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
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        val view: View? = toast.getView()
//        view?.setBackgroundResource(R.drawable.toast_background)
//        val toastview = toast.view!!.findViewById<View>(android.R.id.message) as TextView
//        toastview.setTextColor(Color.YELLOW)
//        toastview.setTextSize(20.0F)
        toast.setGravity(Gravity.CENTER, xOffset, yOffset)
        toast.show()
    }

    private fun addViewsToViewList() {
        mTileImageViews.add(0, iv_tile_01)
        mTileImageViews.add(1, iv_tile_02)
        mTileImageViews.add(2, iv_tile_03)
        mTileImageViews.add(3, iv_tile_04)
        mTileImageViews.add(4, iv_tile_05)
        mTileImageViews.add(5, iv_tile_06)
        mTileImageViews.add(6, iv_tile_07)
        mTileImageViews.add(7, iv_tile_08)
        mTileImageViews.add(8, iv_tile_09)
        mTileImageViews.add(9, iv_tile_10)
        mTileImageViews.add(10, iv_tile_11)
        mTileImageViews.add(11, iv_tile_12)
        mTileImageViews.add(12, iv_tile_13)
        mTileImageViews.add(13, iv_tile_14)
        mTileImageViews.add(14, iv_tile_15)
        mTileImageViews.add(15, iv_tile_16)
        mTileImageViews.add(16, iv_tile_17)
        mTileImageViews.add(17, iv_tile_18)
        mTileImageViews.add(18, iv_tile_19)
        mTileImageViews.add(19, iv_tile_20)
        mTileImageViews.add(20, iv_tile_21)
        mTileImageViews.add(21, iv_tile_22)
        mTileImageViews.add(22, iv_tile_23)
        mTileImageViews.add(23, iv_tile_24)
        mTileImageViews.add(24, iv_tile_25)
        mTileImageViews.add(25, iv_tile_26)
        mTileImageViews.add(26, iv_tile_27)
        mTileImageViews.add(27, iv_tile_28)
        mTileImageViews.add(28, iv_tile_29)
        mTileImageViews.add(29, iv_tile_30)
        mTileImageViews.add(30, iv_tile_31)
        mTileImageViews.add(31, iv_tile_32)
        mTileImageViews.add(32, iv_tile_33)
        mTileImageViews.add(33, iv_tile_34)
        mTileImageViews.add(34, iv_tile_35)
        mTileImageViews.add(35, iv_tile_36)
        mTileImageViews.add(36, iv_tile_37)
        mTileImageViews.add(37, iv_tile_38)
        mTileImageViews.add(38, iv_tile_39)
        mTileImageViews.add(39, iv_tile_40)

        mTileTextViews.add(0, tv_tile_01)
        mTileTextViews.add(1, tv_tile_02)
        mTileTextViews.add(2, tv_tile_03)
        mTileTextViews.add(3, tv_tile_04)
        mTileTextViews.add(4, tv_tile_05)
        mTileTextViews.add(5, tv_tile_06)
        mTileTextViews.add(6, tv_tile_07)
        mTileTextViews.add(7, tv_tile_08)
        mTileTextViews.add(8, tv_tile_09)
        mTileTextViews.add(9, tv_tile_10)
        mTileTextViews.add(10, tv_tile_11)
        mTileTextViews.add(11, tv_tile_12)
        mTileTextViews.add(12, tv_tile_13)
        mTileTextViews.add(13, tv_tile_14)
        mTileTextViews.add(14, tv_tile_15)
        mTileTextViews.add(15, tv_tile_16)
        mTileTextViews.add(16, tv_tile_17)
        mTileTextViews.add(17, tv_tile_18)
        mTileTextViews.add(18, tv_tile_19)
        mTileTextViews.add(19, tv_tile_20)
        mTileTextViews.add(20, tv_tile_21)
        mTileTextViews.add(21, tv_tile_22)
        mTileTextViews.add(22, tv_tile_23)
        mTileTextViews.add(23, tv_tile_24)
        mTileTextViews.add(24, tv_tile_25)
        mTileTextViews.add(25, tv_tile_26)
        mTileTextViews.add(26, tv_tile_27)
        mTileTextViews.add(27, tv_tile_28)
        mTileTextViews.add(28, tv_tile_29)
        mTileTextViews.add(29, tv_tile_30)
        mTileTextViews.add(30, tv_tile_31)
        mTileTextViews.add(31, tv_tile_32)
        mTileTextViews.add(32, tv_tile_33)
        mTileTextViews.add(33, tv_tile_34)
        mTileTextViews.add(34, tv_tile_35)
        mTileTextViews.add(35, tv_tile_36)
        mTileTextViews.add(36, tv_tile_37)
        mTileTextViews.add(37, tv_tile_38)
        mTileTextViews.add(38, tv_tile_39)
        mTileTextViews.add(39, tv_tile_40)
    }
}