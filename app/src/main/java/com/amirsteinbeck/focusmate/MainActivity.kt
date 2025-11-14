package com.amirsteinbeck.focusmate

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amirsteinbeck.focusmate.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
// Needed Before Using Binding
//        val button = findViewById<Button>(R.id.textChangerButton)
//        val textView = findViewById<TextView>(R.id.textView)
//        val nameField = findViewById<EditText>(R.id.nameInput)
//        val resetBtn = findViewById<Button>(R.id.resetButton)

// My Written code, I keep it to compare later for better learning:
//        button.setOnClickListener {
//            val goodbyeToUSerText = getString(R.string.goodbyeToUser, nameField.text)
//            val helloToUSerText = getString(R.string.helloToUser, nameField.text)
//
//            if (nameField.text.isNotEmpty()) {
//                if (textView.text.toString() == getString(R.string.helloMessage)||
//                    textView.text.toString() == getString(R.string.goodbyeMessage) ||
//                    textView.text.toString() == goodbyeToUSerText) {
//                    textView.text = helloToUSerText
//                } else {
//                    textView.text = goodbyeToUSerText
//                }
//            } else {
//                if (textView.text == getString(R.string.helloMessage) ||
//                    textView.text == getString(R.string.helloToUser)) {
//                    textView.text = getString(R.string.goodbyeMessage)
//                } else {
//                    textView.text = getString(R.string.helloMessage)
//                }
//            }
//
//        }

// My version of helper functions:
//        fun sayingHello(text: String, toastMessage: String) {
//            binding.textView.text = text
//            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
//        }
//        fun sayingGoodbye(text: String, toastMessage: String) {
//            binding.textView.text = text
//            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
//        }

// ChatGPT's suggestion of a better and more efficient helper function:
    fun updateTextAndToast(newText: String, toastMessage: String) {
        binding.textView.text = newText
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
    }

// ChatGPT Correction with clearer logic and more efficiency:
        var isHello = true
        binding.textChangerButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            if ((name.length <= 3 && name.isNotEmpty()) || name.length > 30) {
                binding.nameInputLayout.error = "Please enter a valid name or leave the field blank!"
                return@setOnClickListener
            } else {
                if (binding.nameInputLayout.error != null) binding.nameInputLayout.error = null

                val helloToast = getString(R.string.btnGreetingClick)
                val goodbyeToast = getString(R.string.btnByeingClick)
                val helloText = if (name.isNotEmpty()) getString(R.string.helloToUser, name)
                else getString(R.string.helloMessage)
                val goodbyeText = if (name.isNotEmpty()) getString(R.string.goodbyeToUser, name)
                else getString(R.string.goodbyeMessage)

                if (isHello)
                    updateTextAndToast(goodbyeText, goodbyeToast)
                else
                    updateTextAndToast(helloText, helloToast)

                isHello = !isHello
            }
        }

        binding.resetButton.setOnClickListener {
            binding.textView.text = getString(R.string.helloMessage)
            binding.nameInput.setText("")
        }

    }
}