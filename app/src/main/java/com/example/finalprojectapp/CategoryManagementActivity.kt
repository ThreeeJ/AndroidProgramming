package com.example.finalprojectapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * 카테고리 관리 화면을 담당하는 액티비티
 * 수입/지출 카테고리를 탭으로 구분하여 관리할 수 있습니다.
 */
class CategoryManagementActivity : AppCompatActivity() {
    // 수입/지출 탭을 전환하는 ViewPager
    private lateinit var viewPager: ViewPager2
    // 수입/지출 탭을 표시하는 TabLayout
    private lateinit var tabLayout: TabLayout

    /**
     * 액티비티가 생성될 때 호출되는 메서드
     * ViewPager와 TabLayout을 초기화하고 연결합니다.
     */
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

    /**
     * 수입/지출 카테고리 프래그먼트를 관리하는 ViewPager 어댑터
     * @param fa 프래그먼트 액티비티 인스턴스
     */
    private inner class CategoryPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        /**
         * 탭의 개수를 반환하는 메서드 (수입/지출 2개)
         */
        override fun getItemCount() = 2

        /**
         * 각 탭의 프래그먼트를 생성하는 메서드
         * @param position 탭의 위치 (0: 수입, 1: 지출)
         */
        override fun createFragment(position: Int): Fragment {
            return CategoryListFragment.newInstance(if (position == 0) "income" else "expense")
        }
    }
} 