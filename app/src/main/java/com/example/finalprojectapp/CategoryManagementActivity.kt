package com.example.finalprojectapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CategoryManagementActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_category_management)

            viewPager = findViewById(R.id.viewPager)
            tabLayout = findViewById(R.id.tabLayout)

            if (viewPager == null || tabLayout == null) {
                throw Exception("필수 뷰를 찾을 수 없습니다.")
            }

            viewPager.adapter = CategoryPagerAdapter(this)

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = if (position == 0) "수입" else "지출"
            }.attach()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "카테고리 관리 화면 초기화 중 오류가 발생했습니다: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private inner class CategoryPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            return CategoryListFragment.newInstance(if (position == 0) "income" else "expense")
        }
    }
} 