package com.amirsteinbeck.focusmate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amirsteinbeck.focusmate.databinding.ActivityCredentialsBinding
import androidx.core.net.toUri
import com.amirsteinbeck.focusmate.com.amirsteinbeck.focusmate.LocaleHelper

class CredentialsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val context = newBase?.let { LocaleHelper.applyLanguage(it) }
        super.attachBaseContext(context)
    }

    private lateinit var binding: ActivityCredentialsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCredentialsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.githubLogo.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://github.com/Amirhossein77-98".toUri())
            startActivity(intent)
        }
        binding.backButton.setOnClickListener {
            NavigationHelper.goBack(this)
        }
    }
}