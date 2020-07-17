package com.sayantanbanerjee.voicechanger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*


class MainActivity : AppCompatActivity() {

    private var file: File =
        File(Environment.getExternalStorageDirectory().absolutePath, "test.pcm")
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
        frequencies.add("14025")
        frequencies.add("16000")
        frequencies.add("18000")
        frequencies.add("20000")
        frequencies.add("22050")
        frequencies.add("30000")
        frequencies.add("40000")
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, frequencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFrequency.adapter = adapter

        // start record button pressed
        record.setOnClickListener {
            Thread {
                recording = true

                try {
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val intent: Intent = Intent(this, RecordActivity::class.java)
                        startActivity(intent)
                        startRecord()
                    } else {
                        val permissionArrays = arrayOf<String>(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        ActivityCompat.requestPermissions(
                            this,
                            permissionArrays,
                            1
                        );
                    }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent: Intent = Intent(this, RecordActivity::class.java)
                startActivity(intent)
                startRecord()
            } else {
                Toast.makeText(this, getString(R.string.denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playRecord() {
        CoroutineScope(Dispatchers.IO).launch {
            var outputFrequency: Int = 0
            val str: String = spFrequency.selectedItem.toString()
            outputFrequency = Integer.parseInt(str)

            val shortSizeInBytes = Short.SIZE_BYTES / Byte.SIZE_BYTES

            Log.i("####FILE", file.length().toString())

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

            // TODO : Here I have hard-coded 10000 as bufferSizeInBytes value whereas it should be "bufferedSizeInBytes" variable
            audioTrack = AudioTrack(3, outputFrequency, 2, 2, 10000, 1)
            audioTrack.play()
            audioTrack.write(audioData, 0, bufferedSizeInBytes)
        }
    }

    private fun startRecord() {
        val myFile: File = File(Environment.getExternalStorageDirectory().absolutePath, "test.pcm")
        myFile.createNewFile()
        CoroutineScope(Dispatchers.IO).launch {
            val outputStream: OutputStream = FileOutputStream(myFile)
            val bufferedOutputStream: BufferedOutputStream = BufferedOutputStream(outputStream)
            val dataOutputStream: DataOutputStream = DataOutputStream(bufferedOutputStream)

            val minBufferSize: Int = AudioRecord.getMinBufferSize(11025, 2, 2)
            val audioData: ShortArray = ShortArray(minBufferSize)
            val audioRecord: AudioRecord = AudioRecord(1, 11025, 2, 2, minBufferSize)
            audioRecord.startRecording()

            while (recording) {
                val numberOfShort = audioRecord.read(audioData, 0, minBufferSize)
                for (i in 0 until numberOfShort - 1) {
                    dataOutputStream.writeShort(audioData[i].toInt())
                }
            }

            if (!recording) {
                audioRecord.stop()
                dataOutputStream.close()
            }
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
