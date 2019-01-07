package com.example.malattas.imageprocessingtools

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        btnColorTracking.setOnClickListener {
            val colorTrackingIntent = Intent(this@WelcomeActivity, TrackingColorActivity::class.java)
            startActivity(colorTrackingIntent)
        }

        btnTextRecognition.setOnClickListener {
            val textRecognitionIntent = Intent(this@WelcomeActivity, TextRecognitionActivity::class.java)
            startActivity(textRecognitionIntent)
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
