package com.tjknsoft.tjflagquiz

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button


class RestartActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restart)

        // If you want to stop your dialog / activity from being destroyed when the user clicks outside of the dialog:
        // from https://stackoverflow.com/questions/54139503/pause-activity-when-the-alertdialog-is-shown
        this.setFinishOnTouchOutside(false)

        val btnRestartOk = findViewById<Button>(R.id.btn_restart_ok)
        btnRestartOk.setOnClickListener(this)
        val btnRestartCancel = findViewById<Button>(R.id.btn_restart_cancel)
        btnRestartCancel.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.btn_restart_ok) {
            val intent = Intent()
            intent.putExtra("KeyRestart", "Restart")
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            val intent = Intent()
            intent.putExtra("KeyRestart", "Cancel")
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }

}