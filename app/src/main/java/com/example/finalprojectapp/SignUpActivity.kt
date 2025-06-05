package com.example.finalprojectapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finalprojectapp.databinding.ActivitySignupBinding

/**
 * 회원가입 화면을 담당하는 액티비티
 * 사용자로부터 이름, 아이디, 비밀번호를 입력받아 새로운 계정을 생성합니다.
 */
class SignUpActivity : AppCompatActivity() {
    // 뷰 바인딩 객체
    private lateinit var binding: ActivitySignupBinding
    // 데이터베이스 헬퍼 클래스
    private lateinit var dbHelper: DatabaseHelper
    // 아이디 중복 확인 여부를 저장하는 변수
    private var isIdChecked = false

    /**
     * 액티비티가 생성될 때 호출되는 메서드
     * 초기화 작업을 수행합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupIdCheck()
        setupPasswordConfirmation()
        setupRegisterButton()
    }

    /**
     * 아이디 입력 필드의 변경을 감지하고 중복 확인을 수행하는 메서드
     */
    private fun setupIdCheck() {
        binding.editTextSignupId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val username = s.toString().trim()
                if (username.isEmpty()) {
                    binding.editTextSignupId.setBackgroundResource(R.drawable.edit_text_border_1150ab)
                    binding.textViewIdCheck.visibility = View.INVISIBLE
                    isIdChecked = false
                    return
                }

                if (dbHelper.isUserExists(username)) {
                    // 중복된 아이디
                    binding.editTextSignupId.setBackgroundResource(R.drawable.edit_text_background_error)
                    binding.textViewIdCheck.apply {
                        text = "중복된 아이디입니다"
                        setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                        visibility = View.VISIBLE
                    }
                    isIdChecked = false
                } else {
                    // 사용 가능한 아이디
                    binding.editTextSignupId.setBackgroundResource(R.drawable.edit_text_background_success)
                    binding.textViewIdCheck.apply {
                        text = "사용 가능한 아이디입니다"
                        setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                        visibility = View.VISIBLE
                    }
                    isIdChecked = true
                }
            }
        })
    }

    /**
     * 비밀번호 확인 기능을 설정하는 메서드
     * 비밀번호와 비밀번호 확인 필드의 텍스트 변경을 감지합니다.
     */
    private fun setupPasswordConfirmation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkPasswordMatch()
            }
        }

        binding.editTextSignupPassword.addTextChangedListener(textWatcher)
        binding.editTextConfirmPassword.addTextChangedListener(textWatcher)
    }

    /**
     * 비밀번호 일치 여부를 확인하는 메서드
     * 비밀번호가 일치하면 초록색, 불일치하면 빨간색으로 표시합니다.
     */
    private fun checkPasswordMatch() {
        val password = binding.editTextSignupPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()

        // 비밀번호 확인 필드가 비어있고, 비밀번호 필드도 비어있으면 기본 상태 유지
        if (confirmPassword.isEmpty() && password.isEmpty()) {
            binding.editTextConfirmPassword.setBackgroundResource(R.drawable.edit_text_border_1150ab)
            binding.tvPasswordMatch.visibility = View.INVISIBLE
            return
        }

        // 비밀번호 확인 필드가 비어있으면 검사하지 않음
        if (confirmPassword.isEmpty()) {
            binding.editTextConfirmPassword.setBackgroundResource(R.drawable.edit_text_border_1150ab)
            binding.tvPasswordMatch.visibility = View.INVISIBLE
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

    /**
     * 회원가입 버튼 클릭 리스너 설정
     */
    private fun setupRegisterButton() {
        binding.buttonSignupSubmit.setOnClickListener {
            registerUser()
        }
    }

    /**
     * 회원가입을 처리하는 메서드
     * 입력된 정보의 유효성을 검사하고 데이터베이스에 저장합니다.
     * 성공 시 로그인 화면으로 이동합니다.
     */
    private fun registerUser() {
        val name = binding.editTextName.text.toString().trim()
        val username = binding.editTextSignupId.text.toString().trim()
        val password = binding.editTextSignupPassword.text.toString()
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