package com.tjknsoft.tjflagquiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_quiz_flag_memory.*
import kotlin.collections.ArrayList
import kotlin.random.Random

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_flag_memory)

        onCreateHelper()
    }

    private fun onCreateHelper() {
        mAllCountryCodes = Constants.allCountryCodes
        mAllCountryNames = Constants.allCountryNames
        mAllFlagsResID = SplashScreenActivity.mAllFlagsResID

        mapFlagResIDToCountryCode(mAllFlagsResID)

        mapCountryCodeToCountryName(mAllCountryCodes)

        // select random 20 flag resID (20 is 40/2 ie. half of number of tiles; the other half is filled with country codes associated with those 20 flags
        mSelectedFlagsResID = selectRandomFlags(mAllFlagsResID, mNumberOfTiles / 2, null)

        findSelectedCountryCodes(mSelectedFlagsResID)
        findSelectedCountryNames(mSelectedCountryCodes)

        mSelectedFlagResIDandCountryCode = combine(mSelectedFlagsResID, mSelectedCountryCodes)
        Log.i(
            "PANJUTA",
            "mSelectedFlagResIDandCountryCode size: ${mSelectedFlagResIDandCountryCode.size}"
        )

//        mShuffledSelectedFlagResIDandCountryCode =
//            mSelectedFlagResIDandCountryCode.shuffled() as ArrayList<Any>

        mShuffledSelectedFlagResIDandCountryCode =
            fisherYatesShuffle(mSelectedFlagResIDandCountryCode) as ArrayList<Any>
        for ((index, tileContent) in mShuffledSelectedFlagResIDandCountryCode.withIndex()) {
            Log.i(
                "PANJUTA",
                "tile position ${index + 1}: $tileContent"
            )
            //check variable types:
            when (tileContent) {
                is Int -> mTileList.add(Tile(index + 1, true, tileContent.toInt(), "", ""))
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
                "Tile: ${mTileList[index]}")
        }
        Log.i(
            "PANJUTA",
            "mTileList size: ${mTileList.size}"
        )



        btn_quit3.setOnClickListener(this)
        btn_restart3.setOnClickListener(this)

        iv_tile_01.setOnClickListener(this)
        tv_tile_01.setOnClickListener(this)
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
    fun <T, U> combine(first: ArrayList<T>, second: ArrayList<U>): MutableList<Any> {
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
}