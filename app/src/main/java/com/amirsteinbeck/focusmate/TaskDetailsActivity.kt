package com.amirsteinbeck.focusmate

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amirsteinbeck.focusmate.databinding.ActivityTaskDetailsBinding

class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val title = intent.getStringExtra("TASKTITLE")
        val description = intent.getStringExtra("TASKDESCRIPTION")
        binding.taskTitle.text = if (!title.isNullOrEmpty())
            title
        else
            getString(R.string.titlePlaceholder)

        binding.taskDescription.text = if (!description.isNullOrEmpty())
            description
        else
            getString(R.string.descriptionPlaceholder)


        binding.backButton.setOnClickListener {
            NavigationHelper.goBack(this)
        }
    }
}