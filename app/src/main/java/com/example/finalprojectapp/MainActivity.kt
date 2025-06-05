package com.example.finalprojectapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

/**
 * 앱의 메인 화면을 관리하는 액티비티
 * 하단 네비게이션을 통해 홈, 분석, 마이페이지 프래그먼트를 전환하며
 * 선택된 날짜 정보를 전역적으로 관리합니다.
 */
class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    // 하단 네비게이션 뷰 참조
    private lateinit var bottomNavigation: BottomNavigationView
    // 로그인한 사용자의 아이디
    private var username: String = ""
    // 현재 선택된 날짜 (프래그먼트 간 공유됨)
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 앱 실행 시 오늘 날짜로 초기화 (최초 실행 시에만)
        if (savedInstanceState == null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            selectedDate = dateFormat.format(Calendar.getInstance().time)
        }

        initializeViews()
        setupNavigation(savedInstanceState)
    }

    /**
     * 선택된 날짜를 업데이트하는 메서드
     * HomeFragment에서 날짜가 선택될 때 호출됩니다.
     */
    fun updateSelectedDate(date: String?) {
        selectedDate = date
    }

    /**
     * 뷰 초기화 및 사용자 정보 설정
     */
    private fun initializeViews() {
        username = intent.getStringExtra("username") ?: ""
        if (username.isEmpty()) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener(this)
    }

    /**
     * 네비게이션 초기 설정
     * 앱 실행 시 홈 프래그먼트를 기본으로 표시
     */
    private fun setupNavigation(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            loadFragment(createHomeFragment())
        }
    }

    /**
     * 하단 네비게이션 아이템 선택 시 해당하는 프래그먼트로 전환
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment = when (item.itemId) {
            R.id.navigation_home -> createHomeFragment()
            R.id.navigation_analysis -> createAnalysisFragment()
            R.id.navigation_mypage -> createMyPageFragment()
            else -> return false
        }

        return loadFragment(fragment)
    }

    /**
     * 홈 프래그먼트 생성
     * 사용자 정보와 선택된 날짜 정보를 전달
     */
    private fun createHomeFragment(): Fragment {
        return HomeFragment.newInstance().apply {
            arguments = Bundle().apply {
                putString("username", username)
                putString("selected_date", selectedDate)
            }
        }
    }

    /**
     * 분석 프래그먼트 생성
     * 사용자 정보와 선택된 날짜 정보를 전달
     */
    private fun createAnalysisFragment(): Fragment {
        return AnalysisFragment.newInstance(username, selectedDate)
    }

    /**
     * 마이페이지 프래그먼트 생성
     * 사용자 정보 전달
     */
    private fun createMyPageFragment(): Fragment {
        return MyPageFragment().apply {
            arguments = Bundle().apply {
                putString("username", username)
            }
        }
    }

    /**
     * 프래그먼트 전환을 처리하는 메서드
     */
    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}