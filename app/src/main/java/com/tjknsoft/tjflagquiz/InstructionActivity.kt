package com.tjknsoft.tjflagquiz

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView


class InstructionActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var cbDoNotShowAgain: CheckBox
    var isCBchecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction)

        // If you want to stop your dialog / activity from being destroyed when the user clicks outside of the dialog:
        // from https://stackoverflow.com/questions/54139503/pause-activity-when-the-alertdialog-is-shown
        this.setFinishOnTouchOutside(false)

        cbDoNotShowAgain = findViewById(R.id.cb_do_not_show_again)

        isCBchecked = cbDoNotShowAgain.isChecked

        val tvStart = findViewById<TextView>(R.id.tv_start)
        tvStart.setOnClickListener(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(
            "PANJUTA",
            "InstructionActivity; onDestroy() called"
        )

    }

    override fun onClick(view: View?) {
        val intent = Intent()
        intent.putExtra("KeyCB", cbDoNotShowAgain.isChecked.toString())
        setResult(Activity.RESULT_OK, intent)
        Log.i(
            "PANJUTA",
            "InstructionActivity; cbDoNotShowAgain.isChecked: ${cbDoNotShowAgain.isChecked}"
        )
        finish()
    }

}