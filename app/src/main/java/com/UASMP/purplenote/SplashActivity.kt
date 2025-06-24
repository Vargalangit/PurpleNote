package com.UASMP.purplenote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.startup2)

        // URI untuk video dari folder raw
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.startup2}")
        videoView.setVideoURI(videoUri)

        // Mulai video
        videoView.start()

        // Ketika video selesai diputar, langsung lanjut ke MainActivity
        videoView.setOnCompletionListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Jika terjadi error saat memutar video
        videoView.setOnErrorListener { _, _, _ ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            true
        }
    }
}
