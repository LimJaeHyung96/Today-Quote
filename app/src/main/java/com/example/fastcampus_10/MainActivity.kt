package com.example.fastcampus_10

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {

    private val viewPager: ViewPager2 by lazy {
        findViewById(R.id.viewPager)
    }

    private val progressBar : ProgressBar by lazy {
        findViewById(R.id.progressBar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initData()
    }

    private fun initViews() {
        viewPager.setPageTransformer { page, position ->
            when {
                position.absoluteValue>= 1.0F -> {
                    page.alpha= 0F
                }
                position == 0F -> {
                    page.alpha = 1F
                }
                else -> {
                    page.alpha = 1F - (position.absoluteValue * 2)
                }
            }
        }
    }

    private fun initData() {
        val remoteConfig = Firebase.remoteConfig

        //원래는 한 번하고 12시간 뒤에 수정이 이루어지는데 개발하면서 확인하기 위해서 그 텀을 0으로 줄임
        //서버에서 블락하지 않으면 바꾸는 내용이 바로 적용됨
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
        )

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            progressBar.visibility = View.GONE
            if (it.isSuccessful) {
                val quote = parseQuotesJson(remoteConfig.getString("quotes"))
                val isNameReveal = remoteConfig.getBoolean("is_name_reveal")
                displayQuotesPager(quote, isNameReveal)
            }
        }
    }

    private fun parseQuotesJson(remoteConfigJson: String): List<Quote> {
        val jsonArray = JSONArray(remoteConfigJson)
        var jsonList = emptyList<JSONObject>()
        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            jsonObject?.let {
                jsonList = jsonList + it
            }
        }

        return jsonList.map {
            Quote(
                quote = it.getString("quote"),
                name = it.getString("name")
            )
        }
    }

    private fun displayQuotesPager(quote: List<Quote>, isNameReveal : Boolean) {
        val adapter = QuotePagerAdapter(
            quotes = quote,
            isNameRevealed = isNameReveal
        )

        viewPager.adapter= adapter
        viewPager.setCurrentItem(adapter.itemCount / 2 + 2 , false)
    }

}