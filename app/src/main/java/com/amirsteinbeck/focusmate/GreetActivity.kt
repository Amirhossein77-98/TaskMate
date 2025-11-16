package com.amirsteinbeck.focusmate

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amirsteinbeck.focusmate.databinding.ActivityGreetBinding

class GreetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGreetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGreetBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val name = intent.getStringExtra("USERNAME")
        binding.greetTextView.text = if (!name.isNullOrEmpty())
            getString(R.string.welcomeToUser, name)
        else
            getString(R.string.welcomeText)

        binding.backButton.setOnClickListener {
            NavigationHelper.goBack(this)
        }
    }
}