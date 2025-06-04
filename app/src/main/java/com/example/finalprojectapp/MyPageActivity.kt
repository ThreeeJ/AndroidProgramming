package com.example.finalprojectapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.MenuItem

class MyPageActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvUsername: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private var currentUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        dbHelper = DatabaseHelper(this)
        
        // 현재 로그인된 사용자 이름 가져오기
        currentUsername = intent.getStringExtra("username") ?: ""
        android.util.Log.d("MyPageActivity", "받은 username: $currentUsername")
        
        if (currentUsername.isEmpty()) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 뷰 초기화
        tvUsername = findViewById(R.id.tvUsername)
        val userName = dbHelper.getUserName(currentUsername)
        android.util.Log.d("MyPageActivity", "DB에서 가져온 사용자 이름: $userName")
        tvUsername.text = userName

        // 하단 네비게이션 설정
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.selectedItemId = R.id.navigation_mypage

        // 버튼 클릭 리스너 설정
        setupClickListeners()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", currentUsername)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                return true
            }
            R.id.navigation_analysis -> {
                // TODO: 분석 화면으로 이동
                Toast.makeText(this, "분석 화면 준비 중", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.navigation_mypage -> {
                // 이미 마이페이지이므로 아무 작업 안함
                return true
            }
        }
        return false
    }

    private fun setupClickListeners() {
        // 회원 정보 수정 버튼
        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            showEditProfileDialog()
        }

        // 로그아웃 버튼
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmDialog()
        }

        // 회원 탈퇴 버튼
        findViewById<Button>(R.id.btnDeleteAccount).setOnClickListener {
            showDeleteAccountConfirmDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_edit_profile)

        val etNewName = dialog.findViewById<EditText>(R.id.etNewName)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val newName = etNewName.text.toString().trim()
            if (newName.isEmpty()) {
                etNewName.error = "이름을 입력해주세요"
                return@setOnClickListener
            }

            val result = dbHelper.updateUserName(currentUsername, newName)
            if (result > 0) {
                tvUsername.text = newName
                Toast.makeText(this, "회원 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "회원 정보 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("로그아웃") { _, _ ->
                // 로그인 화면으로 이동
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDeleteAccountConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("회원 탈퇴")
            .setMessage("정말로 탈퇴하시겠습니까?\n모든 데이터가 삭제됩니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                if (dbHelper.deleteUser(currentUsername)) {
                    Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    // 로그인 화면으로 이동
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
} 