package com.tjknsoft.tjflagquiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        btn_start.setOnClickListener {
            if (et_name.text.toString().isEmpty()) {
                    Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_LONG).show()}
                    else {
                        val intent = Intent(this, QuizChooseNameActivity::class.java)
                        startActivity(intent)
                        finish()
                    }


        }
    }
}