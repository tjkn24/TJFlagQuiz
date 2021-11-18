package com.tjknsoft.tjflagquiz

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView

private var countdown_seconds = 0L
private const val countdown_duration = 31000L
private const val countdown_interval = 1000L
lateinit var countdown_timer: CountDownTimer

class IdleActivity : AppCompatActivity(), View.OnClickListener {

    private var mButtonContinue: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idle)

        // If you want to stop your dialog / activity from being destroyed when the user clicks outside of the dialog:
        // from https://stackoverflow.com/questions/54139503/pause-activity-when-the-alertdialog-is-shown
        this.setFinishOnTouchOutside(false)

        mButtonContinue = findViewById(R.id.btn_idle_continue)
        mButtonContinue?.setOnClickListener(this)

        val idleDelayedMinutes = this.intent.getIntExtra("idleDelayedMinutes", 10)
        val tvIdleMessage: TextView = findViewById(R.id.tv_idle_message)
        tvIdleMessage.text = "There are no game activities for $idleDelayedMinutes minutes. \nAre you sure you want to continue?"

        startTimer()
    }

    private fun startTimer() {
        countdown_timer = object : CountDownTimer(countdown_duration, countdown_interval) {
            override fun onTick(p0: Long) {
                countdown_seconds = p0/1000
                mButtonContinue?.text = "Continue  ($countdown_seconds)"
            }

            override fun onFinish() {
                Log.i(
                        "PANJUTA",
                        "IdleActivity.kt, onFinish() called"
                )
                intent.putExtra("KeyIdle", "TimeIsUp")
                setResult(Activity.RESULT_OK, intent)
                finish()

            }
        }
        countdown_timer.start()
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