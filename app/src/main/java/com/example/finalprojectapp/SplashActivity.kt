package com.example.finalprojectapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

/**
 * 앱 실행 시 처음 표시되는 스플래시 화면을 담당하는 액티비티
 * 2초 동안 스플래시 화면을 표시한 후 로그인 화면으로 이동합니다.
 */
class SplashActivity : AppCompatActivity() {
    /**
     * 액티비티가 생성될 때 호출되는 메서드
     * 스플래시 화면을 표시하고 2초 후 로그인 화면으로 이동합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)
    }
} 