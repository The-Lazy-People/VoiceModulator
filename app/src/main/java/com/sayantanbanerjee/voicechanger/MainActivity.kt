package com.sayantanbanerjee.voicechanger

import android.content.Intent
import android.media.AudioTrack
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import java.io.File

class MainActivity : AppCompatActivity() {

    val file: File = File(Environment.getExternalStorageDirectory(), "test.pcm")
    private var recording: Boolean = false
    private lateinit var spFrequency: Spinner
    private lateinit var record: Button
    private lateinit var play: Button
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var audioTrack: AudioTrack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        record = findViewById(R.id.record)
        play = findViewById(R.id.play)
        spFrequency = findViewById(R.id.spinner)

        // setting up frequencies and the spinner
        val frequencies: ArrayList<String> = ArrayList<String>(8)
        frequencies.add("5000")
        frequencies.add("5000")
        frequencies.add("6050")
        frequencies.add("8500")
        frequencies.add("11025")
        frequencies.add("16000")
        frequencies.add("22050")
        frequencies.add("30000")
        frequencies.add("40000")
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, frequencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFrequency.adapter = adapter

        // record button pressed
        record.setOnClickListener {
            val intent: Intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
            Thread {
                recording = true
                startRecord()
            }.start()

        }

        // play record button pressed
        play.setOnClickListener {
            if (file.exists()) {
                playRecord()
            }
        }
    }

    private fun playRecord() {

    }

    private fun startRecord() {

    }

    override fun onDestroy() {
        super.onDestroy()
        recording = false
        if (audioTrack != null) {
            audioTrack.release()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}