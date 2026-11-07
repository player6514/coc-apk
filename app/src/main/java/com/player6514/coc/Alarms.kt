package com.player6514.coc

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

/** 闹钟数据存在 SharedPreferences，开机后可重新登记 */
object AlarmStore {
    private const val PREF = "alarms"
    private fun sp(c: Context): SharedPreferences = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun put(c: Context, key: String, at: Long, title: String, body: String) {
        sp(c).edit().putString(key, "$at\u0001$title\u0001$body").apply()
    }

    fun remove(c: Context, key: String) {
        sp(c).edit().remove(key).apply()
    }

    fun all(c: Context): Map<String, Triple<Long, String, String>> =
        sp(c).all.mapNotNull { (k, v) ->
            val p = (v as? String)?.split('\u0001') ?: return@mapNotNull null
            if (p.size < 3) null else k to Triple(p[0].toLongOrNull() ?: 0L, p[1], p[2])
        }.toMap()
}

object Alarms {
    private fun pi(c: Context, key: String, title: String, body: String): PendingIntent {
        val i = Intent(c, AlarmReceiver::class.java).apply {
            putExtra("key", key)
            putExtra("title", title)
            putExtra("body", body)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(c, key.hashCode(), i, flags)
    }

    private fun showPi(c: Context, key: String): PendingIntent {
        val i = Intent(c, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            c, key.hashCode(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * 用 setAlarmClock 登记：这是系统里优先级最高的闹钟（和系统闹钟同级），
     * 后台被划掉、Doze 休眠都更不容易被吞掉。
     */
    fun schedule(c: Context, key: String, at: Long, title: String, body: String) {
        val am = c.getSystemService(AlarmManager::class.java) ?: return
        val p = pi(c, key, title, body)
        try {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, showPi(c, key)), p)
            return
        } catch (e: Exception) { }
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p)
        } catch (e: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, at, p)
        }
    }

    fun cancel(c: Context, key: String) {
        val am = c.getSystemService(AlarmManager::class.java) ?: return
        am.cancel(pi(c, key, "", ""))
    }

    fun rescheduleAll(c: Context) {
        val now = System.currentTimeMillis()
        AlarmStore.all(c).forEach { (k, v) ->
            if (v.first > now) schedule(c, k, v.first, v.second, v.third)
            else AlarmStore.remove(c, k)
        }
    }
}
