package com.example.finalprojectapp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
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

class AnalysisFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var pieChart: PieChart
    private lateinit var tvDate: TextView
    private lateinit var btnDropdown: ImageButton
    private var username: String = ""
    private val calendar: Calendar = Calendar.getInstance()
    private var currentViewMode: ViewMode = ViewMode.DAILY
    private var selectedDate: String? = null

    enum class ViewMode {
        DAILY, MONTHLY, YEARLY
    }

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        initializeViews(view)
        setupPieChart()
        updateDateDisplay()
        loadExpenseData()
    }

    private fun initializeViews(view: View) {
        pieChart = view.findViewById(R.id.pieChart)
        tvDate = view.findViewById(R.id.tvDate)
        btnDropdown = view.findViewById(R.id.btnDropdown)

        btnDropdown.setOnClickListener {
            showDateFilterPopup(it)
        }
    }

    private fun showDateFilterPopup(anchorView: View) {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_date_filter, null)
        
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 팝업 메뉴 아이템 클릭 리스너 설정
        popupView.findViewById<TextView>(R.id.tvDaily).setOnClickListener {
            currentViewMode = ViewMode.DAILY
            updateDateDisplay()
            loadExpenseData()
            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.tvMonthly).setOnClickListener {
            currentViewMode = ViewMode.MONTHLY
            updateDateDisplay()
            loadExpenseData()
            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.tvYearly).setOnClickListener {
            currentViewMode = ViewMode.YEARLY
            updateDateDisplay()
            loadExpenseData()
            popupWindow.dismiss()
        }

        // 팝업 윈도우 표시
        popupWindow.showAsDropDown(anchorView)
    }

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

    private fun updateDateDisplay() {
        val dateFormat = when (currentViewMode) {
            ViewMode.DAILY -> SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
            ViewMode.MONTHLY -> SimpleDateFormat("yyyy년 M월", Locale.KOREA)
            ViewMode.YEARLY -> SimpleDateFormat("yyyy년", Locale.KOREA)
        }
        tvDate.text = dateFormat.format(calendar.time)
    }

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
                else -> 2f - (17f * (categoryCount - 5)) // 5개부터 0f, 이후 15씩 감소
            }
            maxSizePercent = if (categoryCount >= 5) 0.7f else 1f
        }

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        // 기본 색상 배열 수정
        val colorArray = intArrayOf(
            Color.rgb(69, 105, 169),  // 진한 파란색
            Color.rgb(170, 186, 147),  // 부드러운 녹색
            Color.rgb(239, 139, 98),   // 연한 주황색
            Color.rgb(155, 89, 182),   // 보라색
            Color.rgb(65, 179, 164),   // 청록색
            Color.rgb(225, 95, 109),   // 연한 빨간색
            Color.rgb(251, 177, 98),   // 밝은 주황색
            Color.rgb(108, 156, 178)   // 회청색
        )

        var totalAmount = 0f
        expenseData.values.forEach { amount ->
            totalAmount += amount
        }

        var index = 0
        expenseData.forEach { (category, amount) ->
            entries.add(PieEntry((amount / totalAmount) * 100f, category))
            colors.add(colorArray[index % colorArray.size])
            index++
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            setDrawValues(true)
            valueTextSize = 22f
            valueTextColor = Color.BLACK
            valueFormatter = PercentFormatter().apply {
                mFormat = java.text.DecimalFormat("##.0")
            }
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            setDrawIcons(false)
            sliceSpace = 3f
        }

        pieChart.data = PieData(dataSet).apply {
            setValueTextSize(22f)
            setDrawValues(true)
            setValueFormatter(PercentFormatter().apply {
                mFormat = java.text.DecimalFormat("##.0")
            })
        }
        pieChart.setEntryLabelColor(Color.TRANSPARENT)
        pieChart.setEntryLabelTextSize(0f)
        pieChart.invalidate()
    }

    companion object {
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