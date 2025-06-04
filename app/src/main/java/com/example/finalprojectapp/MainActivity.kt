package com.example.finalprojectapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var bottomNavigation: BottomNavigationView
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupNavigation(savedInstanceState)
    }

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

    private fun setupNavigation(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            loadFragment(createHomeFragment())
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment = when (item.itemId) {
            R.id.navigation_home -> createHomeFragment()
            R.id.navigation_analysis -> {
                Toast.makeText(this, "분석 화면 준비 중", Toast.LENGTH_SHORT).show()
                return false
            }
            R.id.navigation_mypage -> createMyPageFragment()
            else -> return false
        }

        return loadFragment(fragment)
    }

    private fun createHomeFragment() = HomeFragment.newInstance().apply {
        arguments = Bundle().apply {
            putString("username", username)
        }
    }

    private fun createMyPageFragment() = MyPageFragment.newInstance().apply {
        arguments = Bundle().apply {
            putString("username", username)
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}