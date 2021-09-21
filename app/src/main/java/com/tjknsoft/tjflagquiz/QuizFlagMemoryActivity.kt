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
    private var mAllCountryCodes: ArrayList<String> = arrayListOf()
    private var mAllCountryNames: ArrayList<String> = arrayListOf()
    private val mNumberOfTiles: Int = 40
    private var mMapFlagResIDtoCountryCode = mutableMapOf<Int, String>()
    private var mMapCountryCodeToCountryName = mutableMapOf<String, String>()
    private var mSelectedFlagsResID: ArrayList<Int> = arrayListOf()
    private var mSelectedCountryCodes: ArrayList<String> = arrayListOf()
    private var mSelectedCountryNames: ArrayList<String> = arrayListOf()
    private var mSelectedFlagResIDandCountryCode: MutableList<Any> = arrayListOf()
    private var mShuffledSelectedFlagResIDandCountryCode: ArrayList<Any> = arrayListOf()
    private var mTileImageViews: ArrayList<ImageView> = arrayListOf()
    private var mTileTextViews: ArrayList<TextView> = arrayListOf()
    private var mIsFlagActive: Boolean = false

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
        mAllCountryCodes = Constants.allCountryCodes
        mAllCountryNames = Constants.allCountryNames
        mAllFlagsResID = SplashScreenActivity.mAllFlagsResID

        mapFlagResIDToCountryCode(mAllFlagsResID)
        mapCountryCodeToCountryName(mAllCountryCodes)
    }

    private fun drawTiles() {
        for ((index, tile) in mTileList.withIndex()) {
            if (tile.countryCode == "") { // tile displays flag image
                mIsFlagActive = false

                mTileTextViews[index].setVisibility(View.GONE)
                mTileImageViews[index].setVisibility(View.VISIBLE)

                if (tile.isFaceUp == true) {
                    mTileImageViews[index].setImageResource(tile.flagResId)
                } else {
                    mTileImageViews[index].setImageResource(R.drawable.tv_background_selected)
                }

                // disable all flag tile before user tap on country tile
                // mTileImageViews[index].isEnabled = false

                mTileImageViews[index].setOnClickListener {

                    if (mIsFlagActive == false) { // if user prevented from tapping the flag tile
                        displayToastAboveButton(
                            mTileImageViews[index],
                            "Please tap a white tile first"
                        )

                    } else {
                        // if user tap on face-down flag imageview, it will reveal face-up flag …
                        // …side for 2 seconds. After 2 seconds, flag is face-down again.
                        if (tile.isFaceUp == false) mTileImageViews[index].setImageResource(tile.flagResId)
                        val timer = object : CountDownTimer(2000, 1000) {

                            override fun onTick(millisUntilFinished: Long) {
                                // mTextField.setText("seconds remaining: " + millisUntilFinished / 1000)
                            }

                            override fun onFinish() {
                                mTileImageViews[index].setImageResource(R.drawable.tv_background_selected)
                            }
                        }
                        timer.start()
                        mIsFlagActive = false
                    }
                }
            } else { // tile displays country code
                mTileImageViews[index].setVisibility(View.GONE)
                mTileTextViews[index].setVisibility(View.VISIBLE)
                mTileTextViews[index].text = tile.countryCode
                mTileTextViews[index].setOnClickListener {
                    // if user tap on a tile containing country code, show a toast above that tile cbout its country name
                    displayToastAboveButton(mTileTextViews[index], tile.countryName)
                    // show border or tapped tile
                    mTileTextViews[index].background =
                        ContextCompat.getDrawable(this, R.drawable.tv_border_selected)
                    // user can tap on closed flag tile after tapping country tile first
                    mIsFlagActive = true
                }
            }
        }
    }

    private fun selectAndShuffleTileContent() {
        // select random 20 flag resID (20 is 40/2 ie. half of number of tiles; the other half is filled with country codes associated with those 20 flags

        mSelectedFlagsResID = selectRandomFlags(mAllFlagsResID, mNumberOfTiles / 2, null)

        findSelectedCountryCodes(mSelectedFlagsResID)
        findSelectedCountryNames(mSelectedCountryCodes)

        mSelectedFlagResIDandCountryCode =
            combineArrayListOfDifferentTypes(mSelectedFlagsResID, mSelectedCountryCodes)
        Log.i(
            "PANJUTA",
            "mSelectedFlagResIDandCountryCode size: ${mSelectedFlagResIDandCountryCode.size}"
        )

        //  mShuffledSelectedFlagResIDandCountryCode =
        //  mSelectedFlagResIDandCountryCode.shuffled() as ArrayList<Any>

        // shuffle
        mShuffledSelectedFlagResIDandCountryCode =
            fisherYatesShuffle(mSelectedFlagResIDandCountryCode) as ArrayList<Any>
    }

    private fun populateTileList() {
        for ((index, tileContent) in mShuffledSelectedFlagResIDandCountryCode.withIndex()) {
            Log.i(
                "PANJUTA",
                "tile position ${index + 1}: $tileContent"
            )
            //check variable types:
            when (tileContent) {
                // flag image resID (int); face down
                is Int -> mTileList.add(Tile(index + 1, false, tileContent.toInt(), "", ""))
                // country code (string)
                is String -> mTileList.add(
                    Tile(
                        index + 1, true, -1, tileContent.toString(),
                        mMapCountryCodeToCountryName[tileContent]!!
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

    private fun mapFlagResIDToCountryCode(allFlagsResID: ArrayList<Int>) {
        for ((index, flagResID) in allFlagsResID.withIndex()) {
            mMapFlagResIDtoCountryCode.put(flagResID, Constants.allCountryCodes[index])
            Log.i(
                "PANJUTA",
                "flag ResID: $flagResID, Country code: ${Constants.allCountryCodes[index]}"
            )
        }
    }

    private fun mapCountryCodeToCountryName(allCountryCodes: ArrayList<String>) {
        for ((index, countryCode) in allCountryCodes.withIndex()) {
            mMapCountryCodeToCountryName.put(countryCode, Constants.allCountryNames[index])
            Log.i(
                "PANJUTA",
                "Country code: $countryCode, Country name: ${Constants.allCountryNames[index]}"
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

    private fun findSelectedCountryCodes(selectedFlagsResID: ArrayList<Int>) {
        // Log.i("PANJUTA", "inside findSelectedCountryCode, parameter size is ${selectedFlagsResID.size}")
        for ((index, flagResID) in selectedFlagsResID.withIndex()) {
            //Log.i("PANJUTA", "mMapFlagResIDtoCountryCode[flagResID]: ${mMapFlagResIDtoCountryCode[flagResID]!!}")
            val countryCode = mMapFlagResIDtoCountryCode[flagResID]!!
            mSelectedCountryCodes.add(countryCode)
            Log.i("PANJUTA", "Selected country codes: $countryCode")
        }
        Log.i("PANJUTA", "mSelectedCountryCodes size: ${mSelectedCountryCodes.size}")
    }

    private fun findSelectedCountryNames(selectedCountryCodes: ArrayList<String>) {
        for ((index, countryCode) in selectedCountryCodes.withIndex()) {
            val countryName = mMapCountryCodeToCountryName[countryCode]!!
            mSelectedCountryNames.add(countryName)
            Log.i("PANJUTA", "Selected country names: $countryName")
        }
        Log.i("PANJUTA", "mSelectedCountryNames size: ${mSelectedCountryNames.size}")
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
        mTileImageViews.add(0, iv_tile_02)
        mTileImageViews.add(0, iv_tile_03)
        mTileImageViews.add(0, iv_tile_04)
        mTileImageViews.add(0, iv_tile_05)
        mTileImageViews.add(0, iv_tile_06)
        mTileImageViews.add(0, iv_tile_07)
        mTileImageViews.add(0, iv_tile_08)
        mTileImageViews.add(0, iv_tile_09)
        mTileImageViews.add(0, iv_tile_10)
        mTileImageViews.add(0, iv_tile_11)
        mTileImageViews.add(0, iv_tile_12)
        mTileImageViews.add(0, iv_tile_13)
        mTileImageViews.add(0, iv_tile_14)
        mTileImageViews.add(0, iv_tile_15)
        mTileImageViews.add(0, iv_tile_16)
        mTileImageViews.add(0, iv_tile_17)
        mTileImageViews.add(0, iv_tile_18)
        mTileImageViews.add(0, iv_tile_19)
        mTileImageViews.add(0, iv_tile_20)
        mTileImageViews.add(0, iv_tile_21)
        mTileImageViews.add(0, iv_tile_22)
        mTileImageViews.add(0, iv_tile_23)
        mTileImageViews.add(0, iv_tile_24)
        mTileImageViews.add(0, iv_tile_25)
        mTileImageViews.add(0, iv_tile_26)
        mTileImageViews.add(0, iv_tile_27)
        mTileImageViews.add(0, iv_tile_28)
        mTileImageViews.add(0, iv_tile_29)
        mTileImageViews.add(0, iv_tile_30)
        mTileImageViews.add(0, iv_tile_31)
        mTileImageViews.add(0, iv_tile_32)
        mTileImageViews.add(0, iv_tile_33)
        mTileImageViews.add(0, iv_tile_34)
        mTileImageViews.add(0, iv_tile_35)
        mTileImageViews.add(0, iv_tile_36)
        mTileImageViews.add(0, iv_tile_37)
        mTileImageViews.add(0, iv_tile_38)
        mTileImageViews.add(0, iv_tile_39)
        mTileImageViews.add(0, iv_tile_40)

        mTileTextViews.add(0, tv_tile_01)
        mTileTextViews.add(0, tv_tile_02)
        mTileTextViews.add(0, tv_tile_03)
        mTileTextViews.add(0, tv_tile_04)
        mTileTextViews.add(0, tv_tile_05)
        mTileTextViews.add(0, tv_tile_06)
        mTileTextViews.add(0, tv_tile_07)
        mTileTextViews.add(0, tv_tile_08)
        mTileTextViews.add(0, tv_tile_09)
        mTileTextViews.add(0, tv_tile_10)
        mTileTextViews.add(0, tv_tile_11)
        mTileTextViews.add(0, tv_tile_12)
        mTileTextViews.add(0, tv_tile_13)
        mTileTextViews.add(0, tv_tile_14)
        mTileTextViews.add(0, tv_tile_15)
        mTileTextViews.add(0, tv_tile_16)
        mTileTextViews.add(0, tv_tile_17)
        mTileTextViews.add(0, tv_tile_18)
        mTileTextViews.add(0, tv_tile_19)
        mTileTextViews.add(0, tv_tile_20)
        mTileTextViews.add(0, tv_tile_21)
        mTileTextViews.add(0, tv_tile_22)
        mTileTextViews.add(0, tv_tile_23)
        mTileTextViews.add(0, tv_tile_24)
        mTileTextViews.add(0, tv_tile_25)
        mTileTextViews.add(0, tv_tile_26)
        mTileTextViews.add(0, tv_tile_27)
        mTileTextViews.add(0, tv_tile_28)
        mTileTextViews.add(0, tv_tile_29)
        mTileTextViews.add(0, tv_tile_30)
        mTileTextViews.add(0, tv_tile_31)
        mTileTextViews.add(0, tv_tile_32)
        mTileTextViews.add(0, tv_tile_33)
        mTileTextViews.add(0, tv_tile_34)
        mTileTextViews.add(0, tv_tile_35)
        mTileTextViews.add(0, tv_tile_36)
        mTileTextViews.add(0, tv_tile_37)
        mTileTextViews.add(0, tv_tile_38)
        mTileTextViews.add(0, tv_tile_39)
        mTileTextViews.add(0, tv_tile_40)
    }
}