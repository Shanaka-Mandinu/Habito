package com.example.habito.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habito.utils.HydrationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val manager = HydrationManager.getInstance(context)
                manager.updateSchedule()
            }
        }
    }
}
