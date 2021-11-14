package com.tjknsoft.tjflagquiz

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button


class IdleActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idle)

        // If you want to stop your dialog / activity from being destroyed when the user clicks outside of the dialog:
        // from https://stackoverflow.com/questions/54139503/pause-activity-when-the-alertdialog-is-shown
        this.setFinishOnTouchOutside(false)

        val btnIdleContinue = findViewById<Button>(R.id.btn_idle_continue)
        btnIdleContinue.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.btn_idle_continue) {
//            val intent = Intent()
//            intent.putExtra("KeyIdle", "Continue")
//            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}