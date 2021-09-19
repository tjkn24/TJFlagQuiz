package com.tjknsoft.tjflagquiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_quiz_flag_memory.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_flag_memory)

        onCreateHelper()
    }

    private fun onCreateHelper() {
        mAllCountryCodes = Constants.allCountryCodes
        mAllCountryNames = Constants.allCountryNames
        mAllFlagsResID = SplashScreenActivity.mAllFlagsResID
//        Log.i(
//            "PANJUTA",
//            "mAllCountryCodes size: ${mAllCountryCodes.size}, mAllCountryNames size: ${mAllCountryNames.size}, mAllFlagsResID: ${mAllFlagsResID.size}"
//        )

        mapFlagResIDToCountryCode(mAllFlagsResID)
        mapCountryCodeToCountryName(mAllCountryCodes)

        // select random 20 flag resID (20 is 40/2 ie. half of number of tiles; the other half is filled with country codes associated with those 20 flags
        mSelectedFlagsResID = selectRandomFlags(mAllFlagsResID,mNumberOfTiles/2, null)



        btn_quit3.setOnClickListener(this)
        btn_restart3.setOnClickListener(this)

        iv_tile_01.setOnClickListener(this)
        tv_tile_01.setOnClickListener(this)
    }



    private fun mapFlagResIDToCountryCode(allFlagsResID: ArrayList<Int>) {
        for ((index, flagResID) in allFlagsResID.withIndex()) {
            mMapFlagResIDtoCountryCode.put(flagResID, Constants.allCountryCodes[index])
            Log.i("PANJUTA", "flag ResID: $flagResID, Country code: ${Constants.allCountryCodes[index]}")
        }
    }

    private fun mapCountryCodeToCountryName(allCountryCodes: ArrayList<String>){
        for ((index, countryCode) in allCountryCodes.withIndex()) {
            mMapCountryCodeToCountryName.put(countryCode, Constants.allCountryNames[index])
            Log.i("PANJUTA", "Country code: $countryCode, Country name: ${Constants.allCountryNames[index]}")
        }
    }

    private fun selectRandomFlags(
        allFlagsResID: ArrayList<Int>,
        halfNumberOfTiles: Int, excludedFlagResID: Int?

    ): ArrayList<Int>  {
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
            if (!selectedFlagsResID.contains(flagResID) ) {
                selectedFlagsResID.add(flagResID)
            }
        }
        for (flagResID in selectedFlagsResID) {
            Log.i("PANJUTA", "Selected flag resource IDs: $flagResID")
        }

        return selectedFlagsResID
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