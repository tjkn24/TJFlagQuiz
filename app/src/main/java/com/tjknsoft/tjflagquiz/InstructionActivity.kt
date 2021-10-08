package com.tjknsoft.tjflagquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox

class InstructionActivity : AppCompatActivity() {

    var cbDoNotShowAgain: CheckBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction)

        cbDoNotShowAgain = findViewById<CheckBox?>(R.id.cb_do_not_show_again)
    }

}