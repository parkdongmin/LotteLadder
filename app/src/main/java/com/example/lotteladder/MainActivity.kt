package com.example.lotteladder

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.lotteladder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener {
            val userInput = binding.participantEditText.text.toString()
            if (userInput.isNotEmpty() && userInput.toInt() >= 1) {
                val participants = binding.participantEditText.text.toString().toInt()
                binding.ladderView.setParameters(participants)

                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            } else {
                if (userInput.isEmpty()) {
                    Toast.makeText(this, "인원 수 입력 해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "1 이상의 값을 입력 해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.previewButton.setOnClickListener {
            val boomNumber = binding.ladderView.preview()
            Toast.makeText(applicationContext,"랜덤 당첨 번호는 : ${boomNumber + 1} 입니다.",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

}