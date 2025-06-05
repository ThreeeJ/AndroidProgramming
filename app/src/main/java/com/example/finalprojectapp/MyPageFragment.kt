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
import java.text.SimpleDateFormat
import java.util.*

/**
 * 마이페이지 화면을 담당하는 프래그먼트
 * 사용자 정보와 통계를 표시하고 계정 관련 기능을 제공합니다.
 */
class MyPageFragment : Fragment() {
    // 데이터베이스 헬퍼 클래스
    private lateinit var dbHelper: DatabaseHelper
    // UI 요소들
    private lateinit var tvUsername: TextView
    private lateinit var tvMonthlyExpense: TextView
    private lateinit var tvTopCategory: TextView
    private lateinit var tvTransactionCount: TextView
    // 로그인한 사용자 아이디
    private var username: String = ""

    /**
     * 프래그먼트가 생성될 때 호출되는 메서드
     * 사용자 정보를 초기화합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("username") ?: ""
        if (username.isEmpty()) {
            Toast.makeText(context, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }

    /**
     * 프래그먼트의 뷰가 생성될 때 호출되는 메서드
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mypage, container, false)
    }

    /**
     * 프래그먼트의 뷰가 생성된 후 호출되는 메서드
     * 초기화 작업을 수행합니다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())
        username = arguments?.getString("username") ?: ""

        initializeViews(view)
        updateUserStatistics()
        setupButtons()
    }

    /**
     * UI 요소들을 초기화하는 메서드
     */
    private fun initializeViews(view: View) {
        tvUsername = view.findViewById(R.id.tvUsername)
        tvMonthlyExpense = view.findViewById(R.id.tvMonthlyExpense)
        tvTopCategory = view.findViewById(R.id.tvTopCategory)
        tvTransactionCount = view.findViewById(R.id.tvTransactionCount)
        
        tvUsername.text = username
    }

    /**
     * 사용자의 통계 정보를 업데이트하는 메서드
     * - 이번 달 총 지출
     * - 가장 많이 지출한 카테고리
     * - 이번 달 지출 건수
     */
    private fun updateUserStatistics() {
        // 현재 달의 시작일과 마지막 날짜 구하기
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = dateFormat.format(calendar.time)
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = dateFormat.format(calendar.time)

        // 이번 달 총 지출
        val monthlyExpense = dbHelper.getMonthlyExpense(username, startDate, endDate)
        tvMonthlyExpense.text = String.format("%,d원", monthlyExpense.toInt())

        // 가장 많이 쓴 카테고리
        val topCategory = dbHelper.getTopCategory(username, startDate, endDate)
        tvTopCategory.text = topCategory ?: "-"

        // 이번 달 지출 건수
        val transactionCount = dbHelper.getTransactionCount(username, startDate, endDate)
        tvTransactionCount.text = "${transactionCount}건"
    }

    /**
     * 버튼들의 클릭 리스너를 설정하는 메서드
     */
    private fun setupButtons() {
        // 버튼 클릭 리스너 설정
        setupClickListeners(view!!)
    }

    /**
     * 각 버튼의 클릭 리스너를 설정하는 메서드
     * - 회원 정보 수정
     * - 로그아웃
     * - 회원 탈퇴
     */
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

    /**
     * 회원 정보 수정 다이얼로그를 표시하는 메서드
     * 사용자 이름을 변경할 수 있습니다.
     */
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

    /**
     * 로그아웃 확인 다이얼로그를 표시하는 메서드
     * 확인 시 로그인 화면으로 이동합니다.
     */
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

    /**
     * 회원 탈퇴 확인 다이얼로그를 표시하는 메서드
     * 확인 시 사용자 데이터를 삭제하고 로그인 화면으로 이동합니다.
     */
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
        /**
         * MyPageFragment의 새로운 인스턴스를 생성하는 팩토리 메서드
         */
        fun newInstance() = MyPageFragment()
    }
} 