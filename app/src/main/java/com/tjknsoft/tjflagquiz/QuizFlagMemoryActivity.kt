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
    private var mNumberOfTiles: Int = 40


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_flag_memory)

        onCreateHelper()
    }

    private fun onCreateHelper() {
        mAllCountryCodes = Constants.CountryCodes
        mAllCountryNames = Constants.CountryNames
        mAllFlagsResID = SplashScreenActivity.mAllFlagsResID
//        Log.i(
//            "PANJUTA",
//            "mAllCountryCodes size: ${mAllCountryCodes.size}, mAllCountryNames size: ${mAllCountryNames.size}, mAllFlagsResID: ${mAllFlagsResID.size}"
//        )

        // select random 40 data (flag resID = 20, country code = 20) to fill all 40 tiles
        selectRandomDataToFillTiles(mAllFlagsResID, mAllCountryCodes, 40)

        btn_quit3.setOnClickListener(this)
        btn_restart3.setOnClickListener(this)

        iv_tile_01.setOnClickListener(this)
        tv_tile_01.setOnClickListener(this)
    }

    private fun selectRandomDataToFillTiles(
        allFlagsResID: ArrayList<Int>,
        allCountryCodes: ArrayList<String>,
        numberOfTiles: Int

    ) {
        // select random 40 data (flag resID = 20, country code = 20) to fill all 40 tile
        // first, select 20 random flag resID


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