package com.example.finalprojectapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
        setupPasswordConfirmation()
    }

    private fun setupButtons() {
        // 아이디 중복 확인 버튼
        binding.buttonCheckId.setOnClickListener {
            val username = binding.editTextRegisterId.text.toString()
            when {
                username.isEmpty() -> {
                    Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                dbHelper.isUserExists(username) -> {
                    binding.textViewIdCheck.apply {
                        text = "X"
                        setTextColor(ContextCompat.getColor(this@RegisterActivity, android.R.color.holo_red_dark))
                        visibility = View.VISIBLE
                    }
                    binding.buttonCheckId.backgroundTintList = ContextCompat.getColorStateList(
                        this,
                        android.R.color.holo_red_light
                    )
                    isIdChecked = false
                    Toast.makeText(this, "이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.textViewIdCheck.apply {
                        text = "O"
                        setTextColor(ContextCompat.getColor(this@RegisterActivity, android.R.color.holo_green_dark))
                        visibility = View.VISIBLE
                    }
                    binding.buttonCheckId.backgroundTintList = ContextCompat.getColorStateList(
                        this,
                        android.R.color.holo_green_light
                    )
                    isIdChecked = true
                    Toast.makeText(this, "사용 가능한 아이디입니다", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 아이디 입력 필드 변경 감지
        binding.editTextRegisterId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.buttonCheckId.backgroundTintList = ContextCompat.getColorStateList(
                    this@RegisterActivity,
                    android.R.color.darker_gray
                )
                binding.textViewIdCheck.visibility = View.GONE
                isIdChecked = false
            }
        })

        // 회원가입 버튼
        binding.buttonRegisterSubmit.setOnClickListener {
            registerUser()
        }
    }

    private fun setupPasswordConfirmation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch()
            }
        }

        binding.editTextRegisterPassword.addTextChangedListener(textWatcher)
        binding.editTextConfirmPassword.addTextChangedListener(textWatcher)
    }

    private fun checkPasswordMatch() {
        val password = binding.editTextRegisterPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()

        // 비밀번호 확인 필드가 비어있고, 비밀번호 필드도 비어있으면 기본 상태 유지
        if (confirmPassword.isEmpty() && password.isEmpty()) {
            binding.editTextConfirmPassword.setBackgroundResource(R.drawable.edit_text_background_normal)
            binding.tvPasswordMatch.visibility = View.GONE
            return
        }

        // 비밀번호 확인 필드가 비어있으면 검사하지 않음
        if (confirmPassword.isEmpty()) {
            binding.editTextConfirmPassword.setBackgroundResource(R.drawable.edit_text_background_normal)
            binding.tvPasswordMatch.visibility = View.GONE
            return
        }

        // 비밀번호 확인 필드에 입력이 있을 때만 검사
        if (password == confirmPassword) {
            // 비밀번호 일치
            binding.editTextConfirmPassword.setBackgroundResource(R.drawable.edit_text_background_success)
            binding.tvPasswordMatch.apply {
                text = "비밀번호가 일치합니다"
                setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                visibility = View.VISIBLE
            }
        } else {
            // 비밀번호 불일치
            binding.editTextConfirmPassword.setBackgroundResource(R.drawable.edit_text_background_error)
            binding.tvPasswordMatch.apply {
                text = "비밀번호가 일치하지 않습니다"
                setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                visibility = View.VISIBLE
            }
        }
    }

    private fun registerUser() {
        val name = binding.editTextName.text.toString().trim()
        val username = binding.editTextRegisterId.text.toString().trim()
        val password = binding.editTextRegisterPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()

        when {
            name.isEmpty() -> {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                return
            }
            username.isEmpty() -> {
                Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                return
            }
            password.isEmpty() -> {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return
            }
            confirmPassword.isEmpty() -> {
                Toast.makeText(this, "비밀번호 확인을 입력해주세요", Toast.LENGTH_SHORT).show()
                return
            }
            !isIdChecked -> {
                Toast.makeText(this, "아이디 중복 확인을 해주세요", Toast.LENGTH_SHORT).show()
                return
            }
            password != confirmPassword -> {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val result = dbHelper.addUser(name, username, password)
        if (result != -1L) {
            Toast.makeText(this, "회원가입이 완료되었습니다", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "회원가입에 실패했습니다", Toast.LENGTH_SHORT).show()
        }
    }
} 