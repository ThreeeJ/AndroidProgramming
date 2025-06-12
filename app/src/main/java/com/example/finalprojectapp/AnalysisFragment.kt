package com.example.finalprojectapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 지출 분석 화면을 담당하는 프래그먼트
 * 일별/월별/연별 지출을 카테고리별로 분석하여 파이 차트로 표시합니다.
 */
class AnalysisFragment : Fragment() {
    // 데이터베이스 헬퍼 클래스
    private lateinit var dbHelper: DB
    // 파이 차트 뷰
    private lateinit var pieChart: PieChart
    // 날짜 표시 텍스트뷰
    private lateinit var tvDate: TextView
    // 기간 선택 드롭다운 버튼
    private lateinit var btnDropdown: ImageButton
    // 로그인한 사용자 아이디
    private var username: String = ""
    // 현재 선택된 날짜의 캘린더 객체
    private val calendar: Calendar = Calendar.getInstance()
    // 현재 선택된 보기 모드 (일별/월별/연별)
    private var currentViewMode: ViewMode = ViewMode.DAILY
    // MainActivity에서 전달받은 선택된 날짜
    private var selectedDate: String? = null

    /**
     * 분석 화면의 보기 모드를 정의하는 열거형
     */
    enum class ViewMode {
        DAILY,    // 일별 보기
        MONTHLY,  // 월별 보기
        YEARLY    // 연별 보기
    }

    /**
     * 프래그먼트가 생성될 때 호출되는 메서드
     * 사용자 정보와 선택된 날짜를 초기화합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("username") ?: ""
        selectedDate = arguments?.getString("selected_date")
        
        // 선택된 날짜가 있으면 calendar에 설정
        selectedDate?.let { dateStr ->
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(dateStr)
                calendar.time = date ?: calendar.time
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
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    /**
     * 프래그먼트의 뷰가 생성된 후 호출되는 메서드
     * 초기화 작업을 수행합니다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DB(requireContext())

        initializeViews(view)
        setupPieChart()
        updateDateDisplay()
        loadExpenseData()
    }

    /**
     * UI 요소들을 초기화하는 메서드
     * 파이 차트, 날짜 표시, 드롭다운 버튼을 초기화하고
     * 드롭다운 버튼의 클릭 리스너를 설정합니다.
     */
    private fun initializeViews(view: View) {
        pieChart = view.findViewById(R.id.pieChart)
        tvDate = view.findViewById(R.id.tvDate)
        btnDropdown = view.findViewById(R.id.btnDropdown)

        btnDropdown.setOnClickListener {
            showDateFilterPopup(it)
        }
    }

    /**
     * 기간 선택 팝업을 표시하는 메서드
     * 일별/월별/연별 보기를 선택할 수 있는 팝업 메뉴를 표시합니다.
     */
    private fun showDateFilterPopup(anchorView: View) {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_date_filter, null)
        
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 현재 선택된 모드의 텍스트 색상과 스타일 변경
        val tvDaily = popupView.findViewById<TextView>(R.id.tvDaily)
        val tvMonthly = popupView.findViewById<TextView>(R.id.tvMonthly)
        val tvYearly = popupView.findViewById<TextView>(R.id.tvYearly)

