package com.sayantanbanerjee.voicechanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RecordActivity : AppCompatActivity() {

    private lateinit var stopButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        stopButton = findViewById(R.id.stop)
        stopButton.setOnClickListener {
            MainActivity.recording = false
            this.finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        MainActivity.recording = false
        this.finish()
    }
}
