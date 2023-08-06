package com.jang.tel_project

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jang.tel_project.FavoritesFragment
import com.jang.tel_project.PhoneNumberFragment
import com.jang.tel_project.R
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("phone_data", Context.MODE_PRIVATE)

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
        val jsonData = sharedPreferences.getString("phone_data", "")
        val universityData = sharedPreferences.getString("university_data", "")
        val institutionData = sharedPreferences.getString("institution_data", "")

        if (jsonData.isNullOrEmpty() || universityData.isNullOrEmpty() || institutionData.isNullOrEmpty()) {
            // JSON 데이터가 없을 경우, 웹 크롤링하여 데이터 추출하고 저장
            crawlPhoneNumberData()
        } else {
            // PhoneNumberFragment로 데이터 전달
            val phoneNumberData = JSONArray(jsonData)
            val phoneNumberFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as PhoneNumberFragment
            phoneNumberFragment.setData(phoneNumberData)
        }

        // 로그로 데이터 확인
        Log.d("JSON_DATA", "phone_data: $jsonData")
        Log.d("JSON_DATA", "university_data: $universityData")
        Log.d("JSON_DATA", "institution_data: $institutionData")
    }

    private fun crawlPhoneNumberData() {
        val url = "https://www.swu.ac.kr/www/camd.html"
        url.httpGet().responseString { _, response, result ->
            when (result) {
                is Result.Success -> {
                    val content = result.get()
                    // jsoup을 사용하여 웹 페이지 파싱
                    val doc = Jsoup.parse(content)

                    val titleArr = mutableListOf<String>()
                    val phoneNumberData = JSONArray()

                    val listElements = doc.select("ul.pnl_list > li")
                    for (listElement in listElements) {
                        val title = listElement.select("h1").text()
                        titleArr.add(title)

                        // 팀명이 존재한다면, 해당 팀원들의 정보를 추출하여 저장
                        val teamName = listElement.select("div.tt2").text()
                        if (teamName.isNotEmpty()) {
                            val teamMembers = listElement.select("table tbody tr")
                            for (teamMember in teamMembers) {
                                val memberName = teamMember.select("td.label").text()
                                val memberPhoneNumber = teamMember.select("td.label + td").text()

                                val jsonObject = JSONObject()
                                jsonObject.put("name", memberName)
                                jsonObject.put("phone", memberPhoneNumber)
                                phoneNumberData.put(jsonObject)
                            }
                        }
                    }

                    // 크롤링한 데이터를 SharedPreferences에 저장
                    val editor = sharedPreferences.edit()
                    editor.putString("phone_data", phoneNumberData.toString())
                    editor.apply()

                    // 타이틀 배열에서 "대학" 문자열이 있는지 확인하고, 대학과 기관으로 데이터 분류
                    val universityData = JSONArray()
                    val institutionData = JSONArray()
                    for (i in titleArr.indices) {
                        val title = titleArr[i]
                        val dataObject = JSONObject()
                        dataObject.put("title", title)
                        if (title.contains("대학")) {
                            universityData.put(dataObject)
                        } else {
                            institutionData.put(dataObject)
                        }
                    }

                    // 크롤링한 대학과 기관 데이터를 SharedPreferences에 저장
                    editor.putString("university_data", universityData.toString())
                    editor.putString("institution_data", institutionData.toString())
                    editor.apply()
                }
                is Result.Failure -> {
                    val ex = result.getException()
                    ex.printStackTrace()
                }
            }
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}