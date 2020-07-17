package com.sayantanbanerjee.voicechanger

import android.content.Intent
import android.media.AudioRecord
import android.media.AudioTrack
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import java.io.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    val file: File = File(Environment.getExternalStorageDirectory(), "test.pcm")
    private lateinit var spFrequency: Spinner
    private lateinit var record: Button
    private lateinit var play: Button
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var audioTrack: AudioTrack

    companion object {
        var recording: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        record = findViewById(R.id.record)
        play = findViewById(R.id.play)
        spFrequency = findViewById(R.id.spinner)

        // setting up frequencies and the spinner
        val frequencies: ArrayList<String> = ArrayList<String>(8)
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

        // start record button pressed
        record.setOnClickListener {
            val intent: Intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
            Thread {
                recording = true

                try {
                    startRecord()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

            }.start()

        }

        // play recording button pressed
        play.setOnClickListener {
            if (file.exists()) {

                try {
                    playRecord()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }

    private fun playRecord() {
        var outputFrequency: Int = 0
        val str: String = spFrequency.selectedItem.toString()
        outputFrequency = Integer.parseInt(str)

        val shortSizeInBytes = Short.SIZE_BYTES / Byte.SIZE_BYTES

        val bufferedSizeInBytes: Int = (file.length() / shortSizeInBytes).toInt()
        val audioData: ShortArray = ShortArray(bufferedSizeInBytes)

        val inputStream: InputStream = FileInputStream(file)
        val bufferedInputStream: BufferedInputStream = BufferedInputStream(inputStream)
        val dataInputStream: DataInputStream = DataInputStream(bufferedInputStream)

        var j: Int = 0
        while (dataInputStream.available() > 0) {
            audioData[j] = dataInputStream.readShort()
            j++
        }
        dataInputStream.close()

        audioTrack = AudioTrack(3, outputFrequency, 2, 2, bufferedSizeInBytes, 1)
        audioTrack.play()
        audioTrack.write(audioData, 0, bufferedSizeInBytes)


    }

    private fun startRecord() {
        val myFile: File = File(Environment.getExternalStorageDirectory().absolutePath, "test.pcm")
        myFile.createNewFile()
        val outputStream: OutputStream = FileOutputStream(myFile)
        val bufferedOutputStream: BufferedOutputStream = BufferedOutputStream(outputStream)
        val dataOutputStream: DataOutputStream = DataOutputStream(bufferedOutputStream)

        val minBufferSize: Int = AudioRecord.getMinBufferSize(11025, 2, 2)
        val audioData: ShortArray = ShortArray(minBufferSize)
        val audioRecord: AudioRecord = AudioRecord(1, 11025, 2, 2, minBufferSize)
        audioRecord.startRecording()

        while (recording) {
            val numberOfShort = audioRecord.read(audioData, 0, minBufferSize)
            for (i in 0..numberOfShort) {
                dataOutputStream.writeShort(audioData[i].toInt())
            }
        }

        if (!recording) {
            audioRecord.stop()
            dataOutputStream.close()
        }
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
