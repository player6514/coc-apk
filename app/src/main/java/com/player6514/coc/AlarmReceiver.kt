package com.player6514.coc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val key = intent.getStringExtra("key") ?: return
        val title = intent.getStringExtra("title") ?: "村庄监工"
        val body = intent.getStringExtra("body") ?: ""

        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel("coc", "升级提醒", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(ctx, "coc")
        else @Suppress("DEPRECATION") Notification.Builder(ctx)
        val n = builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .build()
        try { nm.notify(key.hashCode(), n) } catch (e: SecurityException) { }

        AlarmStore.remove(ctx, key)
    }
}
