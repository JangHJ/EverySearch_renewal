package com.jang.tel_project

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 웹 크롤링 및 데이터 저장
        loadPhoneNumberData()

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_tel -> {
                    replaceFragment(PhoneNumberFragment())
                    true
                }
                R.id.navigation_book -> {
                    replaceFragment(FavoritesFragment())
                    true
                }
                else -> false
            }
        }

        // 기본으로 전화번호 탭 선택
        bottomNavigationView.selectedItemId = R.id.navigation_tel
    }

    private fun loadPhoneNumberData() {
        val jsonData = loadDataFromStorage("phone_data.json")
        if (jsonData.isEmpty()) {
            // JSON 데이터가 없을 경우, 웹 크롤링하여 데이터 추출하고 저장
            val newData = crawlPhoneNumberData()
            saveDataToStorage("phone_data.json", newData)
        }
    }

    private fun loadDataFromStorage(fileName: String): String {
        val fileInputStream: FileInputStream
        try {
            fileInputStream = openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(fileInputStream))
            val content = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                content.append(line)
            }
            reader.close()
            return content.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun saveDataToStorage(fileName: String, jsonData: String) {
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
            fileOutputStream.write(jsonData.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun crawlPhoneNumberData(): String {
        // 웹 크롤링하여 전화번호 데이터 추출하는 코드 작성
        val phoneNumberData = JSONArray()

        try {
            val url = URL("https://www.swu.ac.kr/www/camd.html")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = conn.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    content.append(line)
                }
                reader.close()

                // jsoup을 사용하여 웹 페이지 파싱
                val doc = Jsoup.parse(content.toString())
                // 파싱된 데이터를 JSONArray에 추가
                // 예시로 이름과 전화번호 추출
                val nameElements = doc.select(".name")
                val phoneElements = doc.select(".phone")

                for (i in 0 until nameElements.size) {
                    val name = nameElements[i].text()
                    val phone = phoneElements[i].text()

                    val jsonObject = JSONObject()
                    jsonObject.put("name", name)
                    jsonObject.put("phone", phone)

                    phoneNumberData.put(jsonObject)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return phoneNumberData.toString()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
