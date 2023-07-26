package com.jang.tel_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.*

class StartActivity : AppCompatActivity() {

    private val loadingTime = 2500L // 로딩 화면을 보여줄 시간(밀리초)
    private var job: Job? = null // job 변수를 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // 메모리 누수 방지를 위해 onDestroy에서 잡아주기 위한 Job
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(loadingTime) // 일정 시간 동안 대기
            navigateToMainActivity() // MainActivity로 이동
        }

        // 앱이 백그라운드로 가면 로딩 화면을 띄우지 않도록
        job?.invokeOnCompletion {
            if (it != null && !job?.isCancelled!!) {
                job?.cancel() // Job이 완료되지 않았을 경우 취소
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // StartActivity 종료

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        // 페이드 인(fade_in)과 페이드 아웃(fade_out) 애니메이션 적용
    }

    override fun onDestroy() {
        super.onDestroy()
        // 메모리 누수 방지를 위해 Job 취소
        job?.cancel()
    }
}