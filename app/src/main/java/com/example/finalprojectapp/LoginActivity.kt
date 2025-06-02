package com.example.finalprojectapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupButtons()
        setupTextWatchers()
    }

    private fun setupButtons() {
        binding.buttonClearId.setOnClickListener {
            binding.editTextId.text.clear()
        }

        binding.buttonClearPassword.setOnClickListener {
            binding.editTextPassword.text.clear()
        }

        binding.buttonLogin.setOnClickListener {
            val id = binding.editTextId.text.toString()
            val password = binding.editTextPassword.text.toString()
            
            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (dbHelper.validateUser(id, password)) {
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                // TODO: 메인 화면으로 이동
            } else {
                Toast.makeText(this, "아이디 또는 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupTextWatchers() {
        binding.editTextId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.buttonClearId.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.buttonClearPassword.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
} 