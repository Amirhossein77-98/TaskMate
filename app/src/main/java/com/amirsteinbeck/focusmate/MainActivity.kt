package com.amirsteinbeck.focusmate

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val button = findViewById<Button>(R.id.textChangerButton)
        val textView = findViewById<TextView>(R.id.textView)
        val nameField = findViewById<EditText>(R.id.nameInput)

        button.setOnClickListener {
            val goodbyeToUSerText = getString(R.string.goodbyeToUser, nameField.text)
            val helloToUSerText = getString(R.string.helloToUser, nameField.text)

            if (nameField.text.isNotEmpty()) {
                if (textView.text.toString() == getString(R.string.helloMessage)||
                    textView.text.toString() == getString(R.string.goodbyeMessage) ||
                    textView.text.toString() == goodbyeToUSerText) {
                    textView.text = helloToUSerText
                } else {
                    textView.text = goodbyeToUSerText
                }
            } else {
                if (textView.text == getString(R.string.helloMessage) ||
                    textView.text == getString(R.string.helloToUser)) {
                    textView.text = getString(R.string.goodbyeMessage)
                } else {
                    textView.text = getString(R.string.helloMessage)
                }
            }

        }
    }
}