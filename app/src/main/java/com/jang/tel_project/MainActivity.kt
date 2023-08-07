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
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.toolbox.HttpClient

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
            Log.d("TEST@@@", "phoneNumberData: $phoneNumberData")
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

                    val phoneNumberData = JSONArray()

                    Log.d("TEST@@", "phoneNumberData: $phoneNumberData")

                    val listElements = doc.select("ul.pnl_list > li")
                    for (listElement in listElements) {
                        val title = listElement.select("h1").text()
                        val teamName = listElement.select("div.tt2").text()

                        val group = if (teamName.isNotEmpty()) {
                            "$title - $teamName" // tt2 내용이 있을 경우 title과 tt2 내용을 합쳐서 group 생성
                        } else {
                            title // tt2 내용이 없을 경우 그냥 title 사용
                        }

                        val teamMembers = listElement.select("div.table0.center table tbody tr")
                        for (i in 0 until teamMembers.size step 2) {
                            val memberName = teamMembers[i].select("td").text()
                            val memberPhoneNumber = teamMembers[i + 1].select("td").text()

                            val jsonObject = JSONObject()
                            jsonObject.put("group", group)
                            jsonObject.put("name", memberName)
                            jsonObject.put("phone", memberPhoneNumber)
                            phoneNumberData.put(jsonObject)
                        }
                    }

                    // 크롤링한 데이터를 SharedPreferences에 저장
                    val editor = sharedPreferences.edit()
                    editor.putString("phone_data", phoneNumberData.toString())
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