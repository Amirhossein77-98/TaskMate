package com.amirsteinbeck.focusmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amirsteinbeck.focusmate.com.amirsteinbeck.focusmate.LocaleHelper
import com.amirsteinbeck.focusmate.com.amirsteinbeck.focusmate.SettingsHelper
import com.amirsteinbeck.focusmate.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val context = newBase?.let { LocaleHelper.applyLanguage(it) }
        super.attachBaseContext(context)
    }

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            NavigationHelper.goBack(this)
        }

        binding.dayNightModeSwitch.isChecked = SettingsHelper.isDarkMode(this)
        binding.autoSortSwitch.isChecked = SettingsHelper.isAutoSort(this)

        binding.dayNightModeSwitch.setOnCheckedChangeListener { _, checked ->
            SettingsHelper.setDarkMode(this, checked)
        }

        binding.autoSortSwitch.setOnCheckedChangeListener { _, checked ->
            SettingsHelper.setAutoSort(this, checked)
        }

        val languageItems = listOf(
            getString(R.string.english) to "en",
            getString(R.string.persian) to "fa"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            languageItems.map { it.first }
        )

        binding.languageSelector.setAdapter(adapter)

        val currentLang = LocaleHelper.getLanguage(this)
        val currentIndex = languageItems.indexOfFirst { it.second == currentLang }
        if (currentIndex != -1) {
            binding.languageSelector.setText(languageItems[currentIndex].first, false)
        }

        fun restartApp() {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finishAffinity()
        }

        binding.languageSelector.setOnItemClickListener { _, _, position, _ ->
            val selectedLangCode = languageItems[position].second

            if (selectedLangCode != currentLang) {
                LocaleHelper.setLanguage(this, selectedLangCode)

                recreate()
                restartApp()
            }
        }
    }
}