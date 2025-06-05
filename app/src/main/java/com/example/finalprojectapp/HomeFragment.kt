package com.example.finalprojectapp

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 가계부 앱의 메인 화면을 담당하는 프래그먼트
 * 일별/월별/연별 수입/지출 내역을 표시하고
 * 거래 내역의 추가/수정/삭제를 관리합니다.
 */
class HomeFragment : Fragment() {
    // 데이터베이스 헬퍼 클래스
    private lateinit var dbHelper: DatabaseHelper
    // 거래 내역 목록을 표시하는 어댑터
    private lateinit var transactionAdapter: TransactionAdapter
    
    // UI 요소들
    private lateinit var tvDate: TextView  // 선택된 날짜 표시
    private lateinit var tvYearlyIncome: TextView  // 연간 수입 합계
    private lateinit var tvYearlyExpense: TextView  // 연간 지출 합계
    private lateinit var tvMonthlyIncome: TextView  // 월간 수입 합계
    private lateinit var tvMonthlyExpense: TextView  // 월간 지출 합계
    private lateinit var tvDailyIncome: TextView  // 일별 수입 합계
    private lateinit var tvDailyExpense: TextView  // 일별 지출 합계
    private lateinit var rvTransactions: RecyclerView  // 거래 내역 목록
    
    // 현재 선택된 날짜
    private var selectedDate: Calendar = Calendar.getInstance()
    // 로그인한 사용자 아이디
    private var username: String = ""

    /**
     * 프래그먼트가 생성될 때 호출되는 메서드
     * 사용자 정보와 선택된 날짜를 초기화합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("username") ?: ""
        if (username.isEmpty()) {
            Toast.makeText(context, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }

        // MainActivity에서 전달받은 선택된 날짜가 있으면 설정
        arguments?.getString("selected_date")?.let { dateStr ->
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(dateStr)
                selectedDate.time = date
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * 프래그먼트의 뷰가 생성된 후 호출되는 메서드
     * 초기화 작업을 수행합니다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            dbHelper = DatabaseHelper(requireContext())
            
            // 뷰 초기화
            initializeViews(view)
            
            // 설정
            setupRecyclerView()
            setupClickListeners()
            updateDateDisplay()
            updateSummary()
            loadTransactions()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "초기화 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * UI 요소들을 초기화하는 메서드
     * 모든 TextView와 RecyclerView의 참조를 가져옵니다.
     */
    private fun initializeViews(view: View) {
        tvDate = view.findViewById(R.id.tvDate)
        tvYearlyIncome = view.findViewById(R.id.tvYearlyIncome)
        tvYearlyExpense = view.findViewById(R.id.tvYearlyExpense)
        tvMonthlyIncome = view.findViewById(R.id.tvMonthlyIncome)
        tvMonthlyExpense = view.findViewById(R.id.tvMonthlyExpense)
        tvDailyIncome = view.findViewById(R.id.tvDailyIncome)
        tvDailyExpense = view.findViewById(R.id.tvDailyExpense)
        rvTransactions = view.findViewById(R.id.rvTransactions)
    }

    /**
     * RecyclerView 초기화 및 설정
     * 거래 내역을 표시하는 어댑터를 설정합니다.
     */
    private fun setupRecyclerView() {
        val adapter = TransactionAdapter(
            emptyList(),
            { transaction ->
                showTransactionDialog(transaction)
            },
            false  // 메인 화면에서는 연간 보기가 아님
        )
        rvTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        transactionAdapter = adapter
    }

