package com.example.finalprojectapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectapp.databinding.ActivityLoginBinding

/**
 * 로그인 화면을 담당하는 액티비티
 * 사용자 인증을 처리하고 메인 화면으로 이동합니다.
 */
class LoginActivity : AppCompatActivity() {
    // 뷰 바인딩 객체
    private lateinit var binding: ActivityLoginBinding
    // 데이터베이스 헬퍼 클래스
    private lateinit var dbHelper: DatabaseHelper
    // 아이디 입력 필드
    private lateinit var etUsername: EditText
    // 비밀번호 입력 필드
    private lateinit var etPassword: EditText

    /**
     * 액티비티가 생성될 때 호출되는 메서드
     * 초기화 작업을 수행합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        initializeViews()
        setupTextWatchers()
        setupClickListeners()
    }

    /**
     * UI 요소들을 초기화하는 메서드
     * 입력 필드와 삭제 버튼을 설정합니다.
     */
    private fun initializeViews() {
        etUsername = binding.editTextId
        etPassword = binding.editTextPassword

        // X 버튼으로 입력 텍스트 삭제
        binding.buttonClearId.setOnClickListener {
            etUsername.text.clear()
        }
        binding.buttonClearPassword.setOnClickListener {
            etPassword.text.clear()
        }
    }

    /**
     * 입력 필드의 텍스트 변경을 감지하는 리스너를 설정하는 메서드
     * 텍스트가 있을 때만 삭제 버튼을 표시합니다.
     */
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

    /**
     * 버튼들의 클릭 리스너를 설정하는 메서드
     * - 로그인 버튼: 사용자 인증 후 메인 화면으로 이동
     * - 회원가입 버튼: 회원가입 화면으로 이동
     */
    private fun setupClickListeners() {
        // 로그인 버튼
        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextId.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.validateUser(username, password)) {
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "아이디 또는 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 버튼
        binding.buttonSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
} 