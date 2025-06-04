package com.example.finalprojectapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class MyPageFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvUsername: TextView
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("username") ?: ""
        if (username.isEmpty()) {
            Toast.makeText(context, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mypage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        
        // 뷰 초기화
        tvUsername = view.findViewById(R.id.tvUsername)
        val userName = dbHelper.getUserName(username)
        tvUsername.text = userName

        // 버튼 클릭 리스너 설정
        setupClickListeners(view)
    }

    private fun setupClickListeners(view: View) {
        // 회원 정보 수정 버튼
        view.findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            showEditProfileDialog()
        }

        // 로그아웃 버튼
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmDialog()
        }

        // 회원 탈퇴 버튼
        view.findViewById<Button>(R.id.btnDeleteAccount).setOnClickListener {
            showDeleteAccountConfirmDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialog = Dialog(requireContext())
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

            val result = dbHelper.updateUserName(username, newName)
            if (result > 0) {
                tvUsername.text = newName
                Toast.makeText(context, "회원 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "회원 정보 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("로그아웃") { _, _ ->
                // 로그인 화면으로 이동
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDeleteAccountConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("회원 탈퇴")
            .setMessage("정말로 탈퇴하시겠습니까?\n모든 데이터가 삭제됩니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                if (dbHelper.deleteUser(username)) {
                    Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    // 로그인 화면으로 이동
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    activity?.finish()
                } else {
                    Toast.makeText(context, "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    companion object {
        fun newInstance() = MyPageFragment()
    }
} 