        // 초기 상태 설정
        tvDaily.apply {
            setTextColor(if (currentViewMode == ViewMode.DAILY) Color.parseColor("#1150AB") else Color.parseColor("#757575"))
            typeface = if (currentViewMode == ViewMode.DAILY) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        tvMonthly.apply {
            setTextColor(if (currentViewMode == ViewMode.MONTHLY) Color.parseColor("#1150AB") else Color.parseColor("#757575"))
            typeface = if (currentViewMode == ViewMode.MONTHLY) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        tvYearly.apply {
            setTextColor(if (currentViewMode == ViewMode.YEARLY) Color.parseColor("#1150AB") else Color.parseColor("#757575"))
            typeface = if (currentViewMode == ViewMode.YEARLY) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        // 팝업 메뉴 아이템 클릭 리스너 설정
        tvDaily.setOnClickListener {
            currentViewMode = ViewMode.DAILY
            updateDateDisplay()
            loadExpenseData()
            popupWindow.dismiss()
        }

        tvMonthly.setOnClickListener {
            currentViewMode = ViewMode.MONTHLY
            updateDateDisplay()
            loadExpenseData()
            popupWindow.dismiss()
        }

        tvYearly.setOnClickListener {
            currentViewMode = ViewMode.YEARLY
            updateDateDisplay()
            loadExpenseData()
            popupWindow.dismiss()
        }

        // 팝업 윈도우 표시
        popupWindow.showAsDropDown(anchorView)
    }

    /**
     * 파이 차트의 기본 설정을 하는 메서드
     * - 차트의 모양과 스타일 설정
     * - 범례 설정
     * - 크기와 여백 설정
     */
    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 50f
            transparentCircleRadius = 55f
            setDrawCenterText(true)
            centerText = "지출 분석"
            setCenterTextSize(24f)
            setUsePercentValues(true)
            
            // NoDataText 색상 설정
            setNoDataTextColor(Color.parseColor("#1150AB"))
            
            // 범례 설정
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                textSize = 25f
                yOffset = 60f
                xOffset = 40f
                form = Legend.LegendForm.CIRCLE
                formSize = 24f
                formToTextSpace = 12f
                isWordWrapEnabled = true
                maxSizePercent = 1f
            }

            // 여백 설정
            setExtraOffsets(5f, -150f, 5f, 30f)
            minOffset = 0f

            // 차트 크기 고정
            minimumHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
            minimumWidth = resources.displayMetrics.widthPixels - 60
        }
    }

    /**
     * 현재 선택된 보기 모드에 따라 날짜를 표시하는 메서드
     * - 일별: yyyy년 M월 d일
     * - 월별: yyyy년 M월
     * - 연별: yyyy년
     */
    private fun updateDateDisplay() {
        val dateFormat = when (currentViewMode) {
            ViewMode.DAILY -> SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
            ViewMode.MONTHLY -> SimpleDateFormat("yyyy년 M월", Locale.KOREA)
            ViewMode.YEARLY -> SimpleDateFormat("yyyy년", Locale.KOREA)
        }
        tvDate.text = dateFormat.format(calendar.time)
    }

    /**
     * 선택된 기간의 지출 데이터를 로드하여 파이 차트에 표시하는 메서드
     * - 각 카테고리별 지출 금액을 계산
     * - 카테고리별 비율을 계산하여 파이 차트에 표시
     * - 카테고리별로 서로 다른 색상 적용
     */
    private fun loadExpenseData() {
        val expenseData = when (currentViewMode) {
            ViewMode.DAILY -> {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                val currentDate = dateFormat.format(calendar.time)
                dbHelper.getDailyExpenseByCategory(currentDate)
            }
            ViewMode.MONTHLY -> {
                val dateFormat = SimpleDateFormat("yyyy-MM", Locale.KOREA)
                val currentMonth = dateFormat.format(calendar.time)
                dbHelper.getMonthlyExpenseByCategory(currentMonth)
            }
            ViewMode.YEARLY -> {
                val dateFormat = SimpleDateFormat("yyyy", Locale.KOREA)
                val currentYear = dateFormat.format(calendar.time)
                dbHelper.getYearlyExpenseByCategory(currentYear)
            }
        }
        
        if (expenseData.isEmpty()) {
            val noDataMessage = when (currentViewMode) {
                ViewMode.DAILY -> "오늘의 지출 데이터가 없습니다"
                ViewMode.MONTHLY -> "이번 달의 지출 데이터가 없습니다"
                ViewMode.YEARLY -> "올해의 지출 데이터가 없습니다"
            }
            pieChart.setNoDataText(noDataMessage)
            pieChart.invalidate()
            return
        }

        // 카테고리 개수에 따른 yOffset 계산
        val categoryCount = expenseData.size
        pieChart.legend.apply {
            yOffset = when {
                categoryCount == 1 -> 70f
                categoryCount == 2 -> 53f
                categoryCount == 3 -> 36f
                categoryCount == 4 -> 19f
                else -> 2f - (17f * (categoryCount - 5))
            }
            maxSizePercent = if (categoryCount >= 5) 0.7f else 1f
        }

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        
        // 새로운 색상 배열 정의
        val colorArray = intArrayOf(
            resources.getColor(R.color.chart_color_1, null),  // 인디고
            resources.getColor(R.color.chart_color_2, null),  // 틸
            resources.getColor(R.color.chart_color_3, null),  // 오렌지
            resources.getColor(R.color.chart_color_4, null),  // 레드
            resources.getColor(R.color.chart_color_5, null),  // 그린
            resources.getColor(R.color.chart_color_6, null),  // 브라운
            resources.getColor(R.color.chart_color_7, null),  // 딥 퍼플
            resources.getColor(R.color.chart_color_8, null)   // 라이트 블루
        )

        var totalAmount = 0f
        expenseData.values.forEach { amount -> totalAmount += amount }

        expenseData.entries.forEachIndexed { index, (category, amount) ->
            entries.add(PieEntry((amount / totalAmount) * 100f, category))
            colors.add(colorArray[index % colorArray.size])
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            setDrawValues(true)
            valueTextSize = 25f
            valueTextColor = Color.BLACK
            valueFormatter = PercentFormatter().apply {
                mFormat = java.text.DecimalFormat("##.0")
            }
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            setDrawIcons(false)
            sliceSpace = 3f
        }

        pieChart.apply {
            data = PieData(dataSet).apply {
                setValueTextSize(25f)
                setDrawValues(true)
                setValueFormatter(PercentFormatter().apply {
                    mFormat = java.text.DecimalFormat("##.0")
                })
            }
            setEntryLabelColor(Color.TRANSPARENT)
            setEntryLabelTextSize(0f)
            setHoleColor(resources.getColor(R.color.chart_background, null))
            setCenterTextColor(Color.BLACK)
            legend.textColor = resources.getColor(R.color.chart_text, null)
            invalidate()
        }
    }

    companion object {
        /**
         * AnalysisFragment의 새로운 인스턴스를 생성하는 팩토리 메서드
         * @param username 사용자 아이디
         * @param selectedDate 선택된 날짜 (nullable)
         */
        fun newInstance(username: String, selectedDate: String? = null): AnalysisFragment {
            return AnalysisFragment().apply {
                arguments = Bundle().apply {
                    putString("username", username)
                    selectedDate?.let { putString("selected_date", it) }
                }
            }
        }
    }
} 