    /**
     * 모든 클릭 리스너를 설정하는 메서드
     * - 달력 버튼: 날짜 선택
     * - 수입/지출 추가 버튼: 거래 내역 추가
     * - 카테고리 관리 버튼: 카테고리 관리 화면으로 이동
     * - 더보기 버튼: 전체 거래 내역 표시
     * - 연간/월간 수입/지출 텍스트: 상세 내역 표시
     */
    private fun setupClickListeners() {
        view?.findViewById<View>(R.id.btnCalendar)?.setOnClickListener {
            showDatePicker()
        }

        view?.findViewById<View>(R.id.btnAddIncome)?.setOnClickListener {
            showTransactionDialog(type = "income")
        }

        view?.findViewById<View>(R.id.btnAddExpense)?.setOnClickListener {
            showTransactionDialog(type = "expense")
        }

        view?.findViewById<View>(R.id.btnManageCategories)?.setOnClickListener {
            startActivity(Intent(context, CategoryManagementActivity::class.java))
        }

        view?.findViewById<View>(R.id.btnViewMore)?.setOnClickListener {
            showAllTransactionsDialog()
        }

        // 연간 수입/지출 클릭 이벤트
        tvYearlyIncome.setOnClickListener {
            val year = SimpleDateFormat("yyyy", Locale.KOREA).format(selectedDate.time)
            showTransactionListDialog("연간 수입 내역", year, "income", true)
        }

        tvYearlyExpense.setOnClickListener {
            val year = SimpleDateFormat("yyyy", Locale.KOREA).format(selectedDate.time)
            showTransactionListDialog("연간 지출 내역", year, "expense", true)
        }

        // 월간 수입/지출 클릭 이벤트
        tvMonthlyIncome.setOnClickListener {
            val yearMonth = SimpleDateFormat("yyyy-MM", Locale.KOREA).format(selectedDate.time)
            showTransactionListDialog("월간 수입 내역", yearMonth, "income", false)
        }

        tvMonthlyExpense.setOnClickListener {
            val yearMonth = SimpleDateFormat("yyyy-MM", Locale.KOREA).format(selectedDate.time)
            showTransactionListDialog("월간 지출 내역", yearMonth, "expense", false)
        }
    }

