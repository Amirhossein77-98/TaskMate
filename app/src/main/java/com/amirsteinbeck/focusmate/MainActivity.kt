package com.amirsteinbeck.focusmate

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amirsteinbeck.focusmate.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        lateinit var adapter: TaskAdapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, imeInsets.bottom)
            insets
        }


        val items = mutableListOf(
            Task("Buy Groceries", "Buy groceries from grocery store"),
            Task("Fix computer", "Computer does not boot up properly"),
            Task("Send drafts", "Send drafts of the new project to boss"),
            Task("Watch movie", "Watch a new episode of Mr. Robot"),
            Task("Call Mom", "Call my mom")
        )

        adapter = TaskAdapter(items) { clickedTask, position ->
            Snackbar.make(binding.root, "Chose: ${clickedTask.title}", Snackbar.LENGTH_SHORT).show()
            adapter.removeItem(position)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
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
//    fun updateTextAndToast(newText: String, toastMessage: String) {
////        binding.textView.text = newText
//        Snackbar.make(binding.root, toastMessage, Snackbar.LENGTH_SHORT).show()
//    }

// ChatGPT Correction with clearer logic and more efficiency:
//        var isHello = true
        binding.submitButton.setOnClickListener {
            val title = binding.userInput.text.toString().trim()
            if ((title.length <= 3 && title.isNotEmpty()) || title.length > 30) {
                binding.nameInputLayout.error = "Please enter a valid name or leave the field blank!"
                return@setOnClickListener
            } else {
//                if (binding.nameInputLayout.error != null) binding.nameInputLayout.error = null
//
//                val helloToast = getString(R.string.btnGreetingClick)
//                val goodbyeToast = getString(R.string.btnByeingClick)
//                val helloText = if (name.isNotEmpty()) getString(R.string.helloToUser, name)
//                else getString(R.string.helloMessage)
//                val goodbyeText = if (name.isNotEmpty()) getString(R.string.goodbyeToUser, name)
//                else getString(R.string.goodbyeMessage)
//
//                if (isHello)
//                    updateTextAndToast(goodbyeText, goodbyeToast)
//                else
//                    updateTextAndToast(helloText, helloToast)
//
//                isHello = !isHello

                val newTask = Task(title, "New Task")
                adapter.addItem(newTask)

                binding.recyclerView.scrollToPosition(items.size - 1)
                binding.userInput.setText("")
            }
        }

        binding.resetButton.setOnClickListener {
//            binding.textView.text = getString(R.string.helloMessage)
            binding.userInput.setText("")
            binding.nameInputLayout.error = null
        }

        binding.rightPageButtonNavigator.setOnClickListener {
            val name = binding.userInput.text.toString().trim()
            NavigationHelper.goToGreet(this, name)
        }

        binding.leftPageButtonNavigator.setOnClickListener {
            NavigationHelper.goToCredentials(this)
        }

        AnimationHelper.applyPressAnimation(this, binding.submitButton)
        AnimationHelper.applyPressAnimation(this, binding.resetButton)
        AnimationHelper.applyPressAnimation(this, binding.rightPageButtonNavigator)
        AnimationHelper.applyPressAnimation(this, binding.leftPageButtonNavigator)
    }
}