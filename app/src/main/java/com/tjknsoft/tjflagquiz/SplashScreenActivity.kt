package com.tjknsoft.tjflagquiz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {

    companion object {
        val context by lazy { App.context }
        val mAllFlagsResID: ArrayList<Int> = arrayListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // todo: tjsoft logo splash screen
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        btn_start.setOnClickListener {
//            if (et_name.text.toString().isEmpty()) {
//                Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_LONG).show()
//            } else {
                val intent = Intent(this, QuizChooseNameActivity::class.java)
                startActivity(intent)
                finish()
//            }
        }
        btn_start2.setOnClickListener {
            val intent = Intent(this, QuizChooseFlagActivity::class.java)
            startActivity(intent)
            finish()
        }
        btn_memory_easy.setOnClickListener {
            val intent = Intent(this, QuizFlagMemoryActivity::class.java)
            intent.putExtra("memory", 30)
            startActivity(intent)
            finish()
        }
        btn_memory_medium.setOnClickListener {
            val intent = Intent(this, QuizFlagMemoryActivity::class.java)
            intent.putExtra("memory", 40)
            startActivity(intent)
            finish()
        }
        btn_memory_hard.setOnClickListener {
            val intent = Intent(this, QuizFlagMemoryActivity::class.java)
            intent.putExtra("memory", 50)
            startActivity(intent)
            finish()
        }
        loadFlagsResIDfromDrawable()
    }

    // load only flag pngs from drawables
    private fun loadFlagsResIDfromDrawable() {
        mAllFlagsResID.clear()
        for (flagResID: Int in R.drawable.flag_afghanistan..R.drawable.flag_zimbabwe) {
            val flagName = context?.resources?.getResourceEntryName(flagResID)
            //name is the file name without the extension, flagResID is the resource ID of the flag
            mAllFlagsResID.add(flagResID)
            Log.i("PANJUTA", "Splash Activity; File name & Resource Id: $flagName $flagResID")

        }
        // for (flagIdentifier in mAllFlags) {
        // Log.i("PANJUTA", "All resource ID: $flagIdentifier")
        // };
        Log.i("PANJUTA", "mAllFlags.size: ${mAllFlagsResID.size}")
    }
}