    /**
     * 거래 내역 추가/수정 다이얼로그를 표시하는 메서드
     * @param transaction 수정할 거래 내역 (null인 경우 새로운 거래 추가)
     * @param type 거래 유형 ("income" 또는 "expense")
     */
    private fun showTransactionDialog(transaction: Transaction? = null, type: String? = null) {
        try {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_transaction_editor)

            // 다이얼로그 크기 설정
            val window = dialog.window
            window?.setLayout(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )

            // 뷰 참조
            val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
            val spinnerCategory = dialog.findViewById<Spinner>(R.id.spinnerCategory)
            val etAmount = dialog.findViewById<EditText>(R.id.etAmount)
            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
            val btnSave = dialog.findViewById<Button>(R.id.btnSave)
            val btnDelete = dialog.findViewById<Button>(R.id.btnDelete)

            if (tvTitle == null || spinnerCategory == null || etAmount == null || btnCancel == null || btnSave == null || btnDelete == null) {
                Toast.makeText(context, "다이얼로그 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return
            }

            // 삭제 버튼 표시 여부
            btnDelete.visibility = if (transaction != null) View.VISIBLE else View.GONE

            // 제목 설정
            val transactionType = transaction?.type ?: type ?: "income"
            tvTitle.text = when {
                transaction != null -> if (transactionType == "income") "수입 수정" else "지출 수정"
                else -> if (transactionType == "income") "수입 추가" else "지출 추가"
            }
            
            // 제목 색상 설정
            tvTitle.setTextColor(
                if (transactionType == "income") 
                    android.graphics.Color.parseColor("#4CAF50")  // 수입은 초록색
                else 
                    android.graphics.Color.parseColor("#FF5252")  // 지출은 빨간색
            )

            try {
                // 카테고리 스피너 설정
                val categories = dbHelper.getCategories(transactionType)
                if (categories.isEmpty()) {
                    Toast.makeText(context, "카테고리를 먼저 추가해주세요.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return
                }

                val categoryAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categories.map { it.name }
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerCategory.adapter = categoryAdapter

                // 기존 데이터 설정
                if (transaction != null) {
                    val categoryIndex = categories.indexOfFirst { it.id == transaction.categoryId }
                    if (categoryIndex != -1) {
                        spinnerCategory.setSelection(categoryIndex)
                    }
                    etAmount.setText(transaction.amount.toString())
                }

                // 버튼 클릭 리스너 설정
                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                btnDelete.setOnClickListener {
                    if (transaction != null) {
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("거래 내역 삭제")
                            .setMessage("이 거래 내역을 삭제하시겠습니까?")
                            .setPositiveButton("삭제") { _, _ ->
                                val result = dbHelper.deleteTransaction(transaction.id)
                                if (result > 0) {
                                    Toast.makeText(context, "거래 내역이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                    // 화면 갱신
                                    loadTransactions()
                                    updateSummary()
                                } else {
                                    Toast.makeText(context, "거래 내역 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton("취소", null)
                            .show()
                    }
                }

                btnSave.setOnClickListener {
                    try {
                        val amount = etAmount.text.toString().toIntOrNull()
                        if (amount == null || amount <= 0) {
                            etAmount.error = "올바른 금액을 입력해주세요"
                            return@setOnClickListener
                        }

                        val selectedCategory = categories[spinnerCategory.selectedItemPosition]
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.KOREA)
                        timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                        
                        val currentDate = dateFormat.format(selectedDate.time)
                        val currentTime = timeFormat.format(Date())

                        if (transaction != null) {
                            // 수정
                            dbHelper.updateTransaction(
                                transaction.id,
                                amount,
                                selectedCategory.id
                            )
                        } else {
                            // 추가
                            dbHelper.addTransaction(
                                amount,
                                transactionType,
                                selectedCategory.id,
                                currentDate,
                                currentTime
                            )
                        }

                        loadTransactions()
                        dialog.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "저장 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "카테고리 로딩 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "다이얼로그를 표시할 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 날짜 선택 다이얼로그를 표시하는 메서드
     * 날짜가 선택되면 화면을 업데이트하고 MainActivity에 알립니다.
     */
    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateDisplay()
                loadTransactions()
                // MainActivity에 선택된 날짜 전달
                (activity as? MainActivity)?.updateSelectedDate(getSelectedDate())
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /**
     * 선택된 날짜를 화면에 표시하는 메서드
     */
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("yyyy년 M월 d일 (E)", Locale.KOREA)
        tvDate.text = dateFormat.format(selectedDate.time)
    }

    /**
     * 선택된 날짜의 거래 내역을 데이터베이스에서 불러와 표시하는 메서드
     */
    private fun loadTransactions() {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val currentDate = dateFormat.format(selectedDate.time)
            
            // 최근 거래 내역 표시
            val transactions = dbHelper.getRecentTransactions(currentDate)
            transactionAdapter.updateTransactions(transactions)
            
            // 거래 내역이 없을 경우 메시지 표시
            if (transactions.isEmpty()) {
                Toast.makeText(context, "오늘의 거래 내역이 없습니다.", Toast.LENGTH_SHORT).show()
            }

            // 합계 업데이트 (거래 내역이 없어도 항상 실행)
            updateSummary()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "거래 내역을 불러오는 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 일별/월별/연별 수입/지출 합계를 계산하여 화면에 표시하는 메서드
     */
    private fun updateSummary() {
        try {
            val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
            val calendar = selectedDate.clone() as Calendar
            
            // 연간 합계 계산
            val year = SimpleDateFormat("yyyy", Locale.KOREA).format(calendar.time)
            val (yearlyIncome, yearlyExpense) = dbHelper.getYearlySummary(year)
            tvYearlyIncome.text = "₩ ${numberFormat.format(yearlyIncome)}"
            tvYearlyExpense.text = "₩ ${numberFormat.format(yearlyExpense)}"
            
            // 월간 합계 계산
            val yearMonth = SimpleDateFormat("yyyy-MM", Locale.KOREA).format(calendar.time)
            val (monthlyIncome, monthlyExpense) = dbHelper.getMonthlySummary(yearMonth)
            tvMonthlyIncome.text = "₩ ${numberFormat.format(monthlyIncome)}"
            tvMonthlyExpense.text = "₩ ${numberFormat.format(monthlyExpense)}"

            // 일별 합계 계산
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(calendar.time)
            val (dailyIncome, dailyExpense) = dbHelper.getDailySummary(date)
            tvDailyIncome.text = "₩ ${numberFormat.format(dailyIncome)}"
            tvDailyExpense.text = "₩ ${numberFormat.format(dailyExpense)}"

            // 로그 추가
            android.util.Log.d("HomeFragment", "일별 합계 - 수입: $dailyIncome, 지출: $dailyExpense")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "금액 표시 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 전체 거래 내역을 보여주는 다이얼로그를 표시하는 메서드
     * 선택된 날짜의 모든 거래 내역을 표시합니다.
     */
    private fun showAllTransactionsDialog() {
        try {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_transactions_list)
            
            // 다이얼로그 크기 설정
            val window = dialog.window
            window?.setLayout(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )

            // 뷰 초기화
            val rvAllTransactions = dialog.findViewById<RecyclerView>(R.id.rvAllTransactions)
            val tvDialogDate = dialog.findViewById<TextView>(R.id.tvDialogDate)
            val btnClose = dialog.findViewById<Button>(R.id.btnClose)

            // 날짜 표시
            val dateFormat = SimpleDateFormat("yyyy년 M월 d일 (E)", Locale.KOREA)
            tvDialogDate.text = dateFormat.format(selectedDate.time)

            // RecyclerView 설정
            val allTransactionsAdapter = TransactionAdapter(
                emptyList(),
                { transaction ->
                    showTransactionDialog(transaction)
                    dialog.dismiss()
                },
                false  // 전체 보기에서는 연간 보기가 아님
            )
            
            rvAllTransactions.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = allTransactionsAdapter
            }

            // 전체 거래 내역 로드
            val dateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val currentDate = dateFormat2.format(selectedDate.time)
            val allTransactions = dbHelper.getAllTransactions(currentDate)
            allTransactionsAdapter.updateTransactions(allTransactions)

            // 거래 내역이 없을 경우 메시지 표시
            if (allTransactions.isEmpty()) {
                Toast.makeText(context, "해당 날짜의 거래 내역이 없습니다.", Toast.LENGTH_SHORT).show()
            }

            // 닫기 버튼
            btnClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "전체 거래 내역을 표시할 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 연간/월간 거래 내역을 보여주는 다이얼로그를 표시하는 메서드
     * @param title 다이얼로그 제목
     * @param date 조회할 날짜 (yyyy 또는 yyyy-MM 형식)
     * @param type 거래 유형 ("income" 또는 "expense")
     * @param isYearly true인 경우 연간 내역, false인 경우 월간 내역
     */
    private fun showTransactionListDialog(title: String, date: String, type: String, isYearly: Boolean) {
        try {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_transactions_list)
            
            // 다이얼로그 크기 설정
            val window = dialog.window
            window?.setLayout(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )

            // 뷰 초기화
            val tvDialogTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
            val tvDialogDate = dialog.findViewById<TextView>(R.id.tvDialogDate)
            val rvAllTransactions = dialog.findViewById<RecyclerView>(R.id.rvAllTransactions)
            val btnClose = dialog.findViewById<Button>(R.id.btnClose)

            // 제목 설정 및 색상 설정
            tvDialogTitle.text = title
            tvDialogTitle.setTextColor(
                if (type == "income") 
                    android.graphics.Color.parseColor("#4CAF50")  // 수입은 초록색
                else 
                    android.graphics.Color.parseColor("#FF5252")  // 지출은 빨간색
            )

            // 날짜 표시 설정
            val displayDate = if (isYearly) {
                "${date}년"
            } else {
                val yearMonth = date.split("-")
                "${yearMonth[0]}년 ${yearMonth[1]}월"
            }
            tvDialogDate.text = displayDate

            // RecyclerView 설정
            val adapter = TransactionAdapter(
                emptyList(),
                { transaction ->
                    showTransactionDialog(transaction)
                    dialog.dismiss()
                },
                true  // 연간/월간 보기 모두 날짜 표시
            )
            
            rvAllTransactions.apply {
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
            }

            // 거래 내역 로드
            val transactions = if (isYearly) {
                dbHelper.getYearlyTransactions(date, type)
            } else {
                dbHelper.getMonthlyTransactions(date, type)
            }
            adapter.updateTransactions(transactions)

            // 닫기 버튼
            btnClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "거래 내역을 표시할 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 현재 선택된 날짜를 "yyyy-MM-dd" 형식의 문자열로 반환하는 메서드
     */
    fun getSelectedDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        return dateFormat.format(selectedDate.time)
    }

    companion object {
        /**
         * HomeFragment의 새로운 인스턴스를 생성하는 팩토리 메서드
         */
        fun newInstance() = HomeFragment()
    }
} 