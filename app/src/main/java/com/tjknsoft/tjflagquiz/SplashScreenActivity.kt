package com.tjknsoft.tjflagquiz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {

    companion object {
        val context = App.context
        val mAllFlags: ArrayList<Int> = arrayListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        btn_start.setOnClickListener {
            if (et_name.text.toString().isEmpty()) {
                Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, QuizChooseNameActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        btn_start2.setOnClickListener {
            val intent = Intent(this, QuizChooseFlagActivity::class.java)
            startActivity(intent)
            finish()
        }
        loadDrawables()
    }

    // load only flag pngs from drawables
    private fun loadDrawables() {
        mAllFlags.clear()
        for (identifier in R.drawable.flag_afghanistan..R.drawable.flag_zimbabwe) {
            val name = context?.resources?.getResourceEntryName(identifier.toInt())
            //name is the file name without the extension, identifier is the resource ID
            mAllFlags.add(identifier)
            // Log.i("PANJUTA", "All File name & Resource Id: $name $identifier")

        }
        // for (flagIdentifier in mAllFlags) {
        // Log.i("PANJUTA", "All resource ID: $flagIdentifier")
        // };
        Log.i("PANJUTA", "mAllFlags.size: ${mAllFlags.size}")
    }
}