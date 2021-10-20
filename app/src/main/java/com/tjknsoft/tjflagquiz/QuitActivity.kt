package com.tjknsoft.tjflagquiz

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button


class QuitActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quit)

        // If you want to stop your dialog / activity from being destroyed when the user clicks outside of the dialog:
        // from https://stackoverflow.com/questions/54139503/pause-activity-when-the-alertdialog-is-shown
        this.setFinishOnTouchOutside(false)

        val btnQuitYes = findViewById<Button>(R.id.btn_quit_yes)
        btnQuitYes.setOnClickListener(this)
        val btnQuitNo = findViewById<Button>(R.id.btn_quit_no)
        btnQuitNo.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.btn_quit_yes) {
            val intent = Intent()
            intent.putExtra("KeyQuit", "Yes")
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            val intent = Intent()
            intent.putExtra("KeyQuit", "No")
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}