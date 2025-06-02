package com.example.finalprojectapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finalprojectapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper
    private var isIdChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupButtons()
    }

    private fun setupButtons() {
        binding.buttonCheckId.setOnClickListener {
            val username = binding.editTextRegisterId.text.toString()
            if (username.isEmpty()) {
                Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.isUserExists(username)) {
                // 중복된 아이디
                binding.textViewIdCheck.apply {
                    text = "X"
                    setTextColor(ContextCompat.getColor(this@RegisterActivity, android.R.color.holo_red_dark))
                    visibility = View.VISIBLE
                }
                binding.buttonCheckId.backgroundTintList = ContextCompat.getColorStateList(
                    this,
                    android.R.color.holo_red_dark
                )
                isIdChecked = false
                Toast.makeText(this, "이미 사용중인 아이디입니다", Toast.LENGTH_SHORT).show()
            } else {
                // 사용 가능한 아이디
                binding.textViewIdCheck.apply {
                    text = "O"
                    setTextColor(ContextCompat.getColor(this@RegisterActivity, android.R.color.holo_green_dark))
                    visibility = View.VISIBLE
                }
                binding.buttonCheckId.backgroundTintList = ContextCompat.getColorStateList(
                    this,
                    android.R.color.holo_green_dark
                )
                isIdChecked = true
                Toast.makeText(this, "사용 가능한 아이디입니다", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editTextRegisterId.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                // 아이디 입력값이 변경되면 버튼 색상을 기본값으로 되돌림
                binding.buttonCheckId.backgroundTintList = ContextCompat.getColorStateList(
                    this@RegisterActivity,
                    android.R.color.darker_gray
                )
                binding.textViewIdCheck.visibility = View.GONE
                isIdChecked = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.buttonRegisterSubmit.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val username = binding.editTextRegisterId.text.toString()
            val password = binding.editTextRegisterPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            when {
                name.isEmpty() -> {
                    Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                username.isEmpty() -> {
                    Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "비밀번호 확인을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !isIdChecked -> {
                    Toast.makeText(this, "아이디 중복 확인을 해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val result = dbHelper.addUser(name, username, password)
            if (result != -1L) {
                Toast.makeText(this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "회원가입에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 