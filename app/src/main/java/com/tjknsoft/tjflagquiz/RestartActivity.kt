package com.tjknsoft.tjflagquiz

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button


class RestartActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restart)

        // If you want to stop your dialog / activity from being destroyed when the user clicks outside of the dialog:
        // from https://stackoverflow.com/questions/54139503/pause-activity-when-the-alertdialog-is-shown
        this.setFinishOnTouchOutside(false)

        val btnRestartYes = findViewById<Button>(R.id.btn_restart_yes)
        btnRestartYes.setOnClickListener(this)
        val btnRestartNo = findViewById<Button>(R.id.btn_restart_no)
        btnRestartNo.setOnClickListener(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(
            "PANJUTA",
            "RestartActivity; onDestroy() called"
        )

    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.btn_restart_yes){
            val intent = Intent()
            intent.putExtra("KeyRestart", "Yes")
            setResult(Activity.RESULT_OK, intent)
            Log.i(
                "PANJUTA",
                "RestartActivity; intent extra: ${intent.getStringExtra("KeyRestart")}"
            )
            finish()
        } else {
            finish()
        }

    }

}