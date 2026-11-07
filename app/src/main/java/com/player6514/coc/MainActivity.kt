package com.player6514.coc

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            databaseEnabled = true
        }
        web.webViewClient = WebViewClient()
        web.addJavascriptInterface(Bridge(), "AndroidAlarm")
        web.loadUrl("file:///android_asset/index.html")

        // 启动时把没到点的闹钟重新登记一遍（换版本、重装、开机后都靠这个）
        Alarms.rescheduleAll(this)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        // 申请电池优化白名单：不然后台被清理后，闹钟可能被系统压掉
        if (Build.VERSION.SDK_INT >= 23) {
            val pm = getSystemService(android.os.PowerManager::class.java)
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
                } catch (e: Exception) { }
            }
        }

        if (Build.VERSION.SDK_INT >= 31) {
            val am = getSystemService(AlarmManager::class.java)
            if (am != null && !am.canScheduleExactAlarms()) {
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                } catch (e: Exception) { }
            }
        }
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    inner class Bridge {
        @JavascriptInterface
        fun set(key: String, at: Long, title: String, body: String) {
            AlarmStore.put(this@MainActivity, key, at, title, body)
            Alarms.schedule(this@MainActivity, key, at, title, body)
        }

        @JavascriptInterface
        fun cancel(key: String) {
            AlarmStore.remove(this@MainActivity, key)
            Alarms.cancel(this@MainActivity, key)
        }

        @JavascriptInterface
        fun version(): String = BuildConfig.VERSION_NAME

        @JavascriptInterface
        fun cancelAll() {
            AlarmStore.all(this@MainActivity).keys.toList().forEach { cancel(it) }
        }
    }